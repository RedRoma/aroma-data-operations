package tech.aroma.data.sql;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.MessageRepository;
import tech.aroma.thrift.*;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.functions.TimeFunctions;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

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

    @GenerateString(GenerateString.Type.ALPHABETIC)
    private String alphabetic;

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


    @Test
    public void testGetMessage() throws Exception
    {
        String expectedQuery = SQLStatements.Queries.SELECT_MESSAGE;
        when(database.queryForObject(expectedQuery, messageDeserializer, UUID.fromString(appId), UUID.fromString(messageId)))
                .thenReturn(message);

        Message result = instance.getMessage(appId, messageId);
        assertThat(result, is(message));
    }

    @DontRepeat
    @Test
    public void testGetMessageWhenDatabaseFails() throws Exception
    {
        String expectedQuery = SQLStatements.Queries.SELECT_MESSAGE;

        when(database.queryForObject(expectedQuery, messageDeserializer, UUID.fromString(appId), UUID.fromString(messageId)))
                .thenThrow(new RuntimeException());

        assertThrows(() -> instance.getMessage(appId, messageId))
                .isInstanceOf(OperationFailedException.class);

    }

    @DontRepeat
    @Test
    public void testGetMessageWhenMessageDoesNotExist() throws Exception
    {
        String expectedQuery = SQLStatements.Queries.SELECT_MESSAGE;
        when(database.queryForObject(expectedQuery, messageDeserializer, UUID.fromString(appId), UUID.fromString(messageId)))
                .thenReturn(null);

        assertThrows(() -> instance.getMessage(appId, messageId))
                .isInstanceOf(DoesNotExistException.class);
    }

    @DontRepeat
    @Test
    public void testGetMessageWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getMessage(null, messageId)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.getMessage(appId, null)).isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getMessage("", messageId)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.getMessage(appId, "")).isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getMessage(alphabetic, messageId)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.getMessage(appId, alphabetic)).isInstanceOf(InvalidArgumentException.class);

    }

    @Test
    public void testDeleteMessage() throws Exception
    {
        instance.deleteMessage(appId, messageId);

        String expectedStatement = SQLStatements.Deletes.MESSAGE;

        verify(database).update(expectedStatement, UUID.fromString(appId), UUID.fromString(messageId));
    }

    @DontRepeat
    @Test
    public void testDeleteMessageWithInvalidArgs() throws Exception
    {
        String alphabetic = one(alphabeticString());

        assertThrows(() -> instance.deleteMessage(alphabetic, messageId))
                .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.deleteMessage(appId, alphabetic))
                .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.deleteMessage("", messageId))
                .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.deleteMessage(alphabetic, ""))
                .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.deleteMessage(null, messageId))
                .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.deleteMessage(appId, null))
                .isInstanceOf(InvalidArgumentException.class);

    }

    @DontRepeat
    @Test
    public void testDeleteWhenOperationFails() throws Exception
    {

        String expectedStatement = SQLStatements.Deletes.MESSAGE;
        when(database.update(expectedStatement, UUID.fromString(appId), UUID.fromString(messageId)))
                .thenThrow(new RuntimeException());

        assertThrows(() -> instance.deleteMessage(appId, messageId))
                .isInstanceOf(OperationFailedException.class);

    }


    @Test
    public void testContainsMessage() throws Exception
    {
        boolean expected = one(booleans());

        String query = SQLStatements.Queries.CHECK_MESSAGE;
        when(database.queryForObject(query, Boolean.class, UUID.fromString(appId), UUID.fromString(messageId)))
                .thenReturn(expected);

        boolean result = instance.containsMessage(appId, messageId);
        assertThat(result, is(expected));
    }

    @DontRepeat
    @Test
    public void testContainsMessageWhenOperationFails() throws Exception
    {
        String query = SQLStatements.Queries.CHECK_MESSAGE;
        when(database.update(eq(query), eq(Boolean.class), any(), any()))
                .thenThrow(new RuntimeException());

        assertThrows(() -> instance.containsMessage(appId, messageId))
                .isInstanceOf(OperationFailedException.class);
    }

    @DontRepeat
    @Test
    public void testContainsMessageWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.containsMessage(null, messageId)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.containsMessage(appId, null)).isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.containsMessage("", messageId)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.containsMessage(appId, "")).isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.containsMessage(alphabetic, messageId)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.containsMessage(appId, alphabetic)).isInstanceOf(InvalidArgumentException.class);

    }


    @Test
    public void testGetByHostname() throws Exception
    {
        String hostname = alphabetic;
        String query = SQLStatements.Queries.SELECT_MESSAGES_BY_HOSTNAME;

        List<Message> messages = listOf(pojos(Message.class));

        when(database.query(query, messageDeserializer, hostname)).thenReturn(messages);

        List<Message> result = instance.getByHostname(hostname);

        assertThat(result, is(messages));
    }

    @DontRepeat
    @Test
    public void testGetByHostnameWhenDatabaseFails() throws Exception
    {
        String hostname = alphabetic;
        String query = SQLStatements.Queries.SELECT_MESSAGES_BY_HOSTNAME;

        when(database.query(query, messageDeserializer, hostname))
                .thenThrow(new RuntimeException());

        assertThrows(() -> instance.getByHostname(hostname))
                .isInstanceOf(OperationFailedException.class);
    }

    @DontRepeat
    @Test
    public void testGetByHostnameWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getByHostname(null)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.getByHostname("")).isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetByApplication() throws Exception
    {
        List<Message> messages = listOf(pojos(Message.class));
        String query = SQLStatements.Queries.SELECT_MESSAGES_BY_APPLICATION;

        when(database.query(query, messageDeserializer, UUID.fromString(appId)))
                .thenReturn(messages);

        List<Message> results = instance.getByApplication(appId);
        assertThat(results, is(messages));
    }

    @DontRepeat
    @Test
    public void testGetByApplicationWhenNoMessages() throws Exception
    {
        String query = SQLStatements.Queries.SELECT_MESSAGES_BY_APPLICATION;

        when(database.query(query, messageDeserializer, UUID.fromString(appId)))
                .thenReturn(Lists.emptyList());

        List<Message> results = instance.getByApplication(appId);
        assertThat(results, notNullValue());
        assertThat(results, is(empty()));
    }

    @DontRepeat
    @Test
    public void testGetByApplicationWhenDatabaseFails() throws Exception
    {
        String query = SQLStatements.Queries.SELECT_MESSAGES_BY_APPLICATION;

        when(database.query(query, messageDeserializer, UUID.fromString(appId)))
                .thenThrow(new RuntimeException());

        assertThrows(() -> instance.getByApplication(appId))
                .isInstanceOf(OperationFailedException.class);
    }

    @DontRepeat
    @Test
    public void testGetByApplicationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getByApplication(null)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.getByApplication("")).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.getByApplication(alphabetic)).isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetByTitle() throws Exception
    {
        String query = SQLStatements.Queries.SELECT_MESSAGES_BY_TITLE;
        String title = alphabetic;
        List<Message> messages = listOf(pojos(Message.class));

        when(database.query(query, messageDeserializer, UUID.fromString(appId), title))
                .thenReturn(messages);

        List<Message> results = instance.getByTitle(appId, title);
        assertThat(results, is(messages));
    }

    @DontRepeat
    @Test
    public void testGetByTitleWhenNoMessages() throws Exception
    {
        String query = SQLStatements.Queries.SELECT_MESSAGES_BY_TITLE;
        String title = alphabetic;

        when(database.query(query, messageDeserializer, UUID.fromString(appId), title))
                .thenReturn(Lists.emptyList());

        List<Message> results = instance.getByTitle(appId, title);
        assertThat(results, notNullValue());
        assertThat(results, is(empty()));
    }

    @DontRepeat
    @Test
    public void testGetByTitleWhenDatabaseFails() throws Exception
    {
        String query = SQLStatements.Queries.SELECT_MESSAGES_BY_TITLE;
        String title = alphabetic;

        when(database.query(query, messageDeserializer, UUID.fromString(appId), title))
                .thenThrow(new RuntimeException());

        assertThrows(() -> instance.getByTitle(appId, title))
                .isInstanceOf(OperationFailedException.class);
    }

    @DontRepeat
    @Test
    public void testGetByTitleWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getByTitle("", alphabetic)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.getByTitle(appId, "")).isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getByTitle(null, alphabetic)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.getByTitle(appId, null)).isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getByTitle(alphabetic, alphabetic)).isInstanceOf(InvalidArgumentException.class);
    }
}

