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
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.InboxRepository;
import tech.aroma.data.cassandra.Tables.Inbox;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.functions.TimeFunctions;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static com.datastax.driver.core.querybuilder.QueryBuilder.desc;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.ttl;
import static tech.aroma.data.assertions.RequestAssertions.validMessage;
import static tech.aroma.data.assertions.RequestAssertions.validMessageId;
import static tech.aroma.data.assertions.RequestAssertions.validUser;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class CassandraInboxRepository implements InboxRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(CassandraInboxRepository.class);

    private final Session cassandra;
    private final Function<Row, Message> messageMapper;

    @Inject
    CassandraInboxRepository(Session cassandra,
                             Function<Row, Message> messageMapper)
    {
        checkThat(cassandra, messageMapper)
            .are(notNull());

        this.cassandra = cassandra;
        this.messageMapper = messageMapper;
    }

    @Override
    public void saveMessageForUser(@Required User user, @Required Message message,  @Required LengthOfTime lifetime) throws TException
    {
        checkThat(message)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(validMessage());

        checkThat(user)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(validUser());

        Statement insertStatement = createStatementToSaveMessage(message, user, lifetime);

        try
        {
            cassandra.execute(insertStatement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to save Message in Cassandra Inbox. User [{}] Message [{}]", user.userId, message, ex);
            throw new OperationFailedException("Could not save message in Inbox: " + ex.getMessage());
        }
    }

    @Override
    public List<Message> getMessagesForUser(String userId) throws TException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(validUserId());

        Statement query = createQueryToGetMessagesFor(userId);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query for Messages in Inbox for User [{}]", userId, ex);
            throw new OperationFailedException("Could not fetch inbox: " + ex.getMessage());
        }
        
        checkThat(results)
            .throwing(OperationFailedException.class)
            .usingMessage("Cassandra returned null results")
            .is(notNull());

        List<Message> messages = Lists.create();

        for (Row row : results)
        {
            Message message = messageMapper.apply(row);
            messages.add(message);
        }

        return messages;
    }

    @Override
    public boolean containsMessageInInbox(String userId, Message message) throws TException
    {
        checkUserId(userId);

        checkThat(message)
            .throwing(InvalidArgumentException.class)
            .is(validMessage());

        Statement query = createQueryToCheckIfInInboxOf(userId, message);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query Cassandra for presence of message [{}] for User [{}]", message.messageId, userId, ex);
            throw new OperationFailedException("Could not check if message exists: " + ex.getMessage());
        }

        Row row = results.one();
        checkThat(row)
            .throwing(OperationFailedException.class)
            .usingMessage("Query for message failed")
            .is(notNull());

        long count = row.getLong(0);
        return count > 0;
    }

    @Override
    public void deleteMessageForUser(String userId, String messageId) throws TException
    {
        checkUserId(userId);
        checkMessageId(messageId);

        Statement deleteStatement = createDeleteStatementFor(userId, messageId);

        try
        {
            cassandra.execute(deleteStatement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to delete message [{}] for User [{}] from Inbox", messageId, userId, ex);
            throw new OperationFailedException("Could not delete message: " + ex.getMessage());
        }

    }

    @Override
    public void deleteAllMessagesForUser(String userId) throws TException
    {
        checkUserId(userId);

        Statement deleteStatement = createDeleteAllStatementFor(userId);

        try
        {
            cassandra.execute(deleteStatement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to delete all messages for User [{}] from Inbox", userId, ex);
            throw new OperationFailedException("Could not delete message: " + ex.getMessage());
        }
    }

    @Override
    public long countInboxForUser(String userId) throws TException
    {
        checkUserId(userId);

        Statement query = createQueryToCountMessagesFor(userId);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to count total messages for User [{}]", userId, ex);
            throw new OperationFailedException("Could not count messags for user: " + ex.getMessage());
        }

        Row row = results.one();
        checkThat(row)
            .throwing(OperationFailedException.class)
            .usingMessage("Query for message failed")
            .is(notNull());

        long count = row.getLong(0);
        return count;
    }

    private Statement createStatementToSaveMessage(Message message, User user, LengthOfTime lifetime)
    {
        //UUIDs
        UUID msgUuid = UUID.fromString(message.messageId);
        UUID userUuid = UUID.fromString(user.userId);
        UUID appUuid = UUID.fromString(message.applicationId);
        
        //Urgency
        String urgency = null;
        if (message.urgency != null)
        {
            urgency = message.urgency.toString();
        }
        
        long timeToLive = TimeFunctions.toSeconds(lifetime);

        return QueryBuilder
            .insertInto(Inbox.TABLE_NAME)
            .value(Inbox.USER_ID, userUuid)
            .value(Inbox.MESSAGE_ID, msgUuid)
            .value(Inbox.BODY, message.body)
            .value(Inbox.APP_ID, appUuid)
            .value(Inbox.URGENCY, urgency)
            .value(Inbox.TITLE, message.title)
            .value(Inbox.TIME_CREATED, message.timeOfCreation)
            .value(Inbox.TIME_RECEIVED, message.timeMessageReceived)
            .value(Inbox.DEVICE_NAME, message.deviceName)
            .value(Inbox.HOSTNAME, message.hostname)
            .value(Inbox.MAC_ADDRESS, message.macAddress)
            .value(Inbox.APP_NAME, message.applicationName)
            .using(ttl((int) timeToLive));

    }

    private Statement createQueryToGetMessagesFor(String userId)
    {
        UUID userUuid = UUID.fromString(userId);

        return QueryBuilder
            .select()
            .all()
            .from(Inbox.TABLE_NAME)
            .where(eq(Inbox.USER_ID, userUuid))
            .orderBy(desc(Inbox.MESSAGE_ID))
            .limit(5_000);
    }

    private Statement createQueryToCheckIfInInboxOf(String userId, Message message)
    {
        UUID userUuid = UUID.fromString(userId);
        UUID msgUuid = UUID.fromString(message.messageId);

        return QueryBuilder
            .select()
            .countAll()
            .from(Inbox.TABLE_NAME)
            .where(eq(Inbox.USER_ID, userUuid))
            .and(eq(Inbox.MESSAGE_ID, msgUuid));

    }

    private Statement createDeleteStatementFor(String userId, String messageId)
    {
        UUID userUuid = UUID.fromString(userId);
        UUID msgUuid = UUID.fromString(messageId);
        
        return QueryBuilder
            .delete()
            .all()
            .from(Inbox.TABLE_NAME)
            .where(eq(Inbox.USER_ID, userUuid))
            .and(eq(Inbox.MESSAGE_ID, msgUuid));
    }

    private Statement createDeleteAllStatementFor(String userId)
    {
        UUID userUuid = UUID.fromString(userId);
        
        return QueryBuilder
            .delete()
            .all()
            .from(Inbox.TABLE_NAME)
            .where(eq(Inbox.USER_ID, userUuid));
    }

    private Statement createQueryToCountMessagesFor(String userId)
    {
        UUID userUuid = UUID.fromString(userId);
        
        return QueryBuilder
            .select()
            .countAll()
            .from(Inbox.TABLE_NAME)
            .where(eq(Inbox.USER_ID, userUuid));
    }

    private void checkMessageId(String messageId) throws InvalidArgumentException
    {
        checkThat(messageId)
            .throwing(InvalidArgumentException.class)
            .is(validMessageId());
    }

    private void checkUserId(String userId) throws InvalidArgumentException
    {
        checkThat(userId)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(validUserId());
    }

}
