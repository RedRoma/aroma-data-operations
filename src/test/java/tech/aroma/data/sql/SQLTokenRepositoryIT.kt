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
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.springframework.jdbc.core.JdbcOperations
import sir.wellington.alchemy.collections.sets.Sets
import tech.aroma.data.AromaGenerators.Tokens
import tech.aroma.data.sql.serializers.TokenSerializer
import tech.aroma.thrift.authentication.AuthenticationToken
import tech.aroma.thrift.exceptions.InvalidTokenException
import tech.sirwellington.alchemy.generator.CollectionGenerators.listOf
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
class SQLTokenRepositoryIT
{
    private companion object
    {

        private lateinit var database: JdbcOperations

        @JvmStatic
        @BeforeClass
        fun prepareClass()
        {
            database = TestingResources.connectToDatabase()
        }

    }

    @GeneratePojo
    private lateinit var token: AuthenticationToken

    private lateinit var tokenId: String
    private val tokenUuid get() = tokenId.toUUID()
    private lateinit var ownerId: String
    private lateinit var orgId: String

    private val serializer = TokenSerializer()

    private lateinit var instance: SQLTokenRepository

    @Before
    fun setUp()
    {
        token = Tokens.token
        tokenId = token.tokenId
        orgId = token.organizationId
        ownerId = token.ownerId

        instance = SQLTokenRepository(database, serializer)
    }

    @After
    fun tearDown()
    {
        try
        {
            instance.deleteTokensBelongingTo(ownerId)
            instance.deleteToken(tokenId)
        }
        catch(ex: Exception)
        {
            ex.printStackTrace()
        }

    }

    @Test
    fun testContainsToken()
    {
        assertFalse { instance.containsToken(tokenId) }

        instance.saveToken(token)

        assertTrue { instance.containsToken(tokenId) }
    }

    @Test
    fun testSave()
    {
        instance.saveToken(token)

        assertTrue { instance.containsToken(tokenId) }
    }

    @Test
    fun testGet()
    {
        instance.saveToken(token)

        val result = instance.getToken(tokenId)

        assertEquals(token, result)
    }

    @Test
    fun testGetTokenWhenNotExists()
    {
        assertThrows {
            instance.getToken(tokenId)
        }.isInstanceOf(InvalidTokenException::class.java)
    }


    @Test
    fun testGetTokensBelongingToWhenNone()
    {
        val result = instance.getTokensBelongingTo(ownerId)

        assertThat(result, isEmpty)
    }

    @Test
    fun testGetTokensBelongingTo()
    {
        val tokens = createTokensFor(ownerId)

        tokens.forEach(instance::saveToken)

        val result = instance.getTokensBelongingTo(ownerId)

        assertEquals(tokens.asSet(), result.asSet())
    }

    @Test
    fun testDeleteTokenWhenDoesNotExist()
    {
        instance.deleteToken(tokenId)
    }

    @Test
    fun testDeleteTokenWhenExists()
    {
        instance.saveToken(token)

        instance.deleteToken(tokenId)

        assertFalse { instance.containsToken(tokenId) }
    }

    private fun createTokensFor(owner: String): List<AuthenticationToken>
    {
        return listOf { Tokens.token.setOwnerId(owner) }
    }

    private inline fun <T : Any> List<T>.asSet(): Set<T>
    {
        return Sets.copyOf(this)
    }
}