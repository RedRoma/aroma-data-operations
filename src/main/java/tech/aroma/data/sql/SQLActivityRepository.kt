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

import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.ActivityRepository
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.LengthOfTime
import tech.aroma.thrift.User
import tech.aroma.thrift.events.Event
import tech.aroma.thrift.exceptions.DoesNotExistException
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.sirwellington.alchemy.arguments.AlchemyAssertion
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.*
import tech.sirwellington.alchemy.thrift.ThriftObjects
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
internal class SQLActivityRepository
@Inject constructor(val database: JdbcOperations, val serializer: DatabaseSerializer<Event>) : ActivityRepository
{

    private companion object
    {
        @JvmStatic private val LOG = LoggerFactory.getLogger(this::class.java)!!
    }

    override fun saveEvent(event: Event, forUser: User, lifetime: LengthOfTime?)
    {
        checkEventId(event.eventId)

        val user = forUser
        checkUser(user)

        val recepientId = user.userId.toUUID()
        val eventId = event.eventId.toUUID()
        val appId = event.applicationId.toUUID()
        val actorId = event.userIdOfActor.toUUID()
        val serialized = try
        {
            ThriftObjects.toJson(event)
        }
        catch (ex: Exception)
        {
            val message = "Failed to serialize event [$event]"
            failWithMessage(message, ex)
        }

        val eventType = event.eventType?.toString()
        val timestamp = if (event.timestamp > 0) event.timestamp.toTimestamp() else Timestamps.now()

        val sql = Inserts.ACTIVITY_EVENT

        try
        {
            database.update(sql,
                            recepientId,
                            eventId,
                            appId,
                            actorId,
                            timestamp,
                            eventType,
                            serialized)
        }
        catch(ex: Exception)
        {
            val message = "Failed to save event [$event] for user [$recepientId]"
            failWithMessage(message, ex)
        }

    }


    override fun containsEvent(eventId: String, user: User): Boolean
    {
        checkEventId(eventId)
        checkUser(user)

        val eventId = eventId.toUUID()
        val userId = user.userId.toUUID()

        val sql = Queries.CHECK_ACTIVITY_EVENT

        return try
        {
            database.queryForObject(sql, Boolean::class.java, userId, eventId)
        }
        catch(ex: Exception)
        {
            val message = "Failed to check if event exists: [$userId/$eventId]"
            failWithMessage(message, ex)
        }
    }

    override fun getEvent(eventId: String, user: User): Event
    {
        checkEventId(eventId)
        checkUser(user)

        val sql = Queries.SELECT_ACTIVITY_EVENT
        val eventId = eventId.toUUID()
        val userId = user.userId.toUUID()

        return try
        {
            database.queryForObject(sql, serializer, userId, eventId)
        }
        catch (ex: EmptyResultDataAccessException)
        {
            val message = "Activity Event does not exist: [$userId/$eventId]"
            LOG.warn(message, ex)
            throw DoesNotExistException(message)
        }
        catch (ex: Exception)
        {
            val message = "Failed to get event [$userId/$eventId]"
            failWithMessage(message, ex)
        }
    }

    override fun getAllEventsFor(user: User): MutableList<Event>
    {
        checkUser(user)

        val userId = user.userId.toUUID()
        val sql = Queries.SELECT_ALL_ACTIVITY_FOR_USER

        return try
        {
            database.query(sql, serializer, userId) ?: mutableListOf()
        }
        catch(ex: Exception)
        {
            val message = "Failed to get all events for user [$userId]"
            failWithMessage(message, ex)
        }
    }

    override fun deleteEvent(eventId: String, user: User)
    {
        checkEventId(eventId)
        checkUser(user)

        val userId = user.userId.toUUID()
        val eventId = eventId.toUUID()
        val sql = Deletes.ACTIVITY_EVENT

        try
        {
            val deleted = database.update(sql, userId, eventId)
            LOG.debug("Operation to delete activity [$userId/$eventId] affected $deleted rows")
        }
        catch(ex: Exception)
        {
            val message = "Failed to delete Activity Event [$userId/$eventId]"
            failWithMessage(message, ex)
        }
    }

    override fun deleteAllEventsFor(user: User)
    {
        checkUser(user)

        val userId = user.userId.toUUID()
        val sql = Deletes.ACTIVITY_ALL_EVENTS

        try
        {
            val deleted = database.update(sql, userId)
            LOG.debug("Operation to clear Activity for user [$userId] affected $deleted rows")
        }
        catch (ex: Exception)
        {
            val message = "Failed to delete all messages for user [$user]"
            failWithMessage(message, ex)
        }
    }

    private fun checkEventId(eventId: String?)
    {
        checkThat(eventId)
                .throwing(InvalidArgumentException::class.java)
                .usingMessage("Invalid Event ID : $eventId")
                .isA(validUUID() as AlchemyAssertion<String?>)
    }

}