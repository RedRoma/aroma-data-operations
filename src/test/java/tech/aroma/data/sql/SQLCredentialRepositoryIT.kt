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
import com.natpryce.hamkrest.equalTo
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.doesNotExist
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest
import tech.sirwellington.alchemy.generator.AlchemyGenerator
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.StringGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHANUMERIC
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
@IntegrationTest
class SQLCredentialRepositoryIT
{

    private companion object
    {
        @JvmStatic lateinit var database: JdbcOperations

        @JvmStatic
        @BeforeClass
        fun setupClass()
        {
            database = TestingResources.connectToDatabase()
        }
    }

    @GenerateString(UUID)
    private lateinit var userId: String

    @GenerateString(ALPHANUMERIC)
    private lateinit var encryptedPassword: String

    private lateinit var instance: SQLCredentialRepository

    @Before
    fun setUp()
    {

        setupData()
        setupMocks()

        instance = SQLCredentialRepository(database)
    }

    @After
    fun tearDown()
    {
        instance.deleteEncryptedPassword(userId)
    }

    @Test
    fun testSaveEncryptedPassword()
    {
        instance.saveEncryptedPassword(userId, encryptedPassword)

        assertTrue { instance.containsEncryptedPassword(userId) }
    }

    @Test
    fun testSaveEncryptedPasswordTwice()
    {
        instance.saveEncryptedPassword(userId, encryptedPassword)

        var result = instance.getEncryptedPassword(userId)
        assertThat(result, equalTo(encryptedPassword))

        val newPassword = one(hexadecimalString(20))
        instance.saveEncryptedPassword(userId, newPassword)

        result = instance.getEncryptedPassword(userId)
        assertThat(result, equalTo(newPassword))
    }

    @Test
    fun testContainsEncryptedPassword()
    {
        assertFalse { instance.containsEncryptedPassword(userId) }

        instance.saveEncryptedPassword(userId, encryptedPassword)

        assertTrue { instance.containsEncryptedPassword(userId) }
    }

    @Test
    fun testGetEncryptedPassword()
    {

        instance.saveEncryptedPassword(userId, encryptedPassword)

        val results = instance.getEncryptedPassword(userId)

        assertThat(results, equalTo(encryptedPassword))
    }

    @DontRepeat
    @Test
    fun testGetEncryptedPasswordWhenNotExists()
    {
        assertThrows { instance.getEncryptedPassword(userId) }.doesNotExist()
    }

    @Test
    fun testDeleteEncryptedPassword()
    {
        instance.saveEncryptedPassword(userId, encryptedPassword)

        instance.deleteEncryptedPassword(userId)

        assertFalse { instance.containsEncryptedPassword(userId) }
    }

    @Test
    fun testDeleteEncryptedPasswordWhenNotExists()
    {
        instance.deleteEncryptedPassword(userId)
    }

    private fun setupData()
    {

    }

    private fun setupMocks()
    {

    }

}