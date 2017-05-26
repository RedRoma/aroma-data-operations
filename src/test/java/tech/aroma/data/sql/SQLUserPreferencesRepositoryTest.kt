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

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.PreparedStatementSetter
import tech.aroma.data.AromaGenerators.Devices
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.channels.MobileDevice
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import tech.sirwellington.alchemy.thrift.ThriftObjects
import java.sql.*
import java.sql.Array

@RunWith(AlchemyTestRunner::class)
class SQLUserPreferencesRepositoryTest
{

    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var serializer: DatabaseSerializer<MutableSet<MobileDevice>>

    @Mock
    private lateinit var connection: Connection

    @Mock
    private lateinit var preparedStatement: PreparedStatement

    @Mock
    private lateinit var mockArray: Array

    @Captor
    private lateinit var statementCaptor: ArgumentCaptor<PreparedStatementSetter>

    private lateinit var device: MobileDevice
    private lateinit var devices: MutableSet<MobileDevice>

    private val serializedDevice get() = ThriftObjects.toJson(device)
    private val serializedDevices get() = devices.map(ThriftObjects::toJson)

    @GenerateString(UUID)
    private lateinit var userId: String

    @GenerateString(ALPHABETIC)
    private lateinit var badId: String

    private val invalidDevice get() = MobileDevice()

    private lateinit var instance: SQLUserPreferencesRepository

    @Before
    fun setUp()
    {

        setupData()
        setupMocks()

        instance = SQLUserPreferencesRepository(database, serializer)
    }

    @Repeat
    @Test
    fun testSaveMobileDevice()
    {
        val sql = Inserts.ADD_USER_DEVICE

        instance.saveMobileDevice(userId, device)

        verify(database).update(sql, userId.toUUID(), serializedDevice)
    }

    @Test
    fun testSaveMobileDeviceWhenDatabaseFails()
    {
        setupForFailure()

        assertThrows { instance.saveMobileDevice(userId, device) }.operationError()
    }

    @Test
    fun testSaveMobileDeviceWithBadArgs()
    {
        assertThrows { instance.saveMobileDevice("", device) }.invalidArg()
        assertThrows { instance.saveMobileDevice(badId, device) }.invalidArg()
        assertThrows { instance.saveMobileDevice(userId, invalidDevice) }.invalidArg()
    }

    @Repeat
    @Test
    fun testSaveMobileDevices()
    {
        val sql = Inserts.USER_DEVICES

        instance.saveMobileDevices(userId, devices)

        verify(database).update(eq(sql), statementCaptor.capture())

        val setter = statementCaptor.value

        setter.setValues(preparedStatement)

        verify(connection).createArrayOf("TEXT", serializedDevices.toTypedArray())
        verify(preparedStatement).setObject(1, userId.toUUID())
        verify(preparedStatement).setArray(2, mockArray)
    }

    @Test
    fun testSaveMobileDevicesWhenDatabaseFails()
    {
        setupForFailure()

        assertThrows { instance.saveMobileDevices(userId, devices) }.operationError()
    }

    @Test
    fun testSaveMobileDevicesWithBadArgs()
    {
        assertThrows { instance.saveMobileDevices("", devices) }.invalidArg()
        assertThrows { instance.saveMobileDevices(badId, devices) }.invalidArg()
        assertThrows { instance.saveMobileDevices(userId, mutableSetOf(invalidDevice)) }.invalidArg()
    }

    @Repeat
    @Test
    fun testGetMobileDevices()
    {
        val sql = Queries.SELECT_USER_DEVICES

        whenever(database.queryForObject(sql, serializer, userId.toUUID()))
                .thenReturn(devices)

        val results = instance.getMobileDevices(userId)
        assertThat(results, notEmpty and equalTo(devices))
    }

    @Test
    fun testGetMobileDevicesWhenDatabaseFails()
    {
        setupForFailure()

        assertThrows { instance.getMobileDevices(userId) }.operationError()
    }

    @Test
    fun testGetMobileDevicesWithBadArgs()
    {
        assertThrows { instance.getMobileDevices("") }.invalidArg()
        assertThrows { instance.getMobileDevices(badId) }.invalidArg()
    }

    @Repeat
    @Test
    fun testDeleteMobileDevice()
    {
        val sql = Deletes.USER_DEVICE

        instance.deleteMobileDevice(userId, device)

        verify(database).update(sql, serializedDevice, userId.toUUID())
    }

    @Test
    fun testDeleteMobileDeviceWhenDatabaseFails()
    {
        setupForFailure()

        assertThrows { instance.deleteMobileDevice(userId, device) }.operationError()
    }

    @Test
    fun testDeleteMobileDeviceWithBadArgs()
    {
        assertThrows { instance.deleteMobileDevice("", device) }.invalidArg()
        assertThrows { instance.deleteMobileDevice(badId, device) }.invalidArg()
        assertThrows { instance.deleteMobileDevice(userId, invalidDevice) }.invalidArg()
    }

    @Repeat
    @Test
    fun testDeleteAllMobileDevices()
    {
        val sql = Deletes.ALL_USER_DEVICES

        instance.deleteAllMobileDevices(userId)

        verify(database).update(sql, userId.toUUID())
    }

    @Test
    fun testDeleteAllMobileDevicesWhenDatabaseFails()
    {
        setupForFailure()

        assertThrows { instance.deleteAllMobileDevices(userId) }.operationError()
    }

    @Test
    fun testDeleteAllMobileDevicesWithBadArgs()
    {
        assertThrows { instance.deleteAllMobileDevices("") }.invalidArg()
        assertThrows { instance.deleteAllMobileDevices(badId) }.invalidArg()
    }

    private fun setupData()
    {
        device = Devices.device
        devices = Devices.devices
    }

    private fun setupMocks()
    {
        whenever(preparedStatement.connection).thenReturn(connection)

        whenever(mockArray.array).thenReturn(serializedDevices.toTypedArray())

        whenever(connection.createArrayOf("TEXT", serializedDevices.toTypedArray()))
                .thenReturn(mockArray)
    }

    private fun setupForFailure()
    {

        whenever(database.update(any<String>(), any<PreparedStatementSetter>()))
                .thenThrow(RuntimeException::class.java)

        whenever(database.update(any<String>(), Mockito.anyVararg<Any>()))
                .thenThrow(RuntimeException::class.java)

        whenever(database.queryForObject(any<String>(), eq(serializer), Mockito.anyVararg<Any>()))
                .thenThrow(RuntimeException::class.java)
    }

}