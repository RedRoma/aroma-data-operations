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
import tech.aroma.data.sql.SQLStatements.Deletes
import tech.aroma.data.sql.SQLStatements.Inserts
import tech.aroma.data.sql.SQLStatements.Queries
import tech.aroma.thrift.User
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.UserDoesNotExistException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.*
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
                .isA(validUser())

        val sql = Inserts.USER

        try
        {
            serializer.save(user, sql, database)
        }
        catch(ex: Exception)
        {
            failWithMessage("Failed to save user in database: [$user]", ex)
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
        catch (ex: EmptyResultDataAccessException)
        {
            logAndFailWithNoSuchUser(userId)
        }
        catch (ex: Exception)
        {
            val message = "Failed to retrieve User with ID [$userId] from database"
            failWithMessage(message, ex)
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
            failWithMessage("Failed to delete user $userId", ex)
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
            failWithMessage("Failed to check if user exists: [$userId]", ex)
        }
    }

    override fun getUserByEmail(emailAddress: String): User
    {
        checkThat(emailAddress)
                .throwing(InvalidArgumentException::class.java)
                .isA(validEmailAddress())

        val sql = Queries.SELECT_USER_BY_EMAIL

        return try
        {
            database.queryForObject(sql, serializer, emailAddress)
        }
        catch (ex: EmptyResultDataAccessException)
        {
            val message = "Could not find a user with email address: [$emailAddress]"
            logAndFailWithNoSuchUser(message)
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to get a user by $emailAddress", ex)
        }
    }

    override fun findByGithubProfile(githubProfile: String): User
    {
        checkThat(githubProfile)
                .throwing(InvalidArgumentException::class.java)
                .isA(nonEmptyString())

        val sql = Queries.SELECT_USER_BY_GITHUB

        return try
        {
            database.queryForObject(sql, serializer, githubProfile)
        }
        catch (ex: EmptyResultDataAccessException)
        {
            val message = "Could not find a user with Github profile [$githubProfile]"
            logAndFailWithNoSuchUser(message)
        }
        catch (ex: Exception)
        {
            val message = "Failed to query for a user with Github profile [$githubProfile]"
            failWithMessage(message, ex)
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

    private fun logAndFailWithNoSuchUser(message: String? = null): Nothing
    {
        val message = message ?: "User does not exist"
        LOG.warn(message)
        throw UserDoesNotExistException(message)
    }

}