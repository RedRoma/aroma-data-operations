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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.AromaGenerators.Devices
import tech.aroma.data.sql.serializers.Columns.UserPreferences
import tech.aroma.thrift.channels.MobileDevice
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.thrift.ThriftObjects
import java.sql.Array
import java.sql.ResultSet

@RunWith(AlchemyTestRunner::class)
class DevicesSerializerTest
{

    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var row: ResultSet

    @Mock
    private lateinit var array: Array

    private lateinit var device: MobileDevice
    private lateinit var devices: MutableSet<MobileDevice>

    @GenerateString
    private lateinit var sql: String

    private val serializedDevices get() = devices.map(ThriftObjects::toJson)

    private lateinit var instance: DevicesSerializer

    @Before
    fun setUp()
    {
        setupData()
        setupMocks()

        instance = DevicesSerializer()
    }

    @Test
    fun testSave()
    {
        instance.save(devices, sql, database)

        verifyZeroInteractions(database)
    }

    @Test
    fun testSaveWithBadArgs()
    {
        assertThrows {
            instance.save(devices, "", database)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Repeat
    @Test
    fun testDeserialize()
    {
        val results = instance.deserialize(row)

        assertThat(results, equalTo(devices))
    }

    private fun setupData()
    {
        device = Devices.device
        devices = Devices.devices
    }

    private fun setupMocks()
    {
        whenever(row.getArray(UserPreferences.SERIALIZED_DEVICES))
                .thenReturn(array)

        whenever(array.array).thenReturn(serializedDevices.toTypedArray())
    }

}