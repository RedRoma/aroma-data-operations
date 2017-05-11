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

import org.junit.Before
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.test.junit.runners.*

import org.junit.Assert.*
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.TokenRepository
import tech.aroma.thrift.authentication.AuthenticationToken
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID

@RunWith(AlchemyTestRunner::class)
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

    @GenerateString(UUID)
    private lateinit var ownerId: String

    @GenerateString(UUID)
    private lateinit var orgId: String

    private lateinit var instance: SQLTokenRepository

    @Before
    fun setUp()
    {
        instance = SQLTokenRepository(database, serializer)
    }



}