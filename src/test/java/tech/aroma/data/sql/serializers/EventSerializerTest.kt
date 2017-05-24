package tech.aroma.data.sql.serializers

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
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.apache.thrift.TException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.serializers.Tables.Activity
import tech.aroma.thrift.User
import tech.aroma.thrift.events.Event
import tech.aroma.thrift.generators.EventGenerators.events
import tech.aroma.thrift.generators.UserGenerators.users
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.thrift.ThriftObjects
import java.sql.ResultSet

@RunWith(AlchemyTestRunner::class)
class EventSerializerTest
{

    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var row: ResultSet

    private lateinit var event: Event
    private lateinit var user: User

    private val eventId get() = event.eventId
    private val userId get() = user.userId
    private val serializedEvent get() = ThriftObjects.toJson(event)

    @GenerateString
    private lateinit var sql: String

    private lateinit var instance: EventSerializer

    @Before
    fun setUp()
    {

        setupData()
        setupMocks()

        instance = EventSerializer()
    }

    @Test
    fun testSave()
    {
        instance.save(event, sql, database)
        verifyZeroInteractions(database)
    }

    @Test
    fun testSaveWithBadArgs()
    {
        assertThrows {
            instance.save(event, "", database)
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThrows {
            val emptyEvent = Event()
            instance.save(emptyEvent, sql, database)
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThrows {
            val invalidEvent = Event(event).setEventId(sql)
            instance.save(invalidEvent, sql, database)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testDeserialize()
    {
        val result = instance.deserialize(row)

        assertThat(result, equalTo(event))
    }

    @Test
    fun testDeserializeWhenDeserializeFails()
    {
        whenever(row.getString(Activity.SERIALIZED_EVENT)).thenReturn(sql)

        assertThrows { instance.deserialize(row) }
                .isInstanceOf(TException::class.java)
    }

    private fun setupData()
    {
        event = one(events())
        user = one(users())
    }

    private fun setupMocks()
    {
        whenever(row.getString(Activity.SERIALIZED_EVENT)).thenReturn(serializedEvent)
    }

}