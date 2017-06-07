package tech.aroma.data.sql.serializers

import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.Matchers
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.springframework.jdbc.core.JdbcTemplate
import tech.aroma.data.sql.toTimestamp
import tech.aroma.data.sql.toUUID
import tech.aroma.thrift.Message
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.one
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.Repeat
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
        val statement = one(alphabeticStrings())

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

        assertThat(result, Matchers.equalTo(message))
    }

    private fun setupResults()
    {
        whenever(resultSet.getString(Columns.Messages.MESSAGE_ID)).thenReturn(message.messageId)
        whenever(resultSet.getString(Columns.Messages.APP_ID)).thenReturn(message.applicationId)
        whenever(resultSet.getString(Columns.Messages.APP_NAME)).thenReturn(message.applicationName)
        whenever(resultSet.getString(Columns.Messages.TITLE)).thenReturn(message.title)
        whenever(resultSet.getString(Columns.Messages.BODY)).thenReturn(message.body)
        whenever(resultSet.getString(Columns.Messages.HOSTNAME)).thenReturn(message.hostname)
        whenever(resultSet.getString(Columns.Messages.IP_ADDRESS)).thenReturn(message.macAddress)
        whenever(resultSet.getString(Columns.Messages.DEVICE_NAME)).thenReturn(message.deviceName)
        whenever(resultSet.getString(Columns.Messages.PRIORITY)).thenReturn(message.urgency.toString())

        whenever(resultSet.getTimestamp(Columns.Messages.TIME_CREATED)).thenReturn(message.timeOfCreation.toTimestamp())
        whenever(resultSet.getTimestamp(Columns.Messages.TIME_RECEIVED)).thenReturn(message.timeMessageReceived.toTimestamp())
    }

}