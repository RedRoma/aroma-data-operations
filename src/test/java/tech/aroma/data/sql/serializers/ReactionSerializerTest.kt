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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.notNull
import tech.aroma.data.sql.serializers.Tables.Reactions
import tech.aroma.thrift.generators.ReactionGenerators.reactions
import tech.aroma.thrift.reactions.Reaction
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.thrift.ThriftObjects
import java.sql.ResultSet

@RunWith(AlchemyTestRunner::class)
class ReactionSerializerTest
{

    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var row: ResultSet

    @GenerateString
    private lateinit var sql: String

    private lateinit var reaction: Reaction
    private val serializedReaction get() = ThriftObjects.toJson(reaction)

    private lateinit var instance: ReactionSerializer

    @Before
    fun setUp()
    {
        setupData()
        setupMocks()
        instance = ReactionSerializer()
    }

    @Test
    fun testSave()
    {
        instance.save(reaction, sql, database)

        verifyZeroInteractions(database)
    }

    @DontRepeat
    @Test
    fun testSaveWithBadArgs()
    {
        assertThrows {
            instance.save(reaction, "", database)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testDeserialize()
    {
        val result = instance.deserialize(row)
        assertThat(result, equalTo(reaction))
    }

    @DontRepeat
    @Test
    fun testDeserializeWhenColumnIsEmpty()
    {
        whenever(row.getString(Reactions.SERIALIZED_REACTION))
                .thenReturn("")

        val result = instance.deserialize(row)
        assertThat(result, notNull)
    }

    private fun setupData()
    {
        reaction = one(reactions())
    }

    private fun setupMocks()
    {
        whenever(row.getString(Reactions.SERIALIZED_REACTION)).thenReturn(serializedReaction)
    }

}