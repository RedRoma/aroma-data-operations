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
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.AromaGenerators.Messages
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.Message
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.BooleanGenerators.booleans
import tech.sirwellington.alchemy.generator.NumberGenerators.positiveLongs
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID

@RunWith(AlchemyTestRunner::class)
@Repeat
class SQLInboxRepositoryTest
{
    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var serializer: DatabaseSerializer<Message>

    @GenerateString(UUID)
    private lateinit var userId: String
    private val user get() = tech.aroma.thrift.User().setUserId(userId)

    private lateinit var message: Message
    private val messageId: String get() = message.messageId
    private lateinit var messages: MutableList<Message>

    private lateinit var instance: SQLInboxRepository

    @Before
    fun setup()
    {
        message = Messages.message
        messages = Messages.messages

        instance = SQLInboxRepository(database, serializer)
    }

    @Test
    fun testSaveMessageForUser()
    {
        instance.saveMessageForUser(user, message)

        val sql = Inserts.INBOX_MESSAGE

        verify(database).update(sql,
                                userId.toUUID(),
                                messageId.toUUID(),
                                message.applicationId.toUUID(),
                                message.applicationName,
                                message.title,
                                message.body,
                                message.urgency.toString(),
                                message.timeOfCreation.toTimestamp(),
                                message.timeMessageReceived.toTimestamp(),
                                message.hostname,
                                message.macAddress,
                                message.deviceName)

    }

    @Test
    fun testGetMessagesForUser()
    {
        val sql = Queries.SELECT_INBOX_MESSAGES_FOR_USER

        whenever(database.query(sql, serializer, userId.toUUID()))
                .thenReturn(messages)

        val result = instance.getMessagesForUser(userId)

        assertThat(result, equalTo(messages))
    }

    @Test
    fun testContainsMessageInInbox()
    {
        val sql = Queries.CHECK_INBOX_MESSAGE
        val expected = one(booleans())

        whenever(database.queryForObject(sql, Boolean::class.java, userId.toUUID(), messageId.toUUID()))
                .thenReturn(expected)

        val result = instance.containsMessageInInbox(userId, message)
        assertThat(result, equalTo(expected))

    }

    @Test
    fun testDeleteMessageForUser()
    {
        val sql = Deletes.INBOX_MESSAGE

        instance.deleteMessageForUser(userId, messageId)

        verify(database).update(sql, userId.toUUID(), messageId.toUUID())
    }

    @Test
    fun testDeleteAllMessagesForUser()
    {
        val sql = Deletes.INBOX_ALL_MESSAGES
        instance.deleteAllMessagesForUser(userId)

        verify(database).update(sql, userId.toUUID())
    }

    @Test
    fun testCountInboxForUser()
    {
        val sql = Queries.COUNT_INBOX_MESSAGES
        val count = one(positiveLongs())

        whenever(database.queryForObject(sql, Long::class.java, userId.toUUID()))
                .thenReturn(count)

        val result = instance.countInboxForUser(userId)

        assertThat(result, equalTo(count))
    }

}