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

import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.User
import tech.aroma.thrift.generators.UserGenerators.users
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.BooleanGenerators.booleans
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC
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

        verify(serializer).save(user, null, sql, database)
        verifyZeroInteractions(database)
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

    @Test
    fun testDeleteUser()
    {
        val sql = Deletes.USER

        instance.deleteUser(userId)

        verify(database).update(sql, userId.toUUID())
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


    @Test
    fun testFindByGithub()
    {
        val sql = Queries.SELECT_USER_BY_GITHUB

        whenever(database.queryForObject(sql, serializer, github))
                .thenReturn(user)

        val result = instance.findByGithubProfile(github)

        assertEquals(user, result)
    }
}