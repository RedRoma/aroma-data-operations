package tech.aroma.data.sql;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import tech.aroma.data.MessageRepository;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.functions.TimeFunctions;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static tech.aroma.data.assertions.RequestAssertions.validLengthOfTime;
import static tech.aroma.data.assertions.RequestAssertions.validMessage;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * Saves and retrieves {@linkplain Message Messages} from the SQL Database.
 */
@Internal
final class SQLMessageRepository implements MessageRepository
{

    private static final Logger LOG = LoggerFactory.getLogger(SQLMessageRepository.class);

    private JdbcTemplate database;
    private RowMapper<Message> messageDeserializaer;
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
        return null;
    }

    @Override
    public void deleteMessage(String applicationId, String messageId) throws TException
    {

    }

    @Override
    public boolean containsMessage(String applicationId, String messageId) throws TException
    {
        return false;
    }

    @Override
    public List<Message> getByHostname(String hostname) throws TException
    {
        return null;
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
