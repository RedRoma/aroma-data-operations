package tech.aroma.data.sql

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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmpty
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.notNull
import tech.aroma.data.sql.serializers.MessageSerializer
import tech.aroma.thrift.Message
import tech.aroma.thrift.User
import tech.aroma.thrift.generators.MessageGenerators.messages
import tech.sirwellington.alchemy.generator.CollectionGenerators.Companion.listOf
import tech.sirwellington.alchemy.generator.one
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
class SQLInboxRepositoryIT
{
    private companion object
    {
        @JvmStatic
        lateinit var database: JdbcOperations

        @JvmStatic
        @BeforeClass
        fun setupClass()
        {
            database = TestingResources.connectToDatabase()
        }
    }

    private lateinit var message: Message
    private lateinit var messageId: String

    private lateinit var messages: List<Message>

    @GenerateString(UUID)
    private lateinit var userId: String

    private val user: User get() = User().setUserId(userId)

    private val serializer = MessageSerializer()


    private lateinit var instance: SQLInboxRepository


    @Before
    fun setup()
    {
        message = one(messages())
        messageId = message.messageId
        messages = listOf(messages(), 10)


        instance = SQLInboxRepository(database, serializer)
    }

    @After
    fun destroy()
    {
        try
        {
            instance.deleteAllMessagesForUser(userId)
        }
        catch(ex: Exception)
        {
            print(ex)
        }
    }

    @Test
    fun testSaveMessageForUser()
    {
        instance.saveMessageForUser(user, message)

        assertTrue { instance.containsMessageInInbox(userId, message = message) }
    }

    @Test
    fun testGetMessagesForUser()
    {
        messages.forEach { instance.saveMessageForUser(user, it) }

        val results = instance.getMessagesForUser(userId)

        assertThat(results.toSet(), equalTo(messages.toSet()))
    }

    fun testGetMessagesForUserWhenEmpty()
    {
        val results = instance.getMessagesForUser(userId)

        assertThat(results, notNull)
        assertThat(results, !isEmpty)
    }

    @Test
    fun testContainsMessageInInbox()
    {
        assertFalse { instance.containsMessageInInbox(userId, message) }

        instance.saveMessageForUser(user, message)

        assertTrue { instance.containsMessageInInbox(userId, message) }
    }

    @Test
    fun testDeleteMessageForUser()
    {
        instance.saveMessageForUser(user, message)
        assertTrue { instance.containsMessageInInbox(userId, message) }

        instance.deleteMessageForUser(userId, messageId)
        assertFalse { instance.containsMessageInInbox(userId, message) }
    }

    @Test
    fun testDeleteMessageForUserWhenNoMessage()
    {
        instance.deleteMessageForUser(userId, messageId)
    }

    @Test
    fun testDeleteAllMessagesForUser()
    {
        messages.forEach { instance.saveMessageForUser(user, it) }

        instance.deleteAllMessagesForUser(userId)

        messages.forEach { assertFalse { instance.containsMessageInInbox(userId, it) } }

        assertThat(instance.countInboxForUser(userId), equalTo(0L))
    }

    @Test
    fun testDeleteAllMessagesForUserWhenNone()
    {
        instance.deleteAllMessagesForUser(userId)
    }

    @Test
    fun testCountInboxForUser()
    {
        assertThat(instance.countInboxForUser(userId), equalTo(0L))

        instance.saveMessageForUser(user, message)
        assertThat(instance.countInboxForUser(userId), equalTo(1L))
        instance.deleteAllMessagesForUser(userId)

        messages.forEach { instance.saveMessageForUser(user, it) }
        assertThat(instance.countInboxForUser(userId), equalTo(messages.size.toLong()))
    }

}