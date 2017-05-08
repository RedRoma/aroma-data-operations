package tech.aroma.data.sql;

import java.time.Instant;

import org.apache.thrift.TException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.thrift.Message;
import tech.sirwellington.alchemy.test.junit.runners.*;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class SQLMessageRepositoryIT
{
    private static JdbcTemplate database;

    private SQLMessageRepository instance;

    @GeneratePojo
    private Message message;

    @GenerateString(GenerateString.Type.UUID)
    private String appId;

    @GenerateString(GenerateString.Type.UUID)
    private String messageId;

    private DatabaseSerializer<Message> serializer = Resources.getMessageSerializer();

    @BeforeClass
    public static void setUp() throws Exception
    {
        database = Resources.connectToDatabase();
    }

    @Before
    public void setup()
    {
        message.applicationId = appId;
        message.messageId = messageId;
        message.timeMessageReceived = Instant.now().toEpochMilli();
        message.timeOfCreation = Instant.now().toEpochMilli();

        instance = new SQLMessageRepository(database, serializer);
    }

    @After
    public void tearDown() throws TException
    {
        instance.deleteMessage(appId, messageId);
    }

    @Test
    public void saveMessage() throws Exception
    {
        instance.saveMessage(message);
    }

    @Test
    public void getMessage() throws Exception
    {

    }

    @Test
    public void deleteMessage() throws Exception
    {
    }

    @Test
    public void containsMessage() throws Exception
    {
    }

    @Test
    public void getByHostname() throws Exception
    {
    }

    @Test
    public void getByApplication() throws Exception
    {
    }

    @Test
    public void getByTitle() throws Exception
    {
    }

    @Test
    public void getCountByApplication() throws Exception
    {
    }

}