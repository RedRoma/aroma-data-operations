package tech.aroma.data.sql.deserializers

import org.slf4j.LoggerFactory
import tech.aroma.data.sql.DatabaseDeserializer
import tech.aroma.data.sql.Tables
import tech.aroma.thrift.Message
import tech.aroma.thrift.Urgency
import java.sql.ResultSet

/**
 * Deserializes a [Message] from a JDBC [Row][ResultSet].
 *
 * @author SirWellington
 */
internal class MessageDeserializer : DatabaseDeserializer<Message>
{
    private val LOG = LoggerFactory.getLogger(this.javaClass)

    override fun deserialize(resultSet: ResultSet?): Message
    {
        val resultSet = resultSet ?: throw IllegalArgumentException()

        val message = Message()

        message.applicationId = resultSet.getString(Tables.Messages.APP_ID)
        message.messageId = resultSet.getString(Tables.Messages.MESSAGE_ID)
        message.applicationName = resultSet.getString(Tables.Messages.APP_NAME)
        message.title = resultSet.getString(Tables.Messages.TITLE)
        message.body = resultSet.getString(Tables.Messages.BODY)
        message.hostname = resultSet.getString(Tables.Messages.HOSTNAME)
        message.macAddress = resultSet.getString(Tables.Messages.IP_ADDRESS)
        message.deviceName = resultSet.getString(Tables.Messages.DEVICE_NAME)
        message.urgency = resultSet.getString(Tables.Messages.PRIORITY).asUrgency()
        message.timeOfCreation = resultSet.getTimestamp(Tables.Messages.TIME_CREATED).time
        message.timeMessageReceived = resultSet.getTimestamp(Tables.Messages.TIME_RECEIVED).time

        return message
    }

    private fun String.asUrgency(): Urgency?
    {
        return try
        {
            Urgency.valueOf(this)
        }
        catch (ex: Exception)
        {
            LOG.error("Could not parse Urgency $this", ex)
            return null
        }
    }
}