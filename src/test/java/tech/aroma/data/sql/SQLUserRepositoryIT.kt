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

import junit.framework.TestResult
import org.apache.thrift.TException
import org.junit.*
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.test.junit.runners.*

import org.junit.Assert.*
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.serializers.UserSerializer
import tech.aroma.thrift.User
import tech.aroma.thrift.generators.UserGenerators.users
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
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
}