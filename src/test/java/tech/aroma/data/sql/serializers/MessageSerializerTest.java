package tech.aroma.data.sql.serializers; /**
 * @author SirWellington
 */

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.thrift.Message;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

@RunWith(AlchemyTestRunner.class)
public class MessageSerializerTest
{
    @Mock
    private JdbcTemplate database;

    @GenerateString
    private String statement;

    @GeneratePojo
    private Message message;

    @GenerateString(GenerateString.Type.UUID)
    private String appId;

    @GenerateString(GenerateString.Type.UUID)
    private String messageId;

    private MessageSerializer instance;

    @Before
    public void setup()
    {
        message.messageId = messageId;
        message.applicationId = appId;

        instance = new MessageSerializer();
    }

    @Test
    public void testSave() throws Exception
    {
        instance.save(message, null, statement, database);
        checkMessageWithDuration(message, null);
    }

    @Ignore
    @Test
    public void testSaveWithExpiration() throws Exception
    {
        int daysToLive = one(integers(3, 10));
        Duration ttl = Duration.ofDays(daysToLive);

        instance.save(message, ttl, statement, database);

        checkMessageWithDuration(message, ttl);
    }

    @DontRepeat
    @Test
    public void testWhenDatabaseFails() throws Exception
    {
        when(database.update(anyString(), Matchers.<Object>anyVararg()))
                .thenThrow(new RuntimeException());

        assertThrows(() -> instance.save(message, null, statement, database));
    }

    @DontRepeat
    @Test
    public void testSaveWithBadArgs() throws Exception
    {
        String statement = one(alphabeticString());

        assertThrows(() -> instance.save(null, null, statement, database))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.save(message, null, null, database))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.save(message, null, statement, null))
                .isInstanceOf(IllegalArgumentException.class);

        message.applicationId = statement;
        message.messageId = statement;
        assertThrows(() -> instance.save(message, null, statement, database))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void checkMessageWithDuration(Message message, Duration ttl)
    {

        Timestamp expiration = null;

        if (Objects.nonNull(ttl))
        {
            expiration = Timestamp.from(Instant.now().plus(ttl));
        }


        Instant iTimeCreated = Instant.ofEpochMilli(message.timeOfCreation);
        Instant iTimeReceived = Instant.ofEpochMilli(message.timeMessageReceived);

        verify(database).update(statement,
                                UUID.fromString(messageId),
                                message.title,
                                message.body,
                                message.urgency.toString(),
                                Timestamp.from(iTimeCreated),
                                Timestamp.from(iTimeReceived),
                                expiration,
                                message.hostname,
                                message.macAddress,
                                UUID.fromString(appId),
                                message.applicationName,
                                message.deviceName);
    }
}