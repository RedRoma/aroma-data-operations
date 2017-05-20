package tech.aroma.data.sql

import org.apache.thrift.TException
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import tech.aroma.data.MessageRepository
import tech.aroma.data.assertions.RequestAssertions.validLengthOfTime
import tech.aroma.data.assertions.RequestAssertions.validMessage
import tech.aroma.thrift.LengthOfTime
import tech.aroma.thrift.Message
import tech.aroma.thrift.exceptions.*
import tech.aroma.thrift.functions.TimeFunctions
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.arguments.Optional
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID
import java.time.Duration
import java.util.*
import javax.inject.Inject

/**
 * Saves and retrieves [Messages][Message] from the SQL Database.
 */
@Internal
internal class SQLMessageRepository
@Inject
constructor(private val database: JdbcTemplate, private val serializer: DatabaseSerializer<Message>) : MessageRepository
{

    @Throws(TException::class)
    override fun saveMessage(message: Message?, @Optional lifetime: LengthOfTime?)
    {
        checkThat(message)
                .throwing(InvalidArgumentException::class.java)
                .`is`(notNull<Message>())
                .`is`(validMessage())

        _saveMessage(message!!)
    }

    @Throws(OperationFailedException::class)
    private fun _saveMessage(message: Message)
    {
        val statement = SQLStatements.Inserts.MESSAGE

        try
        {
            serializer.save(message, statement, database)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to serialize Message {} using statement [{}]", message, statement, ex)
            throw OperationFailedException(ex.message)
        }

    }

    @Throws(TException::class)
    override fun getMessage(applicationId: String?, messageId: String?): Message
    {
        checkThat(applicationId, messageId)
                .throwing(InvalidArgumentException::class.java)
                .are(validUUID())

        val appId = applicationId!!.toUUID()
        val msgId = messageId!!.toUUID()
        val statement = SQLStatements.Queries.SELECT_MESSAGE

        val message: Message?

        try
        {
            message = database.queryForObject(statement, serializer, appId, msgId)

        }
        catch (ex: EmptyResultDataAccessException)
        {
            LOG.warn("Message does not exist: [{}/{}]", appId, messageId, ex)
            throw DoesNotExistException(ex.message)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to get message [{}/{}]", applicationId, messageId, ex)
            throw OperationFailedException(ex.message)
        }

        checkThat(message)
                .throwing(DoesNotExistException::class.java)
                .`is`(notNull<Message>())

        return message
    }

    @Throws(TException::class)
    override fun deleteMessage(applicationId: String?, messageId: String?)
    {
        checkThat(applicationId, messageId)
                .throwing(InvalidArgumentException::class.java)
                .are(validUUID())

        val appId = applicationId!!.toUUID()
        val msgId = messageId!!.toUUID()
        val statement = SQLStatements.Deletes.MESSAGE

        try
        {
            val updatedRows = database.update(statement, appId, msgId)

            LOG.debug("{} rows affected deleting message [{}/{}]", updatedRows, applicationId, messageId)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to delete message [{}/{}]", applicationId, messageId)
            throw OperationFailedException(ex.message)
        }

    }

    @Throws(TException::class)
    override fun containsMessage(applicationId: String?, messageId: String?): Boolean
    {
        checkThat(applicationId, messageId)
                .throwing(InvalidArgumentException::class.java)
                .are(validUUID())

        val appId = applicationId!!.toUUID()
        val msgId = messageId!!.toUUID()
        val statement = SQLStatements.Queries.CHECK_MESSAGE

        try
        {
            return database.queryForObject(statement, Boolean::class.java, appId, msgId)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to check whether message exists: [{}/{}]", appId, msgId)
            throw OperationFailedException(ex.message)
        }

    }

    @Throws(TException::class)
    override fun getByHostname(hostname: String?): List<Message>
    {
        checkThat(hostname)
                .throwing(InvalidArgumentException::class.java)
                .`is`(nonEmptyString())

        val statement = SQLStatements.Queries.SELECT_MESSAGES_BY_HOSTNAME

        try
        {
            return database.query(statement, serializer, hostname)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to get all messages by hostname: [{}]", hostname, ex)
            throw OperationFailedException("Coult not get all messages by hostname: " + hostname + " | " + ex.message)
        }

    }

    @Throws(TException::class)
    override fun getByApplication(applicationId: String?): List<Message>
    {
        checkThat(applicationId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validUUID())

        val appId = applicationId!!.toUUID()
        val query = SQLStatements.Queries.SELECT_MESSAGES_BY_APPLICATION

        try
        {
            return database.query(query, serializer, appId)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to get messages for Application: {}", appId, ex)
            throw OperationFailedException(ex.message)
        }

    }


    @Throws(TException::class)
    override fun getByTitle(applicationId: String?, title: String?): List<Message>
    {
        checkThat(applicationId, title)
                .throwing(InvalidArgumentException::class.java)
                .are(nonEmptyString())

        checkThat(applicationId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validUUID())

        val appId = applicationId!!.toUUID()
        val query = SQLStatements.Queries.SELECT_MESSAGES_BY_TITLE

        try
        {
            return database.query(query, serializer, appId, title)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to query for messages by title [{}/{}]", applicationId, title)
            throw OperationFailedException(ex.message)
        }

    }

    @Throws(TException::class)
    override fun getCountByApplication(applicationId: String?): Long
    {
        checkThat(applicationId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(nonEmptyString())
                .`is`(validUUID())

        val appId = applicationId!!.toUUID()
        val query = SQLStatements.Queries.COUNT_MESSAGES


        try
        {
            return database.queryForObject(query, Long::class.java, appId)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to determine the number of messages for App [{}]", applicationId)
            throw OperationFailedException(ex.message)
        }

    }

    companion object
    {

        private val LOG = LoggerFactory.getLogger(SQLMessageRepository::class.java)
    }
}
