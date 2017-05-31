package tech.aroma.data.sql

import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.springframework.jdbc.core.JdbcTemplate
import sir.wellington.alchemy.collections.lists.Lists
import tech.aroma.data.invalidArg
import tech.aroma.data.operationError
import tech.aroma.thrift.*
import tech.aroma.thrift.exceptions.DoesNotExistException
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.BooleanGenerators.booleans
import tech.sirwellington.alchemy.generator.CollectionGenerators.listOf
import tech.sirwellington.alchemy.generator.NumberGenerators.positiveLongs
import tech.sirwellington.alchemy.generator.ObjectGenerators.pojos
import tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@Repeat(50)
class SQLMessageRepositoryTest
{

    @Mock
    private lateinit var database: JdbcTemplate

    @Mock
    private lateinit var serializer: DatabaseSerializer<Message>

    private lateinit var instance: SQLMessageRepository

    @GeneratePojo
    private lateinit var message: Message

    @GenerateString(GenerateString.Type.UUID)
    private lateinit var appId: String

    @GenerateString(GenerateString.Type.UUID)
    private lateinit var messageId: String

    @GeneratePojo
    private lateinit var lifetime: LengthOfTime

    @GenerateString(GenerateString.Type.ALPHABETIC)
    private lateinit var alphabetic: String

    @Before
    @Throws(Exception::class)
    fun setUp()
    {
        instance = SQLMessageRepository(database, serializer)

        lifetime.unit = TimeUnit.SECONDS
        message.applicationId = appId
        message.messageId = messageId
    }

    @Test
    @Throws(Exception::class)
    fun testSaveMessage()
    {
        val expectedStatement = SQLStatements.Inserts.MESSAGE

        instance.saveMessage(message, lifetime)

        verify<DatabaseSerializer<Message>>(serializer).save(message, expectedStatement, database)
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testSaveMessageWithNoDuration()
    {
        val expectedStatement = SQLStatements.Inserts.MESSAGE

        instance.saveMessage(message, null)

        verify<DatabaseSerializer<Message>>(serializer).save(message, expectedStatement, database)
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testSaveMessageWithBadArguments()
    {
        assertThrows { instance.saveMessage(null) }
                .invalidArg()
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testSaveWhenSerializerFails()
    {
        doThrow(RuntimeException())
                .whenever(serializer)
                .save(any(), any(), any())

        assertThrows { instance.saveMessage(message) }
                .operationError()
    }


    @Test
    @Throws(Exception::class)
    fun testGetMessage()
    {
        val expectedQuery = SQLStatements.Queries.SELECT_MESSAGE

        whenever(database.queryForObject(expectedQuery, serializer, appId.toUUID(), messageId.toUUID()))
                .thenReturn(message)

        val result = instance.getMessage(appId, messageId)
        assertThat(result, `is`<Message>(message))
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetMessageWhenDatabaseFails()
    {
        val expectedQuery = SQLStatements.Queries.SELECT_MESSAGE

        whenever(database.queryForObject(expectedQuery, serializer, appId.toUUID(), messageId.toUUID()))
                .thenThrow(RuntimeException())

        assertThrows { instance.getMessage(appId, messageId) }
                .operationError()

    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetMessageWhenMessageDoesNotExist()
    {
        val expectedQuery = SQLStatements.Queries.SELECT_MESSAGE

        whenever(database.queryForObject(expectedQuery, serializer, appId.toUUID(), messageId.toUUID()))
                .thenReturn(null)

        assertThrows { instance.getMessage(appId, messageId) }
                .isInstanceOf(DoesNotExistException::class.java)
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetMessageWithBadArgs()
    {
        assertThrows { instance.getMessage(null, messageId) }.invalidArg()
        assertThrows { instance.getMessage(appId, null) }.invalidArg()

        assertThrows { instance.getMessage("", messageId) }.invalidArg()
        assertThrows { instance.getMessage(appId, "") }.invalidArg()

        assertThrows { instance.getMessage(alphabetic, messageId) }.invalidArg()
        assertThrows { instance.getMessage(appId, alphabetic) }.invalidArg()

    }

    @Test
    @Throws(Exception::class)
    fun testDeleteMessage()
    {
        val expectedStatement = SQLStatements.Deletes.MESSAGE

        instance.deleteMessage(appId, messageId)

        verify<JdbcTemplate>(database).update(expectedStatement, appId.toUUID(), messageId.toUUID())
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testDeleteMessageWithInvalidArgs()
    {
        val alphabetic = one(alphabeticString())

        assertThrows { instance.deleteMessage(alphabetic, messageId) }
                .invalidArg()

        assertThrows { instance.deleteMessage(appId, alphabetic) }
                .invalidArg()

        assertThrows { instance.deleteMessage("", messageId) }
                .invalidArg()

        assertThrows { instance.deleteMessage(alphabetic, "") }
                .invalidArg()

        assertThrows { instance.deleteMessage(null, messageId) }
                .invalidArg()

        assertThrows { instance.deleteMessage(appId, null) }
                .invalidArg()

    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testDeleteWhenOperationFails()
    {
        val expectedStatement = SQLStatements.Deletes.MESSAGE

        whenever(database.update(expectedStatement, appId.toUUID(), messageId.toUUID()))
                .thenThrow(RuntimeException())

        assertThrows { instance.deleteMessage(appId, messageId) }
                .operationError()

    }


    @Test
    @Throws(Exception::class)
    fun testContainsMessage()
    {
        val query = SQLStatements.Queries.CHECK_MESSAGE
        val expected = one(booleans())

        whenever(database.queryForObject(query, Boolean::class.java, appId.toUUID(), messageId.toUUID()))
                .thenReturn(expected)

        val result = instance.containsMessage(appId, messageId)
        assertThat(result, `is`(expected))
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testContainsMessageWhenOperationFails()
    {
        val query = SQLStatements.Queries.CHECK_MESSAGE

        whenever(database.update(eq(query), eq(Boolean::class.java), any(), any()))
                .thenThrow(RuntimeException())

        assertThrows { instance.containsMessage(appId, messageId) }
                .operationError()
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testContainsMessageWithBadArgs()
    {
        assertThrows { instance.containsMessage(null, messageId) }.invalidArg()
        assertThrows { instance.containsMessage(appId, null) }.invalidArg()

        assertThrows { instance.containsMessage("", messageId) }.invalidArg()
        assertThrows { instance.containsMessage(appId, "") }.invalidArg()

        assertThrows { instance.containsMessage(alphabetic, messageId) }.invalidArg()
        assertThrows { instance.containsMessage(appId, alphabetic) }.invalidArg()

    }


    @Test
    @Throws(Exception::class)
    fun testGetByHostname()
    {
        val query = SQLStatements.Queries.SELECT_MESSAGES_BY_HOSTNAME
        val hostname = alphabetic

        val messages = listOf(pojos(Message::class.java))

        whenever(database.query(query, serializer, hostname)).thenReturn(messages)

        val result = instance.getByHostname(hostname)

        assertThat(result, `is`(messages))
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetByHostnameWhenDatabaseFails()
    {
        val query = SQLStatements.Queries.SELECT_MESSAGES_BY_HOSTNAME
        val hostname = alphabetic

        whenever(database.query(query, serializer, hostname))
                .thenThrow(RuntimeException())

        assertThrows { instance.getByHostname(hostname) }
                .operationError()
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetByHostnameWithBadArgs()
    {
        assertThrows { instance.getByHostname(null) }.invalidArg()
        assertThrows { instance.getByHostname("") }.invalidArg()
    }

    @Test
    @Throws(Exception::class)
    fun testGetByApplication()
    {
        val query = SQLStatements.Queries.SELECT_MESSAGES_BY_APPLICATION
        val messages = listOf(pojos(Message::class.java))

        whenever(database.query(query, serializer, appId.toUUID()))
                .thenReturn(messages)

        val results = instance.getByApplication(appId)
        assertThat(results, `is`(messages))
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetByApplicationWhenNoMessages()
    {
        val query = SQLStatements.Queries.SELECT_MESSAGES_BY_APPLICATION

        whenever(database.query(query, serializer, appId.toUUID()))
                .thenReturn(Lists.emptyList<Message>())

        val results = instance.getByApplication(appId)
        assertThat(results, notNullValue())
        assertThat(results, `is`(empty<Message>()))
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetByApplicationWhenDatabaseFails()
    {
        val query = SQLStatements.Queries.SELECT_MESSAGES_BY_APPLICATION

        whenever(database.query(query, serializer, appId.toUUID()))
                .thenThrow(RuntimeException())

        assertThrows { instance.getByApplication(appId) }
                .operationError()
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetByApplicationWithBadArgs()
    {
        assertThrows { instance.getByApplication(null) }.invalidArg()
        assertThrows { instance.getByApplication("") }.invalidArg()
        assertThrows { instance.getByApplication(alphabetic) }.invalidArg()
    }

    @Test
    @Throws(Exception::class)
    fun testGetByTitle()
    {
        val query = SQLStatements.Queries.SELECT_MESSAGES_BY_TITLE
        val title = alphabetic
        val messages = listOf(pojos(Message::class.java))

        whenever(database.query(query, serializer, appId.toUUID(), title))
                .thenReturn(messages)

        val results = instance.getByTitle(appId, title)
        assertThat(results, `is`(messages))
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetByTitleWhenNoMessages()
    {
        val query = SQLStatements.Queries.SELECT_MESSAGES_BY_TITLE
        val title = alphabetic

        whenever(database.query(query, serializer, appId.toUUID(), title))
                .thenReturn(Lists.emptyList())

        val results = instance.getByTitle(appId, title)
        assertThat(results, notNullValue())
        assertThat(results, `is`(empty<Message>()))
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetByTitleWhenDatabaseFails()
    {
        val query = SQLStatements.Queries.SELECT_MESSAGES_BY_TITLE
        val title = alphabetic

        whenever(database.query(query, serializer, appId.toUUID(), title))
                .thenThrow(RuntimeException())

        assertThrows { instance.getByTitle(appId, title) }
                .operationError()
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetByTitleWithBadArgs()
    {
        assertThrows { instance.getByTitle("", alphabetic) }.invalidArg()
        assertThrows { instance.getByTitle(appId, "") }.invalidArg()

        assertThrows { instance.getByTitle(null, alphabetic) }.invalidArg()
        assertThrows { instance.getByTitle(appId, null) }.invalidArg()

        assertThrows { instance.getByTitle(alphabetic, alphabetic) }.invalidArg()
    }

    @Test
    @Throws(Exception::class)
    fun testGetCountByApplication()
    {
        val query = SQLStatements.Queries.COUNT_MESSAGES
        val count = one(positiveLongs())

        whenever(database.queryForObject(query, Long::class.java, appId.toUUID()))
                .thenReturn(count)

        val result = instance.getCountByApplication(appId)
        assertThat(result, `is`(count))
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetCountWhenDatabaseFails()
    {
        val query = SQLStatements.Queries.COUNT_MESSAGES

        whenever(database.queryForObject(query, Long::class.java, appId.toUUID()))
                .thenThrow(RuntimeException())

        assertThrows { instance.getCountByApplication(appId) }
                .operationError()
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testGetCountWithBadArgs()
    {
        assertThrows { instance.getCountByApplication("") }
                .invalidArg()

        assertThrows { instance.getCountByApplication(null) }
                .invalidArg()

        assertThrows { instance.getCountByApplication(alphabetic) }
                .invalidArg()
    }
}

