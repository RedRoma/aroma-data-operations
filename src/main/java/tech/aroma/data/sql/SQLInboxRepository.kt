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

import org.omg.CORBA.DynAnyPackage.Invalid
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.InboxRepository
import tech.aroma.data.assertions.RequestAssertions.*
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.*
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
internal class SQLInboxRepository
@Inject constructor(val database: JdbcOperations, val serializer: DatabaseSerializer<Message>) : InboxRepository
{

    private companion object
    {
        @JvmStatic val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override fun saveMessageForUser(user: User, message: Message, lifetime: LengthOfTime)
    {
        checkThat(user)
                .throwing(InvalidArgumentException::class.java)
                .isA(validUser())

        checkThat(message)
                .throwing(InvalidArgumentException::class.java)
                .isA(validMessage())

        tryToSaveMessage(user, message)
    }

    private fun tryToSaveMessage(user: User, message: Message)
    {
        val sql = Inserts.INBOX_MESSAGE

        val userId = user.userId?.toUUID() ?: throw Invalid("missing user_id")
        val appId = message.applicationId.toUUID() ?: throw InvalidArgumentException("missing app_id")
        val messageId = message.messageId.toUUID() ?: throw InvalidArgumentException("missing message_id")
        val timeCreated = if (message.timeOfCreation > 0) message.timeOfCreation.toTimestamp() else null
        val timeReceived = if (message.timeMessageReceived > 0) message.timeMessageReceived.toTimestamp() else null

        try
        {
            database.update(sql,
                            userId,
                            messageId,
                            appId,
                            message.applicationName,
                            message.title,
                            message.body,
                            message.urgency?.toString(),
                            timeCreated,
                            timeReceived,
                            message.hostname,
                            message.macAddress,
                            message.deviceName)
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to save message in inbox of user [$userId] | [$message]", ex)
        }
    }


    override fun getMessagesForUser(userId: String): MutableList<Message>
    {
        checkUserId(userId)

        val sql = Queries.SELECT_INBOX_MESSAGES_FOR_USER

        return try
        {
            database.query(sql, serializer, userId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to find inbox messages for user [$userId]", ex)
        }
    }

    override fun containsMessageInInbox(userId: String, message: Message): Boolean
    {
        checkUserId(userId)
        checkMessageId(message.messageId)

        val sql = Queries.CHECK_INBOX_MESSAGE
        val userId = userId.toUUID()!!
        val messageId = message.messageId.toUUID()!!

        return try
        {
            database.queryForObject(sql, Boolean::class.java, userId, messageId)
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to check if message [$messageId] exists for user [$userId]", ex)
        }
    }


    override fun deleteMessageForUser(userId: String, messageId: String)
    {
        checkUserId(userId)
        checkMessageId(messageId)

        val sql = Deletes.INBOX_MESSAGE

        try
        {
            val updated = database.update(sql, userId.toUUID(), messageId.toUUID())
            LOG.debug("Operation to delete inbox message [$userId/$messageId] resulted in $updated rows updated")
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to delete inbox message [$userId/$messageId]", ex)
        }
    }

    override fun deleteAllMessagesForUser(userId: String)
    {
        checkUserId(userId)

        val sql = Deletes.INBOX_ALL_MESSAGES

        try
        {
            val updated = database.update(sql, userId.toUUID())
            LOG.debug("Operation to delete all messages for [$userId] deleted $updated rows")
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to delete all messages for user [$userId]", ex)
        }
    }

    override fun countInboxForUser(userId: String): Long
    {
        checkUserId(userId)

        val sql = Queries.COUNT_INBOX_MESSAGES

        return try
        {
            database.queryForObject(sql, Long::class.java, userId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to count inbox messages for user [$userId]", ex)
        }
    }

}