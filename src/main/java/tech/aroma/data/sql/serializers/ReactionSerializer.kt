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

import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.assertions.RequestAssertions.validReaction
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.data.sql.serializers.Tables.Reactions
import tech.aroma.thrift.reactions.Reaction
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import tech.sirwellington.alchemy.thrift.ThriftObjects
import java.sql.ResultSet


/**
 *
 * @author SirWellington
 */
internal class ReactionSerializer : DatabaseSerializer<Reaction>
{
    override fun save(reaction: Reaction, statement: String, database: JdbcOperations)
    {
        checkThat(reaction).`is`(validReaction())
        checkThat(statement).`is`(nonEmptyString())
    }

    override fun deserialize(row: ResultSet): Reaction
    {
        val reaction = Reaction()

        val serializedReaction = row.getString(Reactions.SERIALIZED_REACTION) ?: return reaction

        return ThriftObjects.fromJson(reaction, serializedReaction)

    }

}