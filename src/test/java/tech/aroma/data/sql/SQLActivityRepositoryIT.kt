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
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.notNull
import tech.aroma.data.sql.serializers.EventSerializer
import tech.aroma.thrift.User
import tech.aroma.thrift.events.Event
import tech.aroma.thrift.exceptions.DoesNotExistException
import tech.aroma.thrift.generators.EventGenerators.events
import tech.aroma.thrift.generators.UserGenerators.users
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
@IntegrationTest
class SQLActivityRepositoryIT
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

    private lateinit var event: Event
    private lateinit var user: User

    private val userId get() = user.userId
    private val eventId get() = event.eventId

    private val serializer = EventSerializer()

    private lateinit var instance: SQLActivityRepository

    @Before
    fun setUp()
    {

        setupData()
        setupMocks()

        instance = SQLActivityRepository(database, serializer)
    }

    @After
    fun cleanUp()
    {
        try
        {
            instance.deleteAllEventsFor(user)
        }
        catch (ex: Exception)
        {
            print(ex)
        }
    }

    @Test
    fun testSaveEvent()
    {
        instance.saveEvent(event, user)

        assertTrue { instance.containsEvent(eventId, user) }
    }

    @Test
    fun testSaveEventTwice()
    {
        instance.saveEvent(event, user)

        assertThrows { instance.saveEvent(event, user) }
    }

    @Test
    fun testContainsEvent()
    {
        assertFalse { instance.containsEvent(eventId, user) }

        instance.saveEvent(event, user)

        assertTrue { instance.containsEvent(eventId, user) }
    }

    @Test
    fun testGetEvent()
    {
        instance.saveEvent(event, user)

        val result = instance.getEvent(eventId, user)

        assertThat(result, equalTo(event))
    }

    @Test
    fun testGetEventWhenNotExists()
    {
        assertThrows {
            instance.getEvent(eventId, user)
        }.isInstanceOf(DoesNotExistException::class.java)
    }

    @Test
    fun testGetAllEventsFor()
    {
        val events = CollectionGenerators.listOf(events(), 20)

        events.forEach { instance.saveEvent(it, user) }

        val results = instance.getAllEventsFor(user)

        assertThat(results.toSet(), equalTo(events.toSet()))
    }

    @Test
    fun testGetAllEventsWhenNone()
    {
        val result = instance.getAllEventsFor(user)

        assertThat(result, notNull)
        assertThat(result, isEmpty)
    }

    @Test
    fun testDeleteEvent()
    {
        instance.saveEvent(event, user)

        instance.deleteEvent(eventId, user)
        assertFalse { instance.containsEvent(eventId, user) }
    }

    @Test
    fun testDeleteEventWhenNone()
    {
        instance.deleteEvent(eventId, user)
    }

    @Test
    fun testDeleteAllEventsFor()
    {
        val events = CollectionGenerators.listOf(events(), 40)

        events.forEach { instance.saveEvent(it, user) }

        var results = instance.getAllEventsFor(user)
        assertThat(results, !isEmpty)
        assertThat(results.size, equalTo(events.size))

        instance.deleteAllEventsFor(user)

        results = instance.getAllEventsFor(user)
        assertThat(results, isEmpty)

        events.forEach { assertFalse { instance.containsEvent(it.eventId, user) } }
    }

    private fun setupData()
    {
        user = one(users())
        event = one(events())
    }

    private fun setupMocks()
    {

    }

}