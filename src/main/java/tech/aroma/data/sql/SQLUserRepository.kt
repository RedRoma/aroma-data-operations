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
import tech.aroma.data.UserRepository
import tech.aroma.data.assertions.RequestAssertions.validUser
import tech.aroma.data.assertions.RequestAssertions.validUserId
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.User
import tech.aroma.thrift.exceptions.*
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.PeopleAssertions.validEmailAddress
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
internal class SQLUserRepository
@Inject constructor(val database: JdbcOperations,
                    val serializer: DatabaseSerializer<User>) : UserRepository
{
    private companion object
    {
        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override fun saveUser(user: User)
    {
        checkThat(user)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validUser())

        val sql = Inserts.USER

        try
        {
            serializer.save(user, null, sql, database)
        }
        catch(ex: Exception)
        {
            val message = "Failed to save user in database: [$user]"
            logMessageAndFail(message, ex)
        }
    }


    override fun getUser(userId: String): User
    {
        checkUserId(userId)

        val sql = Queries.SELECT_USER

        return try
        {
            database.queryForObject(sql, serializer, userId.toUUID())
        }
        catch (ex: Exception)
        {
            val message = "Failed to retrieve User with ID [$userId] from database"
            logMessageAndFail(message, ex)
        }

    }

    override fun deleteUser(userId: String)
    {
        checkUserId(userId)

        val sql = Deletes.USER

        try
        {
            val updated = database.update(sql, userId.toUUID())
            LOG.info("Successfully deleted $userId. $updated rows affedted")
        }
        catch (ex: Exception)
        {
            val message = "Failed to delete user $userId"
            logMessageAndFail(message, ex)
        }

    }

    override fun containsUser(userId: String): Boolean
    {
        checkUserId(userId)

        val sql = Queries.CHECK_USER

        return try
        {
            database.queryForObject(sql, Boolean::class.java, userId.toUUID())
        }
        catch (ex: Exception)
        {
            val message = "Failed to check if user exists: [$userId]"
            logMessageAndFail(message, ex)
        }
    }

    override fun getUserByEmail(emailAddress: String): User
    {
        checkThat(emailAddress)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validEmailAddress())

        val sql = Queries.SELECT_USER_BY_EMAIL

        return try
        {
            database.queryForObject(sql, serializer, emailAddress)
        }
        catch (ex: EmptyResultDataAccessException)
        {
            val message = "Could not find a user with email address: [$emailAddress]"
            LOG.info(message, ex)
            throw DoesNotExistException(message)
        }
        catch (ex: Exception)
        {
            val message = "Failed to get a user by $emailAddress"
            logMessageAndFail(message, ex)
        }
    }

    override fun findByGithubProfile(githubProfile: String): User
    {
        checkThat(githubProfile)
                .throwing(InvalidArgumentException::class.java)
                .`is`(nonEmptyString())

        val sql = Queries.SELECT_USER_BY_EMAIL

        return try
        {
            database.queryForObject(sql, serializer, githubProfile)
        }
        catch (ex: EmptyResultDataAccessException)
        {
            val message = "Could not find a user with Github profile [$githubProfile]"
            LOG.warn(message, ex)
            throw DoesNotExistException(message)
        }
        catch (ex: Exception)
        {
            val message = "Failed to query for a user with Github profile [$githubProfile]"
            logMessageAndFail(message, ex)
        }
    }

    override fun getRecentlyCreatedUsers(): MutableList<User>
    {
        val sql = Queries.SELECT_RECENT_USERS

        return try
        {
            database.query(sql, serializer)
        }
        catch (ex: Exception)
        {
            val message = "Failed to return a list of recent Users"
            LOG.error(message, ex)
            return mutableListOf()
        }
    }

    private fun checkUserId(userId: String?)
    {
        checkThat(userId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validUserId())
    }

    private fun logMessageAndFail(message: String, ex: Exception): Nothing
    {
        LOG.error(message, ex)
        throw OperationFailedException("$message | ${ex.message}")
    }
}