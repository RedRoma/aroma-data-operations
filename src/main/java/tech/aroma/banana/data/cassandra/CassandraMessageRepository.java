/*
 * Copyright 2016 Aroma Tech.
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

package tech.aroma.banana.data.cassandra;

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
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.data.cassandra.Tables.Messages;
import tech.aroma.banana.thrift.LengthOfTime;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.MessageDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.functions.TimeFunctions;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.incr;
import static java.lang.String.format;
import static tech.aroma.banana.data.cassandra.Tables.Messages.APP_ID;
import static tech.aroma.banana.data.cassandra.Tables.Messages.APP_NAME;
import static tech.aroma.banana.data.cassandra.Tables.Messages.BODY;
import static tech.aroma.banana.data.cassandra.Tables.Messages.HOSTNAME;
import static tech.aroma.banana.data.cassandra.Tables.Messages.MAC_ADDRESS;
import static tech.aroma.banana.data.cassandra.Tables.Messages.MESSAGE_ID;
import static tech.aroma.banana.data.cassandra.Tables.Messages.TIME_CREATED;
import static tech.aroma.banana.data.cassandra.Tables.Messages.TIME_RECEIVED;
import static tech.aroma.banana.data.cassandra.Tables.Messages.TITLE;
import static tech.aroma.banana.data.cassandra.Tables.Messages.TOTAL_MESSAGES;
import static tech.aroma.banana.data.cassandra.Tables.Messages.URGENCY;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthGreaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
 * @author SirWellington
 */
final class CassandraMessageRepository implements MessageRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(CassandraMessageRepository.class);

    private final Session cassandra;
    private final QueryBuilder queryBuilder;
    private final Function<Row, Message> messageMapper;

    @Inject
    CassandraMessageRepository(Session cassandra, 
                               QueryBuilder queryBuilder,
                               Function<Row, Message> messageMapper)
    {
        checkThat(cassandra, queryBuilder, messageMapper)
            .are(notNull());

        this.cassandra = cassandra;
        this.queryBuilder = queryBuilder;
        this.messageMapper = messageMapper;
    }

    @Override
    public void saveMessage(Message message, LengthOfTime lifetime) throws TException
    {
        checkThat(message, lifetime)
            .throwing(InvalidArgumentException.class)
            .is(notNull());

        Statement insertStatement = createInsertForMessage(message, lifetime);
        Statement updateTotalMessagesByApp = createUpdateForMessageByApp(message);
        Statement updateTotalMessageByTitle = createUpdateForMessageCounterByTitle(message);
        
        try
        {
            cassandra.execute(insertStatement);
            LOG.debug("Successfully saved message in Cassandra with a lifetime of {}: {}", lifetime, message);
            cassandra.executeAsync(updateTotalMessageByTitle);
            cassandra.executeAsync(updateTotalMessagesByApp);
            LOG.debug("Successfully Updated Total Message Counters for App {} and title {}", message.applicationId, message.title);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to store message in Cassandra: {}", message, ex);
            throw new OperationFailedException("Could save Message");
        }
    }

    @Override
    public Message getMessage(String applicationId, String messageId) throws TException
    {
        checkMessageId(messageId);
        checkAppId(applicationId);

        Statement query = createQueryForMessageWithId(applicationId, messageId);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query Cassandra for Message with ID: {}", messageId, ex);
            throw new OperationFailedException("Could not query for message");
        }

        Row row = results.one();
        checkThat(row)
            .throwing(MessageDoesNotExistException.class)
            .usingMessage(format("No Message with App ID [%s] and Msg ID [%s]", applicationId, messageId))
            .is(notNull());
        
        Message message = createMessageFromRow(row);

        return message;
    }

    @Override
    public void deleteMessage(String applicationId, String messageId) throws TException
    {
        Message message = getMessage(applicationId, messageId);

        Statement deleteStatement = createDeleteStatementFor(message);

        try
        {
            cassandra.execute(deleteStatement);
            LOG.debug("Successfully delete Message with ID {} from Cassandra", messageId);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to delete message with ID [{}] from Cassandra", message, ex);
            throw new OperationFailedException("Could not delete message: " + messageId);
        }
    }

    @Override
    public boolean containsMessage(String applicationId, String messageId) throws TException
    {
        checkMessageId(messageId);
        checkAppId(applicationId);

        Statement query = createQueryToCheckIfMessageExists(applicationId, messageId);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query Cassadnra to check for existence of message with ID [{}]", messageId, ex);
            throw new OperationFailedException("Could not query for message: " + messageId);
        }

        Row row = results.one();
        checkRowIsPresent(row);

        long count = row.getLong(0);
        return count > 0;
    }

    @Override
    public List<Message> getByHostname(String hostname) throws TException
    {
        checkThat(hostname)
            .usingMessage("missing hostname")
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString())
            .is(stringWithLengthGreaterThanOrEqualTo(1));

        Statement query = createQueryToFindMessageByHostname(hostname);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query cassandra for Messages by hostname [{}]", hostname, ex);
            throw new OperationFailedException("Could not query for messages by hostname: " + hostname);
        }

        List<Message> messages = Lists.create();

        for (Row row : results)
        {
            Message message = createMessageFromRow(row);
            messages.add(message);
        }

        LOG.debug("Found {} messages by hostname {}", messages.size(), hostname);

        return messages;
    }

    @Override
    public List<Message> getByApplication(String applicationId) throws TException
    {
        checkAppId(applicationId);

        Statement query = createQueryToFindMessagesByApplication(applicationId);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query cassandra for Messages by app_id [{}]", applicationId, ex);
            throw new OperationFailedException("Could not query for messages by app: " + applicationId);
        }

        List<Message> messages = Lists.create();

        for (Row row : results)
        {
            Message message = createMessageFromRow(row);
            messages.add(message);
        }

        LOG.debug("Found {} messages by app with ID [{}]", messages.size(), applicationId);

        return messages;
    }

    @Override
    public List<Message> getByTitle(String applicationId, String title) throws TException
    {
        checkAppId(applicationId);
        checkTitle(title);

        Statement query = createQueryToFindMessagesByTitle(title);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query cassandra for Messages by app_id [{}] and title [{}]", applicationId, title, ex);
            throw new OperationFailedException("Could not query for messages by app and title" + applicationId + ", " + title);
        }

        List<Message> messages = Lists.create();

        for (Row row : results)
        {
            Message message = createMessageFromRow(row);
            messages.add(message);
        }

        LOG.debug("Found {} messages by app with ID [{}]", messages.size(), applicationId);

        return messages;
    }

    @Override
    public long getCountByApplication(String applicationId) throws TException
    {
        checkAppId(applicationId);

        Statement query = createQueryToCountMessagesByApplication(applicationId);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query cassandra for Messages by app_id [{}]", applicationId, ex);
            throw new OperationFailedException("Could not query for messages by app: " + applicationId);
        }

        Row row = results.one();
        checkRowIsPresent(row);

        return row.getLong(0);
    }

    private Statement createInsertForMessage(Message message, LengthOfTime lifetime) throws InvalidArgumentException
    {
        checkThat(message.messageId, message.applicationId)
            .throwing(InvalidArgumentException.class)
            .are(nonEmptyString())
            .are(validUUID());

        UUID msgId = UUID.fromString(message.messageId);
        UUID appId = UUID.fromString(message.applicationId);

        Long timeToLive = TimeFunctions.toSeconds(lifetime);

        return queryBuilder
            .insertInto(Messages.TABLE_NAME)
            .value(MESSAGE_ID, msgId)
            .value(APP_ID, appId)
            .value(APP_NAME, message.applicationName)
            .value(BODY, message.body)
            .value(HOSTNAME, message.hostname)
            .value(MAC_ADDRESS, message.macAddress)
            .value(TITLE, message.title)
            .value(URGENCY, message.urgency)
            .value(TIME_CREATED, message.timeOfCreation)
            .value(TIME_RECEIVED, message.timeMessageReceived)
            .using(QueryBuilder.ttl(timeToLive.intValue()));
    }

    private Statement createUpdateForMessageByApp(Message message)
    {
        UUID appId = UUID.fromString(message.applicationId);

        return queryBuilder
            .update(Messages.TABLE_NAME_TOTALS_BY_APP)
            .where(eq(APP_ID, appId))
            .with(incr(TOTAL_MESSAGES, 1));
    }

    private Statement createUpdateForMessageCounterByTitle(Message message)
    {
        UUID appId = UUID.fromString(message.applicationId);

        return queryBuilder
            .update(Messages.TABLE_NAME_TOTALS_BY_TITLE)
            .where(eq(APP_ID, appId))
            .and(eq(TITLE, message.title))
            .with(incr(TOTAL_MESSAGES, 1));
    }

    private void checkMessageId(String messageId) throws InvalidArgumentException
    {
        checkThat(messageId)
            .usingMessage("missing messageId")
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString())
            .usingMessage("messageId must be a UUID")
            .is(validUUID());
    }
    
    private Statement createQueryForMessageWithId(String applicationId, String messageId)
    {
        UUID appId = UUID.fromString(applicationId);
        UUID msgId = UUID.fromString(messageId);

        return queryBuilder
            .select()
            .all()
            .from(Messages.TABLE_NAME)
            .where(eq(MESSAGE_ID, msgId))
            .and(eq(APP_ID, appId))
            .limit(2);
    }

    private void checkRowIsPresent(Row row) throws OperationFailedException
    {
        checkThat(row)
            .usingMessage("query produced no rows")
            .throwing(OperationFailedException.class)
            .is(notNull());
    }

    private Message createMessageFromRow(Row row) throws OperationFailedException
    {
        return messageMapper.apply(row);
    }

    private Statement createDeleteStatementFor(Message message)
    {
        UUID msgId = UUID.fromString(message.messageId);
        UUID appId = UUID.fromString(message.applicationId);
        
        Statement deleteFromMainTable = queryBuilder
            .delete()
            .all()
            .from(Messages.TABLE_NAME)
            .where(eq(APP_ID, appId))
            .and(eq(MESSAGE_ID, msgId));
        
        return deleteFromMainTable;
    }

    private Statement createQueryToCheckIfMessageExists(String applicationId, String messageId)
    {
        UUID msgId = UUID.fromString(messageId);
        UUID appId = UUID.fromString(applicationId);
        
        return queryBuilder
            .select()
            .countAll()
            .from(Messages.TABLE_NAME)
            .where(eq(APP_ID, appId))
            .and(eq(MESSAGE_ID, msgId));
    }

    private Statement createQueryToFindMessageByHostname(String hostname)
    {
        return queryBuilder
            .select()
            .all()
            .from(Messages.TABLE_NAME)
            .where(eq(HOSTNAME, hostname));
    }

    private void checkTitle(String title) throws InvalidArgumentException
    {
        checkThat(title)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString())
            .is(stringWithLengthGreaterThanOrEqualTo(2));
    }

    private Statement createQueryToFindMessagesByApplication(String applicationId)
    {
        UUID appId = UUID.fromString(applicationId);
        
        return queryBuilder
            .select()
            .all()
            .from(Messages.TABLE_NAME)
            .where(eq(APP_ID, appId))
            .limit(2000);
    }

    private Statement createQueryToCountMessagesByApplication(String applicationId)
    {
        UUID appId = UUID.fromString(applicationId);
        
        return queryBuilder
            .select()
            .column(TOTAL_MESSAGES)
            .from(Messages.TABLE_NAME_TOTALS_BY_APP)
            .where(eq(APP_ID, appId));
    }

    private Statement createQueryToFindMessagesByTitle(String title)
    {
        return queryBuilder
            .select()
            .all()
            .from(Messages.TABLE_NAME)
            .where(eq(TITLE, title));
    }

    private void checkAppId(String applicationId) throws InvalidArgumentException
    {
        checkThat(applicationId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("missing appId")
            .is(nonEmptyString())
            .usingMessage("appId must be a UUID Type")
            .is(validUUID());
    }

}
