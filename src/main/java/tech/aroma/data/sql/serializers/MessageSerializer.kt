package tech.aroma.data.sql.serializers

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.assertions.RequestAssertions.validMessage
import tech.aroma.data.sql.*
import tech.aroma.thrift.Message
import tech.aroma.thrift.Urgency
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import java.sql.*
import java.time.Duration
import java.time.Instant
import java.util.*

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
    }

    @Throws(SQLException::class)
    override fun save(message: Message, timeToLive: Duration?, statement: String, database: JdbcOperations)
    {
        checkThat(message).`is`(validMessage())
        checkThat(database).`is`(notNull())
        checkThat(statement).`is`(nonEmptyString())

        var expiration: Timestamp? = null

        if (Objects.nonNull(timeToLive))
        {
            val instanceOfExpiration = Instant.now().plus(timeToLive)
            expiration = Timestamp.from(instanceOfExpiration)
        }

        database.update(statement,
                        message.messageId.toUUID(),
                        message.title,
                        message.body,
                        message.urgency.toString(),
                        message.timeOfCreation.toTimestamp(),
                        message.timeMessageReceived.toTimestamp(),
                        expiration,
                        message.hostname,
                        message.macAddress,
                        message.applicationId.toUUID(),
                        message.applicationName,
                        message.deviceName)

    }

    override fun deserialize(resultSet: ResultSet?): Message
    {
        val row = resultSet ?: throw IllegalArgumentException()

        val message = Message()

        message.applicationId = row.getString(Tables.Messages.APP_ID)
        message.messageId = row.getString(Tables.Messages.MESSAGE_ID)
        message.applicationName = row.getString(Tables.Messages.APP_NAME)
        message.title = row.getString(Tables.Messages.TITLE)
        message.body = row.getString(Tables.Messages.BODY)
        message.hostname = row.getString(Tables.Messages.HOSTNAME)
        message.macAddress = row.getString(Tables.Messages.IP_ADDRESS)
        message.deviceName = row.getString(Tables.Messages.DEVICE_NAME)
        message.urgency = row.getString(Tables.Messages.PRIORITY).asUrgency()
        message.timeOfCreation = row.getTimestamp(Tables.Messages.TIME_CREATED).time
        message.timeMessageReceived = row.getTimestamp(Tables.Messages.TIME_RECEIVED).time

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
