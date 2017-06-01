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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.aroma.data.assertions.RequestAssertions.*
import tech.aroma.thrift.Application
import tech.aroma.thrift.User
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.OperationFailedException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat


/**
 *
 * @author SirWellington
 */

private object LOG: Logger by LoggerFactory.getLogger(LOG::class.java)

internal fun failWithMessage(message: String, ex: Exception): Nothing
{
    LOG.error(message, ex)
    throw OperationFailedException("$message | ${ex.message}")
}

internal fun checkApplication(app: Application?)
{
    checkThat(app)
            .throwing(InvalidArgumentException::class.java)
            .`is`(validApplication())
}

internal fun checkAppId(appId: String?)
{
    checkThat(appId)
            .throwing(InvalidArgumentException::class.java)
            .`is`(validApplicationId())
}

internal fun checkUser(user: User?)
{
    checkThat(user)
            .throwing(InvalidArgumentException::class.java)
            .`is`(validUser())
}

internal fun checkUserId(userId: String?)
{
    checkThat(userId)
            .throwing(InvalidArgumentException::class.java)
            .`is`(validUserId())

}