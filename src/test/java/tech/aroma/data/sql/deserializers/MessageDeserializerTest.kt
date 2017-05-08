package tech.aroma.data.sql.deserializers

/**
 * @author SirWellington
 */

import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.Matchers.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import tech.aroma.data.sql.Tables
import tech.aroma.data.sql.toTimestamp
import tech.aroma.thrift.Message
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.sql.ResultSet

@RunWith(AlchemyTestRunner::class)
@Repeat
class MessageDeserializerTest
{

    private lateinit var instance: MessageDeserializer

    @GeneratePojo
    private lateinit var message: Message

    @Mock
    private lateinit var resultSet: ResultSet

    @Before
    fun setup()
    {
        instance = MessageDeserializer()

        message.unsetIsTruncated()
        setupResults()
    }

    @Test
    fun testDeserialize()
    {
        val result = instance.deserialize(resultSet)

        Assert.assertThat(result, `is`(message))
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
}
