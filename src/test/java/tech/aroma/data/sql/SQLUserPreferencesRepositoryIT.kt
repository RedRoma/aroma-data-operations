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
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.isEmpty
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.AromaGenerators.Devices
import tech.aroma.data.notEmpty
import tech.aroma.data.notNull
import tech.aroma.data.sql.serializers.DevicesSerializer
import tech.aroma.thrift.channels.MobileDevice
import tech.aroma.thrift.generators.ChannelGenerators.mobileDevices
import tech.sirwellington.alchemy.generator.one
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import kotlin.test.assertTrue


@RunWith(AlchemyTestRunner::class)
class SQLUserPreferencesRepositoryIT
{
    companion object
    {
        @JvmStatic lateinit var database: JdbcOperations

        @JvmStatic
        @BeforeClass
        fun setupClass()
        {
            database = TestingResources.connectToDatabase()
        }
    }

    @GenerateString(UUID)
    private lateinit var userId: String

    private lateinit var device: MobileDevice

    private lateinit var devices: MutableSet<MobileDevice>

    private val serializer = DevicesSerializer()
    private lateinit var instance: SQLUserPreferencesRepository

    @Before
    fun setup()
    {
        device = Devices.device
        devices = Devices.devices

        instance = SQLUserPreferencesRepository(database, serializer)
    }

    @After
    fun destroy()
    {
        try
        {
            val sql = "DELETE FROM user_preferences WHERE user_id = ?"
            database.update(sql, userId.toUUID())
        }
        catch (ex: Exception)
        {
            print(ex)
        }

    }

    @Test
    fun testSaveMobileDevice()
    {
        instance.saveMobileDevice(userId, device)

        val results = instance.getMobileDevices(userId)
        assertTrue { results.contains(device) }
    }

    @Test
    fun testSaveDeviceTwice()
    {
        instance.saveMobileDevice(userId, device)
        instance.saveMobileDevice(userId, device)

        val results = instance.getMobileDevices(userId)
        assertThat(results.size, equalTo(1))
        assertThat(results, hasElement(device))
    }

    @Test
    fun testAddDevice()
    {
        instance.saveMobileDevices(userId, devices)

        val newDevice = one(mobileDevices())

        instance.saveMobileDevice(userId, newDevice)

        val expected = devices + newDevice
        val results = instance.getMobileDevices(userId)

        assertThat(results, equalTo(expected))
    }

    @Test
    fun testSaveMobileDevices()
    {
        instance.saveMobileDevices(userId, devices)

        val results = instance.getMobileDevices(userId)

        assertThat(results, equalTo(devices))
    }

    @Test
    fun testSaveMobileDevicesWithEmpty()
    {
        instance.saveMobileDevices(userId, mutableSetOf())

        val results = instance.getMobileDevices(userId)
        assertThat(results, notNull and isEmpty)
    }

    @Test
    fun testGetMobileDevices()
    {
        instance.saveMobileDevices(userId, devices)

        val results = instance.getMobileDevices(userId)
        assertThat(results, notEmpty)
        assertThat(results, equalTo(devices))
    }

    @Test
    fun testGetMobileDevicesWhenNone()
    {
        val results = instance.getMobileDevices(userId)
        assertThat(results, notNull and isEmpty)
    }

    @Test
    fun testDeleteMobileDevice()
    {
        val userDevices = devices + device
        val expected = devices - device

        instance.saveMobileDevices(userId, userDevices.toMutableSet())

        instance.deleteMobileDevice(userId, device)

        val results = instance.getMobileDevices(userId)
        assertThat(results, equalTo(expected))
    }

    @Test
    fun testDeleteMobileDeviceWhenNone()
    {
        instance.deleteMobileDevice(userId, device)
    }

    @Test
    fun testDeleteMobileDeviceWhenNotExists()
    {
        instance.saveMobileDevices(userId, devices)

        var newDevice = one(mobileDevices())

        while (newDevice.isSetWindowsPhoneDevice)
        {
            newDevice = one(mobileDevices())
        }

        instance.deleteMobileDevice(userId, newDevice)

        val results = instance.getMobileDevices(userId)
        assertThat(results, equalTo(devices))
    }

    @Test
    fun testDeleteAllMobileDevices()
    {
        instance.saveMobileDevices(userId, devices)

        instance.deleteAllMobileDevices(userId)

        val results = instance.getMobileDevices(userId)
        assertThat(results, notNull and isEmpty)
    }

    @Test
    fun testDeleteAllMobileDevicesWhenNone()
    {
        instance.deleteAllMobileDevices(userId)
    }
}