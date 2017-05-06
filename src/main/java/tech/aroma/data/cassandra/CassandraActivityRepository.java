/*
 * Copyright 2017 RedRoma, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.aroma.data.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.cassandra.Tables.Activity;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.functions.TimeFunctions;
import tech.sirwellington.alchemy.thrift.ThriftObjects;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.ttl;
import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.BooleanAssertions.trueStatement;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
 * @author SirWellington
 */
final class CassandraActivityRepository implements ActivityRepository
{
    
    private final static Logger LOG = LoggerFactory.getLogger(CassandraActivityRepository.class);
    
    private final Session session;
    private final Function<Row, Event> eventMapper;
    
    @Inject
    CassandraActivityRepository(Session session, Function<Row, Event> eventMapper)
    {
        checkThat(session, eventMapper)
            .are(notNull());
        
        this.session = session;
        this.eventMapper = eventMapper;
    }
    
    @Override
    public void saveEvent(Event event, User forUser, LengthOfTime lifetime) throws TException
    {
        checkEvent(event);
        checkUser(forUser);
        checkLifetime(lifetime);
        
        User user = forUser;
        
        Statement insertStatement = createStatementToSaveEventForUser(event, user, lifetime);
        
        tryToExecute(insertStatement, "saveEvent");
    }
    
    @Override
    public boolean containsEvent(String eventId, User user) throws TException
    {
        checkEventId(eventId);
        checkUser(user);
        
        Statement query = createQueryToCheckIfEventExists(eventId, user);
        
        ResultSet results = tryToExecute(query, "containsEvent");
        Row row = results.one();
        checkThat(row)
            .throwing(OperationFailedException.class)
            .usingMessage("Failed to query for event with ID " + eventId)
            .is(notNull());
        
        return row.getLong(0) > 0L;
    }
    
    @Override
    public Event getEvent(String eventId, User user) throws TException
    {
        checkEventId(eventId);
        checkUser(user);
        
        Statement query = createQueryToGetEvent(eventId, user);
        
        ResultSet results = tryToExecute(query, "getEvent");
        
        Row row = results.one();
        checkThat(row)
            .throwing(DoesNotExistException.class)
            .usingMessage("No such event with ID " + eventId + " for user " + user)
            .is(notNull());
        
        return mapRowToEvent(row);
    }
    
    @Override
    public List<Event> getAllEventsFor(User user) throws TException
    {
        checkUser(user);
        
        Statement query = createQueryToGetAllEventsForUser(user);
        
        ResultSet results = tryToExecute(query, "getAllEvents");
        
        return results.all().parallelStream()
            .map(eventMapper::apply)
            .filter(Objects::nonNull)
            .collect(toList());
    }
    
    @Override
    public void deleteEvent(String eventId, User user) throws TException
    {
        checkEventId(eventId);
        checkUser(user);
        
        Statement deleteStatement = createStatementToDelete(eventId, user);
        
        tryToExecute(deleteStatement, "deleteEvent");
    }
    
    @Override
    public void deleteAllEventsFor(User user) throws TException
    {
        checkUser(user);
        
        Statement deleteStatement = createStatementToDeleteAllEventsFor(user);
        
        tryToExecute(deleteStatement, "deleteAllEvents");
    }
    
    private void checkUser(User user) throws InvalidArgumentException
    {
        checkThat(user)
            .usingMessage("user cannot be null")
            .throwing(InvalidArgumentException.class)
            .is(notNull());
        
        checkThat(user.userId)
            .throwing(InvalidArgumentException.class)
            .is(validUserId());
    }
    
    private void checkEvent(Event event) throws InvalidArgumentException
    {
        checkThat(event)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Event cannot be null")
            .is(notNull());
        
        checkThat(event.eventId)
            .usingMessage("eventId must be a valid UUID")
            .throwing(InvalidArgumentException.class)
            .is(validUUID());
        
        checkThat(event.eventType.isSet())
            .throwing(InvalidArgumentException.class)
            .usingMessage("EventType must be set")
            .is(trueStatement());
    }
    
    private Statement createStatementToSaveEventForUser(Event event, User user, LengthOfTime lifetime) throws TException
    {
        UUID eventId = UUID.fromString(event.eventId);
        UUID userId = UUID.fromString(user.userId);
        String serializedEvent = ThriftObjects.toJson(event);
        
        
        Insert statement = QueryBuilder
            .insertInto(Activity.TABLE_NAME)
            .value(Activity.USER_ID, userId)
            .value(Activity.EVENT_ID, eventId)
            .value(Activity.SERIALIZED_EVENT, serializedEvent);
        
        UUID appId;
        UUID actorId;
        Date timeOfEvent;
        
        if (event.isSetApplicationId())
        {
            appId = UUID.fromString(event.applicationId);
            statement = statement.value(Activity.APP_ID, appId);
        }
        
        
        if (event.isSetUserIdOfActor())
        {
            actorId = UUID.fromString(event.userIdOfActor);
            statement = statement.value(Activity.ACTOR_ID, actorId);
        }
        
        if (event.isSetTimestamp())
        {
            timeOfEvent = new Date(event.timestamp);
            statement = statement.value(Activity.TIME_OF_EVENT, timeOfEvent);
        }

        int ttl = (int) TimeFunctions.toSeconds(lifetime);

        return statement.using(ttl(ttl));
    }
    
    private ResultSet tryToExecute(Statement statement, String operationName) throws OperationFailedException
    {
        try
        {
            return session.execute(statement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to execute Cassandra Statement: {}", operationName, ex);
            throw new OperationFailedException("Could not perform operation: " + ex.getMessage());
        }
    }
    
    private void checkEventId(String eventId) throws InvalidArgumentException
    {
        checkThat(eventId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("eventId missing")
            .is(nonEmptyString())
            .usingMessage("eventId must be a valid uuid")
            .is(validUUID());
    }
    
    private Statement createQueryToCheckIfEventExists(String eventId, User user)
    {
        UUID eventUuid = UUID.fromString(eventId);
        UUID userUuid = UUID.fromString(user.userId);
        
        return QueryBuilder
            .select()
            .countAll()
            .from(Activity.TABLE_NAME)
            .where(eq(Activity.USER_ID, userUuid))
            .and(eq(Activity.EVENT_ID, eventUuid));
    }
    
    private Statement createQueryToGetEvent(String eventId, User user)
    {
        UUID eventUuid = UUID.fromString(eventId);
        UUID userUuid = UUID.fromString(user.userId);
        
        return QueryBuilder
            .select()
            .all()
            .from(Activity.TABLE_NAME)
            .where(eq(Activity.USER_ID, userUuid))
            .and(eq(Activity.EVENT_ID, eventUuid));
    }
    
    private Statement createQueryToGetAllEventsForUser(User user)
    {
        UUID userUuid = UUID.fromString(user.userId);
        
        return QueryBuilder
            .select()
            .all()
            .from(Activity.TABLE_NAME)
            .where(eq(Activity.USER_ID, userUuid));
    }
    
    private Statement createStatementToDelete(String eventId, User user)
    {
        UUID eventUuid = UUID.fromString(eventId);
        UUID userUuid = UUID.fromString(user.userId);
        
        return QueryBuilder
            .delete()
            .all()
            .from(Activity.TABLE_NAME)
            .where(eq(Activity.USER_ID, userUuid))
            .and(eq(Activity.EVENT_ID, eventUuid));
    }
    
    private Statement createStatementToDeleteAllEventsFor(User user)
    {
        UUID userUuid = UUID.fromString(user.userId);
        
        return QueryBuilder
            .delete()
            .all()
            .from(Activity.TABLE_NAME)
            .where(eq(Activity.USER_ID, userUuid));
    }

    private Event mapRowToEvent(Row row) throws DoesNotExistException
    {
        if (row == null)
        {
            return new Event();
        }

        Event event = eventMapper.apply(row);
        
        checkThat(event)
            .usingMessage("event does not exist")
            .throwing(DoesNotExistException.class)
            .is(notNull());
        
        return event;
    }

    private void checkLifetime(LengthOfTime lifetime) throws InvalidArgumentException
    {
        checkThat(lifetime)
            .throwing(InvalidArgumentException.class)
            .usingMessage("lifetime missing")
            .is(notNull());
        
        checkThat(lifetime.value)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Lifetime duration must be > 0")
            .is(greaterThan(0L));
    }
}
