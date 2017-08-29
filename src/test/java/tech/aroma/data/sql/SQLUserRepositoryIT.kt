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
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.isEmpty
import org.apache.thrift.TException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.serializers.UserSerializer
import tech.aroma.thrift.User
import tech.aroma.thrift.exceptions.UserDoesNotExistException
import tech.aroma.thrift.generators.UserGenerators.users
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.one
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
class SQLUserRepositoryIT
{

    private companion object
    {
        @JvmStatic
        lateinit var database: JdbcOperations

        @JvmStatic
        @BeforeClass
        fun setupClass()
        {
            database = TestingResources.connectToDatabase()
        }
    }

    private val serializer = UserSerializer()

    private lateinit var user: User
    private lateinit var userId: String

    private lateinit var instance: SQLUserRepository

    @Before
    fun setup()
    {
        instance = SQLUserRepository(database, serializer)

        user = one(users())
        userId = user.userId
    }

    @After
    fun cleanUp()
    {
        try
        {
            instance.deleteUser(userId)
        }
        catch (ex: TException)
        {
            println("Could not delete user $userId | ${ex.message}")
        }
    }

    @Test
    fun testSave()
    {
        instance.saveUser(user)

        assertTrue { instance.containsUser(userId) }
    }


    @Test
    fun testGetUser()
    {
        instance.saveUser(user)
        assertTrue { instance.containsUser(userId) }

        val result = instance.getUser(userId)

        assertEquals(user, result)
    }

    @Test
    fun testGetUserWhenNoUser()
    {
        assertThrows {
            instance.getUser(userId)
        }.isInstanceOf(UserDoesNotExistException::class.java)
    }

    @Test
    fun testDeleteUser()
    {
        instance.saveUser(user)
        assertTrue { instance.containsUser(userId) }

        instance.deleteUser(userId)
        assertFalse { instance.containsUser(userId) }
    }

    @Test
    fun testDeleteUserWhenNoUser()
    {
        assertFalse { instance.containsUser(userId) }
        instance.deleteUser(userId)
        assertFalse { instance.containsUser(userId) }
    }

    @Test
    fun testContainsUser()
    {
        assertFalse { instance.containsUser(userId) }

        instance.saveUser(user)
        assertTrue { instance.containsUser(userId) }

        instance.deleteUser(userId)
        assertFalse { instance.containsUser(userId) }
    }

    @Test
    fun testGetUserByEmail()
    {
        instance.saveUser(user)

        val email = user.email

        val result = instance.getUserByEmail(email)
        assertEquals(user, result)
    }

    @Test
    fun testGetUserByEmailWhenNotExists()
    {
        val email = user.email

        assertFalse { instance.containsUser(userId) }
        assertThrows { instance.getUserByEmail(email) }
    }

    @Test
    fun testGetUserByGithub()
    {
        val github = one(alphabeticStrings())
        user.githubProfile = github

        instance.saveUser(user)
        assertTrue { instance.containsUser(userId) }

        val result = instance.findByGithubProfile(github)

        assertEquals(user, result)
    }

    fun testGetUserByGithubWhenNotExists()
    {
        val github = one(alphabeticStrings())

        assertThrows { instance.findByGithubProfile(github) }
                .isInstanceOf(UserDoesNotExistException::class.java)
    }

    @Test
    fun testGetRecentlyCreatedUsers()
    {
        instance.saveUser(user)

        val recent = instance.recentlyCreatedUsers

        assertThat(recent, hasElement(user))
    }

    @Test
    fun testGetRecentlyCreatedWhenRemoved()
    {
        instance.saveUser(user)
        instance.deleteUser(userId)

        val recent = instance.recentlyCreatedUsers
        assertThat(recent, !hasElement(user))
    }

    @Test
    fun testGetRecentlyCreatedUsersWhenNone()
    {
        val recent = instance.recentlyCreatedUsers

        assertThat(recent, isEmpty)
    }
}