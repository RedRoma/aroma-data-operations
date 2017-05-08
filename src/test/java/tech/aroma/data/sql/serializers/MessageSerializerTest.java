package tech.aroma.data.sql.serializers; /**
 * @author SirWellington
 */

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.thrift.Message;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.equalToInstantWithinDelta;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.inTheFuture;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

@RunWith(AlchemyTestRunner.class)
@Repeat(100)
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

    @GenerateString(GenerateString.Type.ALPHABETIC)
    private String alphabetic;

    private MessageSerializer instance;

    @Captor
    private ArgumentCaptor<Object> captor;

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
    public void testSaveWithInvalidIDs() throws Exception
    {
        message.messageId = alphabetic;

        assertThrows(() -> instance.save(message, null, statement, database))
                .isInstanceOf(IllegalArgumentException.class);
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

        assertThrows(() -> instance.save(message, null, "", database))
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
        verify(database).update(eq(statement), captor.capture());

        List<Object> arguments = captor.getAllValues();

        assertThat(arguments.get(0), is(UUID.fromString(messageId)));
        assertThat(arguments.get(1), is(message.title));
        assertThat(arguments.get(2), is(message.body));
        assertThat(arguments.get(3), is(message.urgency.toString()));
        assertThat(arguments.get(4), is(epochToTimestamp(message.timeOfCreation)));
        assertThat(arguments.get(5), is(epochToTimestamp(message.timeMessageReceived)));

        Object expiration = arguments.get(6);
        if (Objects.nonNull(ttl))
        {
            checkExpirationWithTTL(expiration, ttl);
        }

        assertThat(arguments.get(7), is(message.hostname));
        assertThat(arguments.get(8), is(message.macAddress));
        assertThat(arguments.get(9), is(UUID.fromString(appId)));
        assertThat(arguments.get(10), is(message.applicationName));
        assertThat(arguments.get(11), is(message.deviceName));
    }

    private void checkExpirationWithTTL(Object expiration, Duration ttl)
    {
        assertThat(expiration, notNullValue());
        assertThat(expiration, instanceOf(Timestamp.class));

        Instant actualExpiration = ((Timestamp) expiration).toInstant();
        Instant expectedExpiration = Instant.now().plus(ttl);

        long acceptableDelta = 100;

        checkThat(actualExpiration)
                .is(inTheFuture())
                .is(equalToInstantWithinDelta(expectedExpiration, acceptableDelta));
    }

    public Timestamp epochToTimestamp(long epochMilli)
    {
        Instant instant = Instant.ofEpochMilli(epochMilli);
        return Timestamp.from(instant);
    }
}