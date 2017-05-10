/*
 * Copyright 2017 RedRoma, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.aroma.data.sql

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.apache.thrift.TException
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.jdbc.core.JdbcTemplate
import tech.aroma.thrift.Message
import tech.aroma.thrift.exceptions.DoesNotExistException
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
class SQLMessageRepositoryIT
{

    private lateinit var instance: SQLMessageRepository

    @GeneratePojo
    private lateinit var message: Message

    @GenerateString(GenerateString.Type.UUID)
    private lateinit var appId: String

    @GenerateString(GenerateString.Type.UUID)
    private lateinit var messageId: String

    private val serializer = TestingResources.getMessageSerializer()

    @Before
    fun setup()
    {
        message.applicationId = appId
        message.messageId = messageId
        message.timeMessageReceived = Instant.now().toEpochMilli()
        message.timeOfCreation = Instant.now().toEpochMilli()
        message.unsetIsTruncated()

        instance = SQLMessageRepository(database, serializer)
    }

    @After
    @Throws(TException::class)
    fun tearDown()
    {
        instance.deleteMessage(appId, messageId)
    }

    @Test
    @Throws(Exception::class)
    fun saveMessage()
    {
        instance.saveMessage(message)
    }

    @Test
    @Throws(Exception::class)
    fun getMessage()
    {
        assertThrows {
            instance.getMessage(appId, messageId)
        }.isInstanceOf(DoesNotExistException::class.java)

        instance.saveMessage(message)

        val result = instance.getMessage(appId, messageId)

        assertThat(result, equalTo(message))
    }

    @Test
    @Throws(Exception::class)
    fun deleteMessage()
    {
        instance.deleteMessage(appId, messageId)

        instance.saveMessage(message)

        assertThat(instance.getCountByApplication(appId), greaterThan(0L))

        instance.deleteMessage(appId, messageId)
        assertThat(instance.getCountByApplication(appId), equalTo(0L))

    }

    @Test
    @Throws(Exception::class)
    fun containsMessage()
    {
        assertFalse { instance.containsMessage(appId, messageId) }

        instance.saveMessage(message)

        assertTrue { instance.containsMessage(appId, messageId) }
    }

    @Test
    @Throws(Exception::class)
    fun getByHostname()
    {
        instance.saveMessage(message)

        val results: List<Message> = instance.getByHostname(message.hostname)

        assertFalse { results.isEmpty() }
        assertThat(results, hasElement(message))
    }

    @Test
    fun testGetByHostnameWhenNone()
    {
        val results = instance.getByHostname(message.hostname)

        assertFalse { results == null }
        assertThat(results, isEmpty)
    }

    @Test
    @Throws(Exception::class)
    fun getByApplication()
    {

        instance.saveMessage(message)

        val results = instance.getByApplication(appId)
        assertThat(results, !isEmpty)
        assertThat(results, hasElement(message))
    }

    @Test
    fun testGetByApplicationWhenNone()
    {
        val result = instance.getByApplication(appId)

        assertFalse { result == null }
        assertThat(result, isEmpty)
    }

    @Test
    @Throws(Exception::class)
    fun getByTitle()
    {
        instance.saveMessage(message)

        val results = instance.getByTitle(appId, message.title)
        assertThat(results, !isEmpty)
        assertThat(results, hasElement(message))
    }

    @Test
    fun testGetByTitleWhenNone()
    {
        val results = instance.getByTitle(appId, message.title)

        assertFalse { results == null }
        assertThat(results, isEmpty)
    }

    @Test
    @Throws(Exception::class)
    fun getCountByApplication()
    {
        assertThat(instance.getCountByApplication(appId), equalTo(0L))

        instance.saveMessage(message)

        assertThat(instance.getCountByApplication(appId), greaterThanOrEqualTo(1L))
    }

    companion object
    {
        private lateinit var database: JdbcTemplate

        @BeforeClass
        @JvmStatic
        @Throws(Exception::class)
        fun setUp()
        {
            database = TestingResources.connectToDatabase()
        }
    }

}