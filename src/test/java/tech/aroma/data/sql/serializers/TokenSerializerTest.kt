package tech.aroma.data.sql.serializers

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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.AromaGenerators
import tech.aroma.data.sql.serializers.Columns.Tokens
import tech.aroma.data.sql.toTimestamp
import tech.aroma.data.sql.toUUID
import tech.aroma.thrift.authentication.AuthenticationToken
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import java.lang.IllegalArgumentException
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.test.assertEquals

@RunWith(AlchemyTestRunner::class)
class TokenSerializerTest
{
    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var results: ResultSet

    private lateinit var instance: TokenSerializer

    private lateinit var token: AuthenticationToken
    private lateinit var tokenId: String
    private lateinit var ownerId: String
    private lateinit var orgId: String

    @GenerateString
    private lateinit var statement: String


    @Before
    fun setUp()
    {
        token = AromaGenerators.Tokens.token
        tokenId = token.tokenId
        ownerId = token.ownerId
        orgId = token.organizationId

        results.prepareWith(token)

        instance = TokenSerializer()
    }


    @Test
    fun testSave()
    {
        instance.save(token, statement, database)

        verify(database).update(statement,
                                tokenId.toUUID(),
                                ownerId.toUUID(),
                                orgId.toUUID(),
                                token.ownerName,
                                token.timeOfCreation.toTimestamp(),
                                token.timeOfExpiration.toTimestamp(),
                                token.tokenType.toString(),
                                token.status.toString())
    }

    @DontRepeat
    @Test
    fun testSaveWhenDatabaseFails()
    {
        whenever(database.update(any(), Mockito.anyVararg<Any>()))
                .thenThrow(EmptyResultDataAccessException::class.java)

        assertThrows { instance.save(token, statement, database) }
                .isInstanceOf(DataAccessException::class.java)
    }

    @DontRepeat
    @Test
    fun testSaveWithBadArgs()
    {

        assertThrows { instance.save(token, "", database) }
                .isInstanceOf(IllegalArgumentException::class.java)

        assertThrows {
            val invalidToken = token.deepCopy().setTokenId(statement)
            instance.save(invalidToken, statement, database)
        }.isInstanceOf(IllegalArgumentException::class.java)

    }

    @Test
    fun testDeserialize()
    {
        val result = instance.deserialize(results)
        assertEquals(token, result)
    }

    @DontRepeat
    @Test
    fun testDeserializeWhenColumnNoPresent()
    {
        whenever(results.getString(any<String>()))
                .thenThrow(SQLException())

        assertThrows { instance.deserialize(results) }
                .isInstanceOf(SQLException::class.java)
    }

    private fun ResultSet.prepareWith(token: AuthenticationToken)
    {
        whenever(this.getString(Tokens.TOKEN_ID)).thenReturn(tokenId)
        whenever(this.getString(Tokens.OWNER_ID)).thenReturn(ownerId)
        whenever(this.getString(Tokens.ORG_ID)).thenReturn(orgId)
        whenever(this.getString(Tokens.OWNER_NAME)).thenReturn(token.ownerName)
        whenever(this.getTimestamp(Tokens.TIME_OF_CREATION)).thenReturn(token.timeOfCreation.toTimestamp())
        whenever(this.getTimestamp(Tokens.TIME_OF_EXPIRATION)).thenReturn(token.timeOfExpiration.toTimestamp())
        whenever(this.getString(Tokens.TOKEN_TYPE)).thenReturn(token.tokenType.toString())
        whenever(this.getString(Tokens.TOKEN_STATUS)).thenReturn(token.status.toString())
    }
}