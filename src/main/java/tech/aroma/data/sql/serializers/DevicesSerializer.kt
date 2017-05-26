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

package tech.aroma.data.sql.serializers

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.assertions.RequestAssertions.validMobileDevice
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.data.sql.serializers.Tables.UserPreferences
import tech.aroma.thrift.channels.MobileDevice
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import tech.sirwellington.alchemy.thrift.ThriftObjects
import java.sql.ResultSet


/**
 *
 * @author SirWellington
 */
internal class DevicesSerializer : DatabaseSerializer<Set<MobileDevice>>
{
    private companion object
    {
        @JvmStatic val LOG = LoggerFactory.getLogger(this::class.java)!!
    }

    override fun save(devices: Set<MobileDevice>, statement: String, database: JdbcOperations)
    {
        checkThat(statement).`is`(nonEmptyString())
        devices.forEach { checkThat(it).`is`(validMobileDevice()) }

    }

    override fun deserialize(row: ResultSet): Set<MobileDevice>
    {
        val devicesArray = row.getArray(UserPreferences.SERIALIZED_DEVICES) ?: return emptySet()

        val devices = devicesArray.array as? Array<String> ?: return emptySet()

        return devices.map(this::deviceFromJson).filterNotNull().toSet()
    }

    private fun deviceFromJson(json: String): MobileDevice?
    {
        return try
        {
            ThriftObjects.fromJson(MobileDevice(), json)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to parse Mobile Device from JSON | $json", ex)
            return null
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

}