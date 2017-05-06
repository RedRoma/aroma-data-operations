package tech.aroma.data.sql;

import java.time.Duration;
import java.util.*;
import javax.inject.Inject;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.data.MessageRepository;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.functions.TimeFunctions;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.Optional;

import static tech.aroma.data.assertions.RequestAssertions.validLengthOfTime;
import static tech.aroma.data.assertions.RequestAssertions.validMessage;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 * Saves and retrieves {@linkplain Message Messages} from the SQL Database.
 */
@Internal
final class SQLMessageRepository implements MessageRepository
{

    private static final Logger LOG = LoggerFactory.getLogger(SQLMessageRepository.class);

    private final JdbcTemplate database;
    private final DatabaseDeserializer<Message> messageDeserializer;
    private final DatabaseSerializer<Message> messageSerializer;

    @Inject
    SQLMessageRepository(JdbcTemplate database, DatabaseDeserializer<Message> messageDeserializer, DatabaseSerializer<Message> messageSerializer)
    {
        checkThat(database, messageDeserializer, messageSerializer)
                .are(notNull());

        this.database = database;
        this.messageDeserializer = messageDeserializer;
        this.messageSerializer = messageSerializer;
    }

    @Override
    public void saveMessage(Message message, @Optional LengthOfTime lifetime) throws TException
    {
        checkThat(message)
                .throwing(InvalidArgumentException.class)
                .is(notNull())
                .is(validMessage());

        Duration messageDuration = null;

        if (Objects.nonNull(lifetime))
        {

            checkThat(lifetime)
                    .throwing(InvalidArgumentException.class)
                    .is(validLengthOfTime());

            messageDuration = TimeFunctions.lengthOfTimeToDuration().apply(lifetime);
        }

        _saveMessage(message, messageDuration);
    }

    private void _saveMessage(Message message, Duration messageDuration) throws OperationFailedException
    {
        final String statement = SQLStatements.Inserts.MESSAGE;

        try
        {
            messageSerializer.save(message, messageDuration, statement, database);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to serialize Message {} using statement [{}]", message, statement);
            throw new OperationFailedException();
        }
    }

    @Override
    public Message getMessage(String applicationId, String messageId) throws TException
    {
        checkThat(applicationId, messageId)
                .throwing(InvalidArgumentException.class)
                .are(validUUID());

        final UUID appId = UUID.fromString(applicationId);
        final UUID msgId = UUID.fromString(messageId);
        final String statement = SQLStatements.Queries.SELECT_MESSAGE;

        Message message;

        try
        {
            message = database.queryForObject(statement, messageDeserializer, appId, msgId);

        }
        catch (Exception ex)
        {
            LOG.error("Failed to get message [{}/{}]", applicationId, messageId, ex);
            throw new OperationFailedException(ex.getMessage());
        }

        checkThat(message)
                .throwing(DoesNotExistException.class)
                .is(notNull());

        return message;
    }

    @Override
    public void deleteMessage(String applicationId, String messageId) throws TException
    {
        checkThat(applicationId, messageId)
                .throwing(InvalidArgumentException.class)
                .are(validUUID());

        UUID appId = UUID.fromString(applicationId);
        UUID msgId = UUID.fromString(messageId);
        String statement = SQLStatements.Deletes.MESSAGE;

        try
        {
            int updatedRows = database.update(statement, appId, msgId);

            LOG.debug("{} rows affected deleting message [{}/{}]", updatedRows, applicationId, messageId);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to delete message [{}/{}]", applicationId, messageId);
            throw new OperationFailedException(ex.getMessage());
        }
    }

    @Override
    public boolean containsMessage(String applicationId, String messageId) throws TException
    {
        checkThat(applicationId, messageId)
                .throwing(InvalidArgumentException.class)
                .are(validUUID());

        UUID appId = UUID.fromString(applicationId);
        UUID msgId = UUID.fromString(messageId);
        String statement = SQLStatements.Queries.CHECK_MESSAGE;

        try
        {
            return database.queryForObject(statement, Boolean.class, appId, msgId);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to check whether message exists: [{}/{}]", appId, msgId);
            throw new OperationFailedException(ex.getMessage());
        }
    }

    @Override
    public List<Message> getByHostname(String hostname) throws TException
    {
        checkThat(hostname)
                .throwing(InvalidArgumentException.class)
                .is(nonEmptyString());

        String statement = SQLStatements.Queries.SELECT_MESSAGES_BY_HOSTNAME;

        return database.query(statement, messageDeserializer, hostname);
    }

    @Override
    public List<Message> getByApplication(String applicationId) throws TException
    {
        checkThat(applicationId)
                .throwing(InvalidArgumentException.class)
                .is(validUUID());

        UUID appId = UUID.fromString(applicationId);
        String query = SQLStatements.Queries.SELECT_APP_MESSAGES;

        try
        {
            return database.query(query, messageDeserializer, appId);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to get messages for Application: {}", appId, ex);
            throw new OperationFailedException(ex.getMessage());
        }
    }


    @Override
    public List<Message> getByTitle(String applicationId, String title) throws TException
    {
        checkThat(applicationId, title)
                .throwing(InvalidArgumentException.class)
                .are(nonEmptyString());

        checkThat(applicationId)
                .throwing(InvalidArgumentException.class)
                .is(validUUID());

        UUID appId = UUID.fromString(applicationId);
        String query = SQLStatements.Queries.SELECT_MESSAGES_BY_TITLE;

        try
        {
            return database.query(query, messageDeserializer, appId, title);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query for messages by title [{}/{}]", applicationId, title);
            throw new OperationFailedException(ex.getMessage());
        }
    }

    @Override
    public long getCountByApplication(String applicationId) throws TException
    {
        checkThat(applicationId)
                .throwing(InvalidArgumentException.class)
                .is(nonEmptyString())
                .is(validUUID());

        UUID appId = UUID.fromString(applicationId);
        String query = SQLStatements.Queries.COUNT_MESSAGES;


        try
        {
            return database.queryForObject(query, Long.class, appId);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to determine the number of messages for App [{}]", applicationId);
            throw new OperationFailedException(ex.getMessage());
        }
    }
}
