package tech.aroma.data.sql.serializers

import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.Mockito.verify
import org.springframework.jdbc.core.JdbcTemplate
import tech.aroma.data.sql.toTimestamp
import tech.aroma.data.sql.toUUID
import tech.aroma.thrift.Message
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import java.sql.ResultSet
import org.hamcrest.Matchers.`is` as Is

/**
 * @author SirWellington
 */

@RunWith(AlchemyTestRunner::class)
@Repeat(100)
class MessageSerializerTest
{
    @Mock
    private lateinit var database: JdbcTemplate

    @Mock
    private lateinit var resultSet: ResultSet

    @GenerateString
    private lateinit var sql: String

    @GeneratePojo
    private lateinit var message: Message

    @GenerateString(GenerateString.Type.UUID)
    private lateinit var appId: String

    @GenerateString(GenerateString.Type.UUID)
    private lateinit var messageId: String

    @GenerateString(GenerateString.Type.ALPHABETIC)
    private lateinit var alphabetic: String

    private lateinit var instance: MessageSerializer

    @Captor
    private lateinit var captor: ArgumentCaptor<Any>

    @Before
    fun setup()
    {
        message.messageId = messageId
        message.applicationId = appId

        message.unsetIsTruncated()
        setupResults()

        instance = MessageSerializer()
    }

    @Test
    @Throws(Exception::class)
    fun testSave()
    {
        instance.save(message, sql, database)

        verify(database).update(sql,
                                messageId.toUUID(),
                                appId.toUUID(),
                                message.applicationName,
                                message.title,
                                message.body,
                                message.urgency.toString(),
                                message.timeOfCreation.toTimestamp(),
                                message.timeMessageReceived.toTimestamp(),
                                message.hostname,
                                message.macAddress,
                                message.deviceName)
    }


    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testWhenDatabaseFails()
    {
        whenever(database.update(eq(sql), Mockito.anyVararg<Any>()))
                .thenThrow(RuntimeException())

        assertThrows { instance.save(message, sql, database) }
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testSaveWithInvalidIDs()
    {
        message.messageId = alphabetic

        assertThrows {
            instance.save(message, sql, database)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testSaveWithBadArgs()
    {
        val statement = one(alphabeticString())

        assertThrows {
            instance.save(message, "", database)
        }.isInstanceOf(IllegalArgumentException::class.java)

        message.applicationId = statement
        message.messageId = statement

        assertThrows {
            instance.save(message, statement, database)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testDeserialize()
    {
        val result = instance.deserialize(resultSet)

        Assert.assertThat(result, org.hamcrest.Matchers.`is`(message))
    }

    @DontRepeat
    @Test
    fun testDeserializeWithBadArgs()
    {
        assertThrows {
            instance.deserialize(null)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    private fun setupResults()
    {
        whenever(resultSet.getString(Tables.Messages.MESSAGE_ID)).thenReturn(message.messageId)
        whenever(resultSet.getString(Tables.Messages.APP_ID)).thenReturn(message.applicationId)
        whenever(resultSet.getString(Tables.Messages.APP_NAME)).thenReturn(message.applicationName)
        whenever(resultSet.getString(Tables.Messages.TITLE)).thenReturn(message.title)
        whenever(resultSet.getString(Tables.Messages.BODY)).thenReturn(message.body)
        whenever(resultSet.getString(Tables.Messages.HOSTNAME)).thenReturn(message.hostname)
        whenever(resultSet.getString(Tables.Messages.IP_ADDRESS)).thenReturn(message.macAddress)
        whenever(resultSet.getString(Tables.Messages.DEVICE_NAME)).thenReturn(message.deviceName)
        whenever(resultSet.getString(Tables.Messages.PRIORITY)).thenReturn(message.urgency.toString())

        whenever(resultSet.getTimestamp(Tables.Messages.TIME_CREATED)).thenReturn(message.timeOfCreation.toTimestamp())
        whenever(resultSet.getTimestamp(Tables.Messages.TIME_RECEIVED)).thenReturn(message.timeMessageReceived.toTimestamp())
    }

//    private fun checkMessageWithDuration(message: Message)
//    {
//        verify(database).update(eq(sql), captor.capture())
//
//        val arguments = captor.allValues
//
//        assertThat(arguments[0], Is<Any>(UUID.fromString(messageId)))
//        assertThat(arguments[1], Is<Any>(message.title))
//        assertThat(arguments[2], Is<Any>(message.body))
//        assertThat(arguments[3], Is<Any>(message.urgency.toString()))
//        assertThat(arguments[4], Is<Any>(message.timeOfCreation.toTimestamp()))
//        assertThat(arguments[5], Is<Any>(message.timeMessageReceived.toTimestamp()))
//
//        assertThat(arguments[6], Is<Any>(message.hostname))
//        assertThat(arguments[7], Is<Any>(message.macAddress))
//        assertThat(arguments[8], Is<Any>(UUID.fromString(appId)))
//        assertThat(arguments[9], Is<Any>(message.applicationName))
//        assertThat(arguments[10], Is<Any>(message.deviceName))
//    }

}