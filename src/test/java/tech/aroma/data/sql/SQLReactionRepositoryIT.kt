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

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.*
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.test.junit.runners.*

import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.SQLStatements.Deletes
import tech.aroma.data.sql.serializers.ReactionsSerializer
import tech.aroma.thrift.generators.ReactionGenerators
import tech.aroma.thrift.reactions.Reaction
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID

@RunWith(AlchemyTestRunner::class)
class SQLReactionRepositoryIT
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
    private lateinit var ownerId: String

    private lateinit var reactions: List<Reaction>

    private val serializer = ReactionsSerializer()
    private lateinit var instance: SQLReactionRepository


    @Before
    fun setUp()
    {

        setupData()
        setupMocks()

        instance = SQLReactionRepository(database, serializer)
    }

    @After
    fun tearDown()
    {
        val delete = Deletes.REACTIONS

        try
        {
            database.update(delete, ownerId.toUUID())
        }
        catch (ex: Exception)
        {
            print(ex)
        }
    }

    @Test
    fun testSaveReactionsForUser()
    {
        instance.saveReactionsForUser(ownerId, reactions)

        val result = instance.getReactionsForUser(ownerId)
        assertThat(result, notNull)
        assertThat(result, notEmpty)
        assertThat(result.size, equalTo(reactions.size))
    }

    @Test
    fun testGetReactionsForUser()
    {
        instance.saveReactionsForApplication(ownerId, reactions)

        val result = instance.getReactionsForUser(ownerId)
        assertThat(result, equalTo(reactions))
    }

    @Test
    fun testGetReactionsForUserWhenNone()
    {
        val result = instance.getReactionsForUser(ownerId)

        assertThat(result, notNull)
        assertThat(result, isEmpty)
    }

    @Test
    fun testSaveReactionsForApplication()
    {
        instance.saveReactionsForApplication(ownerId, reactions)

        val result = instance.getReactionsForApplication(ownerId)
        assertThat(result, notEmpty)
        assertThat(result.size, equalTo(reactions.size))
    }

    @Test
    fun testGetReactionsForApplication()
    {
        instance.saveReactionsForApplication(ownerId, reactions)

        val result = instance.getReactionsForApplication(ownerId)

        assertThat(result, notEmpty and equalTo(reactions))
    }

    @Test
    fun testGetReactionsForApplicationWhenEmpty()
    {
        val result = instance.getReactionsForApplication(ownerId)
        assertThat(result, notNull and isEmpty)
    }

    private fun setupData()
    {
        reactions = CollectionGenerators.listOf(ReactionGenerators.reactions(), 20)
    }

    private fun setupMocks()
    {

    }

}