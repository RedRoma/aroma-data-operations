package tech.aroma.data.sql.serializers

import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.Assert.assertThat
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.Mockito.verify
import org.springframework.jdbc.core.JdbcTemplate
import tech.aroma.data.sql.toTimestamp
import tech.aroma.thrift.Message
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.equalToInstantWithinDelta
import tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.inTheFuture
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.NumberGenerators.integers
import tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.util.*
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
    private lateinit var statement: String

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
        instance.save(message, null, statement, database)
        checkMessageWithDuration(message, null)
    }

    @Test
    @Throws(Exception::class)
    fun testSaveWithExpiration()
    {
        val daysToLive = one(integers(3, 10))
        val ttl = Duration.ofDays(daysToLive.toLong())

        instance.save(message, ttl, statement, database)

        checkMessageWithDuration(message, ttl)
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testWhenDatabaseFails()
    {
        whenever(database.update(eq(statement), Mockito.anyVararg<Any>()))
                .thenThrow(RuntimeException())

        assertThrows { instance.save(message, null, statement, database) }
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testSaveWithInvalidIDs()
    {
        message.messageId = alphabetic

        assertThrows {
            instance.save(message, null, statement, database)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testSaveWithBadArgs()
    {
        val statement = one(alphabeticString())

        assertThrows {
            instance.save(message, null, "", database)
        }.isInstanceOf(IllegalArgumentException::class.java)

        message.applicationId = statement
        message.messageId = statement

        assertThrows {
            instance.save(message, null, statement, database)
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

    private fun checkMessageWithDuration(message: Message, ttl: Duration?)
    {
        verify(database).update(eq(statement), captor.capture())

        val arguments = captor.allValues

        assertThat(arguments[0], Is<Any>(UUID.fromString(messageId)))
        assertThat(arguments[1], Is<Any>(message.title))
        assertThat(arguments[2], Is<Any>(message.body))
        assertThat(arguments[3], Is<Any>(message.urgency.toString()))
        assertThat(arguments[4], Is<Any>(message.timeOfCreation.toTimestamp()))
        assertThat(arguments[5], Is<Any>(message.timeMessageReceived.toTimestamp()))

        val expiration = arguments[6]
        if (ttl != null)
        {
            checkExpirationWithTTL(expiration, ttl)
        }
        else
        {
            assertThat(expiration, nullValue())
        }

        assertThat(arguments[7], Is<Any>(message.hostname))
        assertThat(arguments[8], Is<Any>(message.macAddress))
        assertThat(arguments[9], Is<Any>(UUID.fromString(appId)))
        assertThat(arguments[10], Is<Any>(message.applicationName))
        assertThat(arguments[11], Is<Any>(message.deviceName))
    }

    private fun checkExpirationWithTTL(expiration: Any, ttl: Duration)
    {
        assertThat(expiration, notNullValue())
        assertThat(expiration, instanceOf<Any>(Timestamp::class.java))

        val actualExpiration = (expiration as Timestamp).toInstant()
        val expectedExpiration = Instant.now().plus(ttl)

        val acceptableDelta: Long = 100

        checkThat(actualExpiration)
                .`is`(inTheFuture())
                .`is`(equalToInstantWithinDelta(expectedExpiration, acceptableDelta))
    }

}