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
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.SQLStatements.*
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.BooleanGenerators.booleans
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHANUMERIC
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID

@RunWith(AlchemyTestRunner::class)
class SQLCredentialRepositoryTest
{

    @Mock
    private lateinit var database: JdbcOperations

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

    @Test
    fun testSaveEncryptedPassword()
    {
        instance.saveEncryptedPassword(userId, encryptedPassword)

        val sql = Inserts.CREDENTIAL

        verify(database).update(sql, userId.toUUID(), encryptedPassword)
    }

    @Test
    fun testContainsEncryptedPassword()
    {
        val sql = Queries.CHECK_CREDENTIAL
        val expected = one(booleans())

        whenever(database.queryForObject(sql, Boolean::class.java, userId.toUUID()))
                .thenReturn(expected)

        val result = instance.containsEncryptedPassword(userId)

        assertThat(result, equalTo(expected))
    }

    @Test
    fun testGetEncryptedPassword()
    {
        val sql = Queries.SELECT_CREDENTIAL

        whenever(database.queryForObject(sql, String::class.java, userId.toUUID()))
                .thenReturn(encryptedPassword)

        val result = instance.getEncryptedPassword(userId)
        assertThat(result, equalTo(encryptedPassword))
    }

    @Test
    fun testDeleteEncryptedPassword()
    {
        val sql = Deletes.CREDENTIAL

        instance.deleteEncryptedPassword(userId)

        verify(database).update(sql, userId.toUUID())
    }

    private fun setupData()
    {

    }

    private fun setupMocks()
    {
    }

}