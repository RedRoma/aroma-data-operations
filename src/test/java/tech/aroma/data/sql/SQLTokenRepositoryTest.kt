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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.jdbc.core.JdbcOperations
import sir.wellington.alchemy.collections.lists.Lists
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.authentication.AuthenticationToken
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.OperationFailedException
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.BooleanGenerators.booleans
import tech.sirwellington.alchemy.generator.CollectionGenerators.listOf
import tech.sirwellington.alchemy.generator.ObjectGenerators.pojos
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID

@RunWith(AlchemyTestRunner::class)
@Repeat
class SQLTokenRepositoryTest
{
    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var serializer: DatabaseSerializer<AuthenticationToken>

    @GeneratePojo
    private lateinit var token: AuthenticationToken

    @GenerateString(UUID)
    private lateinit var tokenId: String

    private val tokenUuid get() = tokenId.toUUID()

    @GenerateString(UUID)
    private lateinit var ownerId: String

    @GenerateString(UUID)
    private lateinit var orgId: String

    @GenerateString
    private lateinit var badTokenId: String

    private lateinit var instance: SQLTokenRepository

    @Before
    fun setUp()
    {
        token.tokenId = tokenId
        token.organizationId = orgId
        token.ownerId = ownerId

        instance = SQLTokenRepository(database, serializer)
    }


    @Test
    fun testContainsToken()
    {
        val query = Queries.CHECK_TOKEN
        val expected = one(booleans())

        whenever(database.queryForObject(query, Boolean::class.java, tokenUuid))
                .thenReturn(expected)

        val result = instance.containsToken(tokenId)

        assertEquals(expected, result)
    }

    @DontRepeat
    @Test
    fun testContainsTokenWithBadArgs()
    {
        assertThrows { instance.containsToken(null) }.invalidArg()
        assertThrows { instance.containsToken("") }.invalidArg()
        assertThrows { instance.containsToken(badTokenId) }.invalidArg()
    }

    @Test
    fun testContainsTokenWhenDatabaseFails()
    {
        whenever(database.queryForObject(any<String>(), eq(Boolean::class.java), eq(tokenUuid)))
                .thenThrow(UncategorizedSQLException::class.java)

        assertThrows { instance.containsToken(tokenId) }
                .isInstanceOf(OperationFailedException::class.java)
    }

    @Test
    fun testGetToken()
    {
        val query = Queries.SELECT_TOKEN

        whenever(database.queryForObject(query, serializer, tokenUuid))
                .thenReturn(token)

        val result = instance.getToken(tokenId)

        assertEquals(token, result)
    }

    @DontRepeat
    @Test
    fun testGetTokenWithBadArgs()
    {
        assertThrows { instance.getToken(null) }.invalidArg()
        assertThrows { instance.getToken("") }.invalidArg()
        assertThrows { instance.getToken(badTokenId) }.invalidArg()
    }

    @Test
    fun testGetTokenWhenDatabaseFails()
    {
        val query = Queries.SELECT_TOKEN

        whenever(database.queryForObject(query, serializer, tokenUuid))
                .thenThrow(UncategorizedSQLException::class.java)

        assertThrows { instance.getToken(tokenId) }
                .isInstanceOf(OperationFailedException::class.java)
    }

    @Test
    fun testSaveToken()
    {
        val statement = Inserts.TOKEN

        instance.saveToken(token)

        verify(serializer).save(token, statement, database)
    }

    @DontRepeat
    @Test
    fun testSaveTokenWithBadArgs()
    {
        assertThrows { instance.saveToken(null) }.invalidArg()

        assertThrows {
            val emptyToken = AuthenticationToken()
            instance.saveToken(emptyToken)

        }.invalidArg()

        assertThrows {
            val invalidToken = token.deepCopy().setTokenId(badTokenId)
            instance.saveToken(invalidToken)
        }
    }

    @DontRepeat
    @Test
    fun testSaveTokenWhenDatabaseFails()
    {
        val sql = Inserts.TOKEN

        doThrow(UncategorizedSQLException::class)
                .whenever(serializer)
                .save(token, sql, database)

        assertThrows { instance.saveToken(token) }
                .isInstanceOf(OperationFailedException::class.java)
    }

    @Test
    fun testGetTokensBelongingTo()
    {
        val query = Queries.SELECT_TOKENS_FOR_OWNER
        val tokens = listOf(pojos(AuthenticationToken::class.java))

        whenever(database.query(query, serializer, ownerId.toUUID()))
                .thenReturn(tokens)

        val result = instance.getTokensBelongingTo(ownerId)

        assertEquals(tokens, result)
    }

    @DontRepeat
    @Test
    fun testGetTokensBelongingToWithBadArgs()
    {
        assertThrows { instance.getToken(null) }.invalidArg()
        assertThrows { instance.getToken("") }.invalidArg()
        assertThrows { instance.getToken(badTokenId) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testGetTokensBelongingToWhenDatabaseFails()
    {
        val query = Queries.SELECT_TOKENS_FOR_OWNER

        whenever(database.query(query, serializer, ownerId.toUUID()))
                .thenThrow(UncategorizedSQLException::class.java)

        assertThrows { instance.getTokensBelongingTo(ownerId) }
                .isInstanceOf(OperationFailedException::class.java)
    }

    @Test
    fun testDeleteToken()
    {
        val statement = Deletes.TOKEN

        instance.deleteToken(tokenId)

        verify(database).update(statement, tokenUuid)
    }

    @DontRepeat
    @Test
    fun testDeleteTokenWithBadArgs()
    {
        assertThrows { instance.deleteToken(null) }.invalidArg()
        assertThrows { instance.deleteToken("") }.invalidArg()
        assertThrows { instance.deleteToken(badTokenId) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testDeleteTokenWhenDatabaseFails()
    {
        val sql = Deletes.TOKEN

        whenever(database.update(sql, tokenUuid))
                .thenThrow(UncategorizedSQLException::class.java)

        assertThrows { instance.deleteToken(tokenId) }
                .isInstanceOf(OperationFailedException::class.java)

    }

    @Test
    fun testDeleteTokensBelongingTo()
    {
        val query = Queries.SELECT_TOKENS_FOR_OWNER

        whenever(database.query(query, serializer, ownerId.toUUID()))
                .thenReturn(Lists.createFrom(token))

        instance.deleteTokensBelongingTo(ownerId)

        val statementToDelete = Deletes.TOKEN
        verify(database).update(statementToDelete, tokenUuid)
    }

    @DontRepeat
    @Test
    fun testDeleteTokensBelongingToWhenDatabaseFails()
    {
        val query = Queries.SELECT_TOKENS_FOR_OWNER

        whenever(database.query(query, serializer, ownerId.toUUID()))
                .thenThrow(UncategorizedSQLException::class.java)

        assertThrows { instance.deleteTokensBelongingTo(ownerId) }
                .isInstanceOf(OperationFailedException::class.java)
    }

}