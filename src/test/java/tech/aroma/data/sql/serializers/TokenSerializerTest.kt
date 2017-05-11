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

import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.asUUID
import tech.aroma.data.sql.toTimestamp
import tech.aroma.thrift.authentication.AuthenticationToken
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import java.lang.IllegalArgumentException

@RunWith(AlchemyTestRunner::class)
class TokenSerializerTest
{
    @Mock
    private lateinit var database: JdbcOperations

    private lateinit var instance: TokenSerializer

    @GeneratePojo
    private lateinit var token: AuthenticationToken

    @GenerateString(UUID)
    private lateinit var tokenId: String

    @GenerateString(UUID)
    private lateinit var ownerId: String

    @GenerateString(UUID)
    private lateinit var orgId: String

    @GenerateString
    private lateinit var statement: String


    @Before
    fun setUp()
    {
        token.tokenId = tokenId
        token.ownerId = ownerId
        token.organizationId = orgId

        instance = TokenSerializer()
    }


    @Test
    fun testSave()
    {
        instance.save(token, null, statement, database)

        verify(database).update(statement,
                                tokenId.asUUID(),
                                ownerId.asUUID(),
                                orgId.asUUID(),
                                token.ownerName,
                                token.timeOfCreation.toTimestamp(),
                                token.timeOfExpiration.toTimestamp(),
                                token.tokenType.toString(),
                                token.status.toString())
    }

    @DontRepeat
    @Test
    fun testSaveWithBadArgs()
    {
        assertThrows { instance.save(null, null, statement, database) }
                .isInstanceOf(IllegalArgumentException::class.java)

        assertThrows { instance.save(token, null, null, database) }
                .isInstanceOf(IllegalArgumentException::class.java)

        assertThrows { instance.save(token, null, "", database) }
                .isInstanceOf(IllegalArgumentException::class.java)

        assertThrows { instance.save(token, null, statement, null) }
                .isInstanceOf(IllegalArgumentException::class.java)

        assertThrows {
            val invalidToken = token.deepCopy().setTokenId(statement)
            instance.save(invalidToken, null, statement, database)
        }.isInstanceOf(IllegalArgumentException::class.java)

    }
}