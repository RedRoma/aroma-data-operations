package tech.aroma.data.sql;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.MessageRepository;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.functions.TimeFunctions;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions;

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

    private JdbcTemplate database;
    private RowMapper<Message> messageDeserializer;
    private DatabaseSerializer<Message> messageSerializer;

    @Override
    public void saveMessage(Message message, LengthOfTime lifetime) throws TException
    {
        checkThat(message, lifetime)
                .throwing(InvalidArgumentException.class)
                .are(notNull());

        checkThat(message)
                .throwing(InvalidArgumentException.class)
                .is(validMessage());

        checkThat(lifetime)
                .throwing(InvalidArgumentException.class)
                .is(validLengthOfTime());


        Duration messageDuration = TimeFunctions.lengthOfTimeToDuration().apply(lifetime);

        _saveMessage(message, messageDuration);
    }

    private void _saveMessage(Message message, Duration messageDuration) throws OperationFailedException
    {
        final String statement = SQLStatements.Inserts.MESSAGE;

        try
        {
            messageSerializer.save(message, messageDuration, statement, database);
        }
        catch(SQLException ex)
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

        Message message = database.queryForObject(statement,
                                                  messageDeserializer,
                                                  appId,
                                                  msgId);

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
        catch(Exception ex)
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
        catch(Exception ex)
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

        return Lists.emptyList();
    }

    @Override
    public List<Message> getByApplication(String applicationId) throws TException
    {
        return null;
    }

    @Override
    public List<Message> getByTitle(String applicationId, String title) throws TException
    {
        return null;
    }

    @Override
    public long getCountByApplication(String applicationId) throws TException
    {
        return 0;
    }
}
