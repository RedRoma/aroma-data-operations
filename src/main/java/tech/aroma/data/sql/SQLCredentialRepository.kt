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

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.CredentialRepository
import tech.aroma.data.assertions.RequestAssertions.validUserId
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.exceptions.DoesNotExistException
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
internal class SQLCredentialRepository @Inject constructor(val database: JdbcOperations) : CredentialRepository
{
    override fun saveEncryptedPassword(userId: String, encryptedPassword: String)
    {
        checkUserId(userId)
        checkThat(encryptedPassword)
                .throwing(InvalidArgumentException::class.java)
                .`is`(StringAssertions.nonEmptyString())

        val sql = Inserts.CREDENTIAL

        try
        {
            database.update(sql, userId.toUUID(), encryptedPassword)
        }
        catch (ex: Exception)
        {
            failWithError("Failed to save encrypted password for [$userId]", ex)
        }

    }

    override fun containsEncryptedPassword(userId: String): Boolean
    {
        checkUserId(userId)

        val sql = Queries.CHECK_CREDENTIAL

        return try
        {
            database.queryForObject(sql, Boolean::class.java, userId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithError("Could not check if user has password: [$userId]", ex)
        }
    }

    override fun getEncryptedPassword(userId: String): String
    {
        checkUserId(userId)

        val sql = Queries.SELECT_CREDENTIAL

        return try
        {
            database.queryForObject(sql, String::class.java, userId.toUUID())
        }
        catch (ex: EmptyResultDataAccessException)
        {
            throw DoesNotExistException("No credentials found for [$userId]")
        }
        catch (ex: Exception)
        {
            failWithError("Could not get user password: [$userId]", ex)
        }

    }

    override fun deleteEncryptedPassword(userId: String)
    {
        checkUserId(userId)

        val sql = Deletes.CREDENTIAL

        try
        {
            database.update(sql, userId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithError("Failed to delete credentials for User [$userId]", ex)
        }

    }

    private fun checkUserId(id: String)
    {
        checkThat(id)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validUserId())
    }

}