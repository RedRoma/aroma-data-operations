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

    companion object
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

    override fun deserialize(row: ResultSet?): Message
    {
        val row = row ?: throw IllegalArgumentException()

        val message = Message()

        message.applicationId = row.getString(Tables.Messages.APP_ID)
        message.messageId = row.getString(Tables.Messages.MESSAGE_ID)
        message.applicationName = row.getString(Tables.Messages.APP_NAME)
        message.title = row.getString(Tables.Messages.TITLE)
        message.body = row.getString(Tables.Messages.BODY)
        message.hostname = row.getString(Tables.Messages.HOSTNAME)

        if (row.hasColumn(Tables.Messages.IP_ADDRESS))
        {
            message.macAddress = row.getString(Tables.Messages.IP_ADDRESS)
        }

        message.deviceName = row.getString(Tables.Messages.DEVICE_NAME)
        message.urgency = row.getString(Tables.Messages.PRIORITY).asUrgency()
        message.timeOfCreation = row.getTimestamp(Tables.Messages.TIME_CREATED)?.time ?: 0L
        message.timeMessageReceived = row.getTimestamp(Tables.Messages.TIME_RECEIVED)?.time ?: 0L

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
