package tech.aroma.data.sql;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import sun.security.util.Length;
import tech.aroma.thrift.*;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.functions.TimeFunctions;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat(50)
public class SQLMessageRepositoryTest
{

    @Mock
    private JdbcTemplate database;

    @Mock
    private DatabaseDeserializer<Message> messageDeserializer;

    @Mock
    private DatabaseSerializer<Message> messageSerializer;

    private SQLMessageRepository instance;

    @GeneratePojo
    private Message message;

    @GenerateString(GenerateString.Type.UUID)
    private String appId;

    @GenerateString(GenerateString.Type.UUID)
    private String messageId;

    @GeneratePojo
    private LengthOfTime lifetime;

    @Before
    public void setUp() throws Exception
    {
        instance = new SQLMessageRepository(database, messageDeserializer, messageSerializer);

        lifetime.unit = TimeUnit.SECONDS;
        message.applicationId = appId;
        message.messageId = messageId;
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new SQLMessageRepository(null, messageDeserializer, messageSerializer));
        assertThrows(() -> new SQLMessageRepository(database, null, messageSerializer));
        assertThrows(() -> new SQLMessageRepository(database, messageDeserializer, null));
    }


    @Test
    public void testSaveMessage() throws Exception
    {

        Duration duration = TimeFunctions.lengthOfTimeToDuration().apply(lifetime);
        instance.saveMessage(message, lifetime);

        String expectedStatement = SQLStatements.Inserts.MESSAGE;

        verify(messageSerializer).save(message, duration, expectedStatement, database);
    }

    @DontRepeat
    @Test
    public void testSaveMessageWithNoDuration() throws Exception
    {
        instance.saveMessage(message, null);

        String expectedStatement = SQLStatements.Inserts.MESSAGE;

        verify(messageSerializer).save(message, null, expectedStatement, database);
    }

    @DontRepeat
    @Test
    public void testSaveMessageWithBadArguments() throws Exception
    {
        assertThrows(() -> instance.saveMessage(null))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testSaveWhenSerializerFails() throws Exception
    {
        doThrow(new RuntimeException())
                .when(messageSerializer)
                .save(any(), any(), any(), any());

        assertThrows(() -> instance.saveMessage(message))
                .isInstanceOf(OperationFailedException.class);
    }
}