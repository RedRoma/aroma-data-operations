package tech.aroma.data.sql.serializers

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import tech.aroma.data.assertions.RequestAssertions.validMessage
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.thrift.Message
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * Serializes [Messages][Message] to the Database.
 *
 * @author SirWellington
 */
class MessageSerializer : DatabaseSerializer<Message>
{
    private val LOG = LoggerFactory.getLogger(this.javaClass)

    @Throws(SQLException::class)
    override fun save(message: Message, timeToLive: Duration?, statement: String, database: JdbcTemplate)
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

        val appId = UUID.fromString(message.applicationId)
        val messageId = UUID.fromString(message.messageId)

        database.update(statement,
                        messageId,
                        message.title,
                        message.body,
                        message.urgency.toString(),
                        Timestamp.from(Instant.ofEpochMilli(message.timeOfCreation)),
                        Timestamp.from(Instant.ofEpochMilli(message.timeMessageReceived)),
                        expiration,
                        message.hostname,
                        message.macAddress,
                        appId,
                        message.applicationName,
                        message.deviceName)
    }

    override fun deserialize(resultSet: ResultSet?): Message?
    {
        return Message()
    }


}
