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
import tech.aroma.data.UserPreferencesRepository
import tech.aroma.data.assertions.RequestAssertions.validMobileDevice
import tech.aroma.data.assertions.RequestAssertions.validUserId
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.channels.MobileDevice
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.OperationFailedException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.thrift.ThriftObjects
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
internal class SQLUserPreferencesRepository
@Inject constructor(val database: JdbcOperations, val serializer: DatabaseSerializer<MutableSet<MobileDevice>>) : UserPreferencesRepository
{
    override fun saveMobileDevice(userId: String, device: MobileDevice)
    {
        checkUserId(userId)
        checkMobileDevice(device)

        val sql = Inserts.ADD_USER_DEVICE

        val serialized = try { ThriftObjects.toJson(device) }
        catch (ex: Exception)
        {
            failWithError("Failed to serialized Mobile Device: [$device]", ex)
        }

        try
        {
            database.update(sql, userId.toUUID(), serialized)
        }
        catch (ex: Exception)
        {
            failWithError("Failed to save a new mobile device for [$userId]", ex)
        }
    }

    override fun saveMobileDevices(userId: String, devices: MutableSet<MobileDevice>)
    {
        checkUserId(userId)
        devices.forEach(this::checkMobileDevice)

        val sql = Inserts.USER_DEVICES
        val serialized = devices.map(this::deviceToJson).filterNotNull()

        try
        {
            database.update(sql, { preparedStatement ->
                preparedStatement.setObject(1, userId.toUUID())

                val serializedStringArray = serialized.toTypedArray()
                val serializedDevices = preparedStatement.connection.createArrayOf("TEXT", serializedStringArray)
                preparedStatement.setArray(2, serializedDevices)
            })
        }
        catch (ex: Exception)
        {
            failWithError("Failed to save ${devices.size} devices for [$userId]", ex)
        }
    }

    override fun getMobileDevices(userId: String): MutableSet<MobileDevice>
    {
        checkUserId(userId)

        val sql = Queries.SELECT_USER_DEVICES

        return try
        {
            database.queryForObject(sql, serializer, userId.toUUID()) ?: mutableSetOf()
        }
        catch (ex: EmptyResultDataAccessException)
        {
            mutableSetOf()
        }
        catch (ex: Exception)
        {
            failWithError("Failed to get mobile devices for [$userId]", ex)
        }
    }

    override fun deleteMobileDevice(userId: String, mobileDevice: MobileDevice)
    {
        checkUserId(userId)
        checkMobileDevice(mobileDevice)

        val sql = Deletes.USER_DEVICE
        val serialized = ThriftObjects.toJson(mobileDevice)

        try
        {
            database.update(sql, serialized, userId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithError("Failed to remove device for user [$userId] | [$mobileDevice]", ex)
        }
    }

    override fun deleteAllMobileDevices(userId: String)
    {
        checkUserId(userId)

        val sql = Deletes.ALL_USER_DEVICES

        try
        {
            database.update(sql, userId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithError("Failed to delete all mobile devices for [$userId]", ex)
        }
    }

    private fun deviceToJson(device: MobileDevice): String?
    {
        return try
        {
            ThriftObjects.toJson(device)
        }
        catch (ex: Exception)
        {
            LOG.warn("Failed to serialized mobile device [$device]", ex)
            return null
        }
    }

    private fun checkUserId(userId: String)
    {
        checkThat(userId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validUserId())
    }

    private fun checkMobileDevice(device: MobileDevice)
    {
        checkThat(device)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validMobileDevice())
    }

    private fun failWithError(message: String, ex: Exception): Nothing
    {
        LOG.error(message, ex)
        throw OperationFailedException("$message | ${ex.message}")
    }

    private companion object
    {
        @JvmStatic val LOG = LoggerFactory.getLogger(this::class.java)!!
    }

}