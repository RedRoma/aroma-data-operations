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
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.PreparedStatementSetter
import tech.aroma.data.sql.SQLStatements.Inserts
import tech.aroma.data.sql.SQLStatements.Queries
import tech.aroma.thrift.generators.ReactionGenerators
import tech.aroma.thrift.reactions.Reaction
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import tech.sirwellington.alchemy.thrift.ThriftObjects
import java.sql.Connection
import java.sql.PreparedStatement

@RunWith(AlchemyTestRunner::class)
class SQLReactionRepositoryTest
{
    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var serializer: DatabaseSerializer<List<Reaction>>

    @Mock
    private lateinit var preparedStatement: PreparedStatement

    @Mock
    private lateinit var connection: Connection

    @Mock
    private lateinit var sqlArray: java.sql.Array

    @Captor
    private lateinit var statementCaptor: ArgumentCaptor<PreparedStatementSetter>

    @GenerateString(UUID)
    private lateinit var ownerId: String

    private lateinit var reactions: List<Reaction>
    private val serializedReactions get() = reactions.map(ThriftObjects::toJson)

    private lateinit var instance: SQLReactionRepository

    @Before
    fun setUp()
    {

        setupData()
        setupMocks()

        instance = SQLReactionRepository(database, serializer)
    }

    @Test
    fun testSaveReactionsForUser()
    {
        instance.saveReactionsForUser(ownerId, reactions)

        verifyInsertOccurred()
    }



    @Test
    fun testGetReactionsForUser()
    {
        val result = instance.getReactionsForUser(ownerId)
        assertThat(result, equalTo(reactions))
    }

    @Test
    fun testSaveReactionsForApplication()
    {
        instance.saveReactionsForApplication(ownerId, reactions)

        verifyInsertOccurred()
    }

    @Test
    fun testGetReactionsForApplication()
    {
        val result = instance.getReactionsForApplication(ownerId)
        assertThat(result, equalTo(reactions))
    }

    private fun verifyInsertOccurred()
    {
        val sql = Inserts.REACTION

        verify(database).update(eq(sql), statementCaptor.capture())

        val statement = statementCaptor.value
        statement.setValues(preparedStatement)

        verify(preparedStatement).setObject(1, ownerId.toUUID())
        verify(preparedStatement).setArray(2, sqlArray)
    }

    private fun setupData()
    {
        reactions = CollectionGenerators.listOf(ReactionGenerators.reactions(), 10)

    }

    private fun setupMocks()
    {
        whenever(preparedStatement.connection).thenReturn(connection)

        whenever(connection.createArrayOf("text", serializedReactions.toTypedArray()))
                .thenReturn(sqlArray)

        whenever(database.queryForObject(Queries.SELECT_REACTION, serializer, ownerId.toUUID()))
                .thenReturn(reactions)
    }

}