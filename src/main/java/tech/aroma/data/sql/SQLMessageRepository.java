package tech.aroma.data.sql;

import java.sql.ResultSet;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.apache.thrift.TException;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.data.MessageRepository;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.functions.TimeFunctions;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static tech.aroma.data.assertions.RequestAssertions.validLengthOfTime;
import static tech.aroma.data.assertions.RequestAssertions.validMessage;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * Created by sirwellington on 5/6/17.
 */
@Internal
final class SQLMessageRepository implements MessageRepository
{
    private JdbcTemplate database;
    private Function<ResultSet, Message> messageMapper;

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

        tryToSaveMessage(message, messageDuration);

    }

    private void tryToSaveMessage(Message message, Duration messageDuration)
    {
        UUID messageId = UUID.fromString(message.messageId);
        UUID appId = UUID.fromString(message.applicationId);


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
