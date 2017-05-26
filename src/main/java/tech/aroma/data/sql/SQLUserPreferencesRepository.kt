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
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.UserPreferencesRepository
import tech.aroma.data.assertions.RequestAssertions.validMobileDevice
import tech.aroma.data.assertions.RequestAssertions.validUserId
import tech.aroma.thrift.channels.MobileDevice
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.OperationFailedException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
internal class SQLUserPreferencesRepository
@Inject constructor(val database: JdbcOperations, val serializer: DatabaseSerializer<Set<MobileDevice>>) : UserPreferencesRepository
{
    override fun saveMobileDevice(userId: String, mobileDevice: MobileDevice)
    {
        checkUserId(userId)
        checkMobileDevice(mobileDevice)

    }

    override fun saveMobileDevices(userId: String, mobileDevices: MutableSet<MobileDevice>)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMobileDevices(userId: String): MutableSet<MobileDevice>
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMobileDevice(userId: String, mobileDevice: MobileDevice)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAllMobileDevices(userId: String)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    private fun failWithError(message: String, ex: Exception) : Nothing
    {
        LOG.error(message, ex)
        throw OperationFailedException("$message | ${ex.message}")
    }

    private companion object
    {
        @JvmStatic val LOG = LoggerFactory.getLogger(this::class.java)!!
    }

}