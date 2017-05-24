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

import com.natpryce.hamkrest.anything
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.User
import tech.aroma.thrift.events.Event
import tech.aroma.thrift.exceptions.*
import tech.aroma.thrift.generators.EventGenerators
import tech.aroma.thrift.generators.UserGenerators.users
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.BooleanGenerators.booleans
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.thrift.ThriftObjects
import kotlin.reflect.KClass

@RunWith(AlchemyTestRunner::class)
@Repeat
class SQLActivityRepositoryTest
{
    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var serializer: DatabaseSerializer<Event>

    private lateinit var event: Event
    private lateinit var user: User
    private lateinit var events: MutableList<Event>
    private val eventId get() = event.eventId
    private val userId get() = user.userId
    private val serializedEvent get() = ThriftObjects.toJson(event)

    private lateinit var invalidId: String

    private lateinit var instance: SQLActivityRepository

    @Before
    fun setUp()
    {

        setupData()
        setupMocks()

        instance = SQLActivityRepository(database, serializer)
    }

    @Test
    fun testSaveEvent()
    {
        val sql = Inserts.ACTIVITY_EVENT

        instance.saveEvent(event, user)

        verify(database).update(sql,
                                userId.toUUID(),
                                eventId.toUUID(),
                                event.applicationId.toUUID(),
                                event.userIdOfActor.toUUID(),
                                event.timestamp.toTimestamp(),
                                event.eventType.toString(),
                                serializedEvent)
    }

    @DontRepeat
    @Test
    fun testSaveEventWhenDatabaseFails()
    {
        setupForFailure()

        assertThrows { instance.saveEvent(event, user) }
                .isInstanceOf(OperationFailedException::class.java)
    }

    @DontRepeat
    @Test
    fun testSaveEventWithBadArgs()
    {
        assertThrows {
            val emptyEvent = Event()
            instance.saveEvent(emptyEvent, user)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val emptyUser = User()
            instance.saveEvent(event, emptyUser)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val invalidUser = User(user).setUserId(invalidId)
            instance.saveEvent(event, invalidUser)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val invalidEvent = Event(event).setEventId(invalidId)
            instance.saveEvent(invalidEvent, user)
        }
    }

    @Test
    fun testContainsEvent()
    {
        val sql = Queries.CHECK_ACTIVITY_EVENT

        val expected = one(booleans())

        whenever(database.queryForObject(sql, Boolean::class.java, userId.toUUID(), eventId.toUUID()))
                .thenReturn(expected)

        val result = instance.containsEvent(eventId, user)
        assertThat(result, equalTo(expected))
    }

    @DontRepeat
    @Test
    fun testContainsEventWhenDatabaseFails()
    {
        setupForFailure()

        assertThrows { instance.containsEvent(eventId, user) }
                .isInstanceOf(OperationFailedException::class.java)
    }

    @DontRepeat
    @Test
    fun testContainsEventWithBadArgs()
    {
        assertThrows {
            instance.containsEvent("", user)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            instance.containsEvent(invalidId, user)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val emptyUser = User()
            instance.containsEvent(eventId, emptyUser)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val invalidUser = User(user).setUserId(invalidId)
            instance.containsEvent(eventId, invalidUser)
        }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @Test
    fun testGetEvent()
    {
        val sql = Queries.SELECT_ACTIVITY_EVENT

        whenever(database.queryForObject(sql, serializer, userId.toUUID(), eventId.toUUID()))
                .thenReturn(event)

        val result = instance.getEvent(eventId, user)
        assertThat(result, equalTo(event))
    }

    @DontRepeat
    @Test
    fun testGetEventWhenDatabaseFails()
    {
        setupForFailure()

        assertThrows { instance.getEvent(eventId, user) }
                .isInstanceOf(OperationFailedException::class.java)
    }

    @DontRepeat
    @Test
    fun testGetEventWhenDoesNotExist()
    {
        val sql = Queries.SELECT_ACTIVITY_EVENT

        whenever(database.queryForObject(sql, serializer, userId.toUUID(), eventId.toUUID()))
                .thenThrow(EmptyResultDataAccessException(0))

        assertThrows { instance.getEvent(eventId, user) }
                .isInstanceOf(DoesNotExistException::class.java)
    }

    @DontRepeat
    @Test
    fun testGetEventWithBadArgs()
    {

        assertThrows {
            instance.getEvent("", user)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            instance.getEvent(invalidId, user)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val emptyUser = User()
            instance.getEvent(eventId, emptyUser)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val invalidUser = User(user).setUserId(invalidId)
            instance.getEvent(eventId, invalidUser)
        }.isInstanceOf(InvalidArgumentException::class.java)

    }

    @Test
    fun testGetAllEventsFor()
    {
        val sql = Queries.SELECT_ALL_ACTIVITY_FOR_USER

        whenever(database.query(sql, serializer, userId.toUUID()))
                .thenReturn(events)

        val results = instance.getAllEventsFor(user)

        assertThat(results, equalTo(events))
    }

    @DontRepeat
    @Test
    fun testGetAllEventsWhenDatabaseFails()
    {
        setupForFailure()

        assertThrows { instance.getAllEventsFor(user) }
                .isInstanceOf(OperationFailedException::class.java)
    }

    @DontRepeat
    @Test
    fun testGetAllEventsWithBadArgs()
    {
        assertThrows {
            val emptyUser = User()
            instance.getAllEventsFor(emptyUser)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val invalidUser = User(user).setUserId(invalidId)
            instance.getAllEventsFor(invalidUser)
        }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @Test
    fun testDeleteEvent()
    {
        val sql = Deletes.ACTIVITY_EVENT

        instance.deleteEvent(eventId, user)

        verify(database).update(sql, userId.toUUID(), eventId.toUUID())
    }

    @DontRepeat
    @Test
    fun testDeleteEventWhenDatabaseFails()
    {
        setupForFailure()

        assertThrows { instance.deleteEvent(eventId, user) }
                .isInstanceOf(OperationFailedException::class.java)
    }

    @DontRepeat
    @Test
    fun testDeleteEventWithBadArgs()
    {
        assertThrows {
            instance.deleteEvent("", user)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            instance.deleteEvent(invalidId, user)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val emptyUser = User()
            instance.deleteEvent(eventId, emptyUser)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val invalidUser = User(user).setUserId(invalidId)
            instance.deleteEvent(eventId, invalidUser)
        }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @Test
    fun testDeleteAllEventsFor()
    {
        val sql = Deletes.ACTIVITY_ALL_EVENTS

        instance.deleteAllEventsFor(user)

        verify(database).update(sql, userId.toUUID())
    }

    @DontRepeat
    @Test
    fun testDeleteAllEventsWhenDatabaseFails()
    {
        setupForFailure()

        assertThrows { instance.deleteAllEventsFor(user) }
                .isInstanceOf(OperationFailedException::class.java)
    }

    @DontRepeat
    @Test
    fun testDeleteAllEventsWithBadArgs()
    {
        assertThrows {
            val emptyUser = User()
            instance.deleteAllEventsFor(emptyUser)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val invalidUser = User(user).setUserId(invalidId)
            instance.deleteAllEventsFor(invalidUser)
        }.isInstanceOf(InvalidArgumentException::class.java)
    }

    private fun setupData()
    {
        event = one(EventGenerators.events())
        user = one(users())

        events = CollectionGenerators.listOf(EventGenerators.events(), 20)

        invalidId = one(StringGenerators.alphabeticString())
    }

    private fun setupMocks()
    {

    }

    private fun setupForFailure()
    {
        whenever(database.query(any<String>(), eq(serializer), Mockito.anyVararg<Any>()))
                .thenThrow(RuntimeException())

        whenever(database.queryForObject(any<String>(), eq(serializer), Mockito.anyVararg<Any>()))
                .thenThrow(RuntimeException())

        whenever(database.queryForObject(any<String>(), eq(Boolean::class.java), Mockito.anyVararg<Any>()))
                .thenThrow(RuntimeException())

        whenever(database.update(any<String>(), Mockito.anyVararg<Any>()))
                .thenThrow(RuntimeException())
    }

}