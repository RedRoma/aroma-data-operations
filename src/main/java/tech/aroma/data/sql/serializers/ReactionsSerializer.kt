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

package tech.aroma.data.sql.serializers

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.assertions.RequestAssertions.validReaction
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.data.sql.serializers.Columns.Reactions
import tech.aroma.thrift.reactions.Reaction
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.*
import tech.sirwellington.alchemy.thrift.ThriftObjects
import java.sql.ResultSet


/**
 *
 * @author SirWellington
 */
internal class ReactionsSerializer : DatabaseSerializer<MutableList<Reaction>>
{

    private companion object
    {
        @JvmStatic val LOG = LoggerFactory.getLogger(this::class.java)!!
    }

    override fun save(reactions: MutableList<Reaction>, statement: String, database: JdbcOperations)
    {
        reactions.forEach { checkThat(it).isA(validReaction()) }
        checkThat(statement).isA(nonEmptyString())
    }

    override fun deserialize(row: ResultSet): MutableList<Reaction>
    {

        val array = row.getArray(Reactions.SERIALIZED_REACTIONS)?.array as? Array<*> ?: return mutableListOf()

        return array.filterNotNull()
                .map { it.toString() }
                .map(this::reactionFromString)
                .filterNotNull()
                .toMutableList()

    }

    private fun reactionFromString(string: String): Reaction?
    {
        val prototype = Reaction()

        return try
        {
            ThriftObjects.fromJson(prototype, string)
        }
        catch (ex: Exception)
        {
            LOG.warn("Failed to deserialize reaction from $string", ex)
            return null
        }
    }
}