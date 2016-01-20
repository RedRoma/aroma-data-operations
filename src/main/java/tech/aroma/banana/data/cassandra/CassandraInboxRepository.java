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
import tech.aroma.banana.data.InboxRepository;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;

import static tech.aroma.banana.data.assertions.RequestAssertions.validMessage;
import static tech.aroma.banana.data.assertions.RequestAssertions.validUser;
import static tech.aroma.banana.data.assertions.RequestAssertions.validUserId;
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
    private final QueryBuilder queryBuilder;
    private final Function<Row, Message> messageMapper;

    @Inject
    CassandraInboxRepository(Session cassandra,
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
    public void saveMessageForUser(Message message, User user) throws TException
    {
        checkThat(message)
            .throwing(InvalidArgumentException.class)
            .is(validMessage());

        checkThat(user)
            .throwing(InvalidArgumentException.class)
            .is(validUser());

        Statement insertStatement = createInsertStatementFor(message, user);

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

    private Statement createInsertStatementFor(Message message, User user)
    {
        UUID msgUuid = UUID.fromString(message.messageId);
        UUID userUuid = UUID.fromString(user.userId);
        
        return null;
    }

    private Statement createQueryToGetMessagesFor(String userId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void checkUserId(String userId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Statement createQueryToCheckIfInInboxOf(String userId, Message message)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void checkMessageId(String messageId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Statement createDeleteStatementFor(String userId, String messageId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Statement createDeleteAllStatementFor(String userId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Statement createQueryToCountMessagesFor(String userId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
