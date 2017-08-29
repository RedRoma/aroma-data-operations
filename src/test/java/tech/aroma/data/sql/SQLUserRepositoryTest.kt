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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isEmpty
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.invalidArg
import tech.aroma.data.operationError
import tech.aroma.data.sql.SQLStatements.Deletes
import tech.aroma.data.sql.SQLStatements.Inserts
import tech.aroma.data.sql.SQLStatements.Queries
import tech.aroma.thrift.User
import tech.aroma.thrift.exceptions.UserDoesNotExistException
import tech.aroma.thrift.generators.UserGenerators.users
import tech.sirwellington.alchemy.generator.BooleanGenerators.Companion.booleans
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.one
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import kotlin.test.assertEquals

@RunWith(AlchemyTestRunner::class)
@Repeat
class SQLUserRepositoryTest
{
    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var serializer: DatabaseSerializer<User>

    private lateinit var user: User
    private lateinit var userId: String

    @GenerateString(ALPHABETIC)
    private lateinit var invalidId: String

    @GenerateString
    private lateinit var github: String

    private lateinit var instance: SQLUserRepository


    @Before
    fun setup()
    {
        instance = SQLUserRepository(database, serializer)

        user = one(users())
        userId = user.userId
        user.githubProfile = github
    }

    @Test
    fun testSave()
    {
        val sql = Inserts.USER

        instance.saveUser(user)

        verify(serializer).save(user, sql, database)
        verifyZeroInteractions(database)
    }

    @DontRepeat
    @Test
    fun testSaveWhenSerializerFails()
    {
        val sql = Inserts.USER

        Mockito.doThrow(RuntimeException())
                .whenever(serializer)
                .save(user, sql, database)

        assertThrows { instance.saveUser(user) }.operationError()
    }

    @DontRepeat
    @Test
    fun testSaveUserWithBadArgs()
    {
        assertThrows {
            val emptyUser = User()
            instance.saveUser(emptyUser)
        }.invalidArg()

        assertThrows {
            val invalidUser = User(user).setUserId(invalidId)
            instance.saveUser(invalidUser)
        }.invalidArg()

        val shouldPass = {
            val withoutEmail = User(user)
            withoutEmail.unsetEmail()
            instance.saveUser(withoutEmail)
        }
        shouldPass()
    }

    @Test
    fun testGetUser()
    {
        val sql = Queries.SELECT_USER

        whenever(database.queryForObject(sql, serializer, userId.toUUID()))
                .thenReturn(user)

        val result = instance.getUser(userId)

        assertEquals(user, result)
    }

    @DontRepeat
    @Test
    fun testGetUserWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.getUser(userId) }.operationError()
    }

    @DontRepeat
    @Test
    fun testGetUserWhenUserDoesNotExist()
    {
        val sql = Queries.SELECT_USER

        whenever(database.queryForObject(sql, serializer, userId.toUUID()))
                .thenThrow(EmptyResultDataAccessException::class.java)

        assertThrows { instance.getUser(userId) }.isInstanceOf(UserDoesNotExistException::class.java)
    }

    @DontRepeat
    @Test
    fun testGetUserWithBadArgs()
    {
        assertThrows { instance.getUser("") }.invalidArg()
        assertThrows { instance.getUser(invalidId) }.invalidArg()
    }

    @Test
    fun testDeleteUser()
    {
        val sql = Deletes.USER

        instance.deleteUser(userId)

        verify(database).update(sql, userId.toUUID())
    }

    @DontRepeat
    @Test
    fun testDeleteUserWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.deleteUser(userId) }.operationError()
    }

    @DontRepeat
    @Test
    fun testDeleteUserWithBadArgs()
    {
        assertThrows { instance.deleteUser("") }.invalidArg()
        assertThrows { instance.deleteUser(invalidId) }.invalidArg()
    }

    @Test
    fun testContainsUser()
    {
        val sql = Queries.CHECK_USER

        val expected = one(booleans())

        whenever(database.queryForObject(sql, Boolean::class.java, userId.toUUID()))
                .thenReturn(expected)

        val result = instance.containsUser(userId)

        assertEquals(expected, result)
    }

    @DontRepeat
    @Test
    fun testContainsUserWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.containsUser(userId) }.operationError()
    }

    @DontRepeat
    @Test
    fun testContainsUserWithBadArgs()
    {
        assertThrows { instance.containsUser("") }.invalidArg()
        assertThrows { instance.containsUser(invalidId) }.invalidArg()
    }

    @Test
    fun testGetUserByEmail()
    {
        val query = Queries.SELECT_USER_BY_EMAIL
        val email = user.email

        whenever(database.queryForObject(query, serializer, email))
                .thenReturn(user)

        val result = instance.getUserByEmail(email)
        assertEquals(user, result)
    }

    @DontRepeat
    @Test
    fun testGetUserByEmailWhenDatabaseFails()
    {
        database.setupForFailure()

        val email = user.email

        assertThrows { instance.getUserByEmail(email) }
                .operationError()
    }

    @DontRepeat
    @Test
    fun testGetUserByEmailWhenUserDoesNotExist()
    {
        val sql = Queries.SELECT_USER_BY_EMAIL
        val email = user.email

        whenever(database.queryForObject(sql, serializer, email))
                .thenThrow(EmptyResultDataAccessException::class.java)

        assertThrows { instance.getUserByEmail(email) }
                .isInstanceOf(UserDoesNotExistException::class.java)
    }

    @Test
    fun testGetUserByEmailWithBadArgs()
    {
        assertThrows { instance.getUserByEmail("") }.invalidArg()

        assertThrows {
            val invalidEmail = one(alphabeticStrings())
            instance.getUserByEmail(invalidEmail)
        }.invalidArg()
    }

    @Test
    fun testFindByGithub()
    {
        val sql = Queries.SELECT_USER_BY_GITHUB

        whenever(database.queryForObject(sql, serializer, github))
                .thenReturn(user)

        val result = instance.findByGithubProfile(github)

        assertEquals(user, result)
    }

    @DontRepeat
    @Test
    fun testFindByGithubWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.findByGithubProfile(github) }
                .operationError()
    }

    @DontRepeat
    @Test
    fun testFindByGithubWhenUserDoesNotExist()
    {
        val sql = Queries.SELECT_USER_BY_GITHUB

        whenever(database.queryForObject(sql, serializer, github))
                .thenThrow(EmptyResultDataAccessException::class.java)

        assertThrows { instance.findByGithubProfile(github) }
                .isInstanceOf(UserDoesNotExistException::class.java)
    }

    @DontRepeat
    @Test
    fun testFindByGitHubWithBadArgs()
    {
        assertThrows { instance.findByGithubProfile("") }.invalidArg()
    }

    @Test
    fun testGetRecentlyCreated()
    {
        val sql = Queries.SELECT_RECENT_USERS
        val expected = CollectionGenerators.listOf(users(), 10)

        whenever(database.query(sql, serializer))
                .thenReturn(expected)

        val result = instance.recentlyCreatedUsers
        assertEquals(expected, result)
    }

    @DontRepeat
    @Test
    fun testGetRecentlyCreatedWhenDatabaseFails()
    {
        database.setupForFailure()

        val result = instance.recentlyCreatedUsers
        assertThat(result, isEmpty)
    }

    @DontRepeat
    @Test
    fun testGetRecentlyCreatedWhenNone()
    {
        val sql = Queries.SELECT_RECENT_USERS

        whenever(database.query(sql, serializer))
                .thenReturn(emptyList())

        val result = instance.recentlyCreatedUsers
        assertThat(result, isEmpty)
    }
}