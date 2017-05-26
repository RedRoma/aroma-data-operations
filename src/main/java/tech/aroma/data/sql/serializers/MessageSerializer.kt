package tech.aroma.data.sql.serializers

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.assertions.RequestAssertions.validMessage
import tech.aroma.data.sql.*
import tech.aroma.thrift.Message
import tech.aroma.thrift.Urgency
import tech.aroma.thrift.message.service.MessageServiceConstants
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Serializes [Messages][Message] to the Database.
 *
 * @author SirWellington
 */
internal class MessageSerializer : DatabaseSerializer<Message>
{

    private companion object
    {
        @JvmStatic val LOG = LoggerFactory.getLogger(MessageSerializer::class.java)

        @JvmStatic val DEFAULT_TTL = MessageServiceConstants.DEFAULT_MESSAGE_LIFETIME
    }

    @Throws(SQLException::class)
    override fun save(message: Message, statement: String, database: JdbcOperations)
    {
        checkThat(message).`is`(validMessage())
        checkThat(database).`is`(notNull())
        checkThat(statement).`is`(nonEmptyString())

        val timeCreated = if (message.timeOfCreation > 0) message.timeOfCreation.toTimestamp() else null
        val timeReceived = if (message.timeMessageReceived > 0) message.timeMessageReceived.toTimestamp() else null

        database.update(statement,
                        message.messageId.toUUID(),
                        message.applicationId.toUUID(),
                        message.applicationName,
                        message.title,
                        message.body,
                        message.urgency?.toString() ?: Urgency.MEDIUM.toString(),
                        timeCreated,
                        timeReceived,
                        message.hostname,
                        message.macAddress,
                        message.deviceName)

    }

    override fun deserialize(row: ResultSet): Message
    {
        val message = Message()

        message.applicationId = row.getString(Columns.Messages.APP_ID)
        message.messageId = row.getString(Columns.Messages.MESSAGE_ID)
        message.applicationName = row.getString(Columns.Messages.APP_NAME)
        message.title = row.getString(Columns.Messages.TITLE)
        message.body = row.getString(Columns.Messages.BODY)
        message.hostname = row.getString(Columns.Messages.HOSTNAME)

        if (row.hasColumn(Columns.Messages.IP_ADDRESS))
        {
            message.macAddress = row.getString(Columns.Messages.IP_ADDRESS)
        }

        message.deviceName = row.getString(Columns.Messages.DEVICE_NAME)
        message.urgency = row.getString(Columns.Messages.PRIORITY).asUrgency()
        message.timeOfCreation = row.getTimestamp(Columns.Messages.TIME_CREATED)?.time ?: 0L
        message.timeMessageReceived = row.getTimestamp(Columns.Messages.TIME_RECEIVED)?.time ?: 0L

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
