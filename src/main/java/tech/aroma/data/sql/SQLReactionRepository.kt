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

package tech.aroma.data.sql

import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.ReactionRepository
import tech.aroma.data.assertions.RequestAssertions.*
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.OperationFailedException
import tech.aroma.thrift.reactions.Reaction
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.thrift.ThriftObjects
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
internal class SQLReactionRepository
@Inject constructor(val database: JdbcOperations, val serializer: DatabaseSerializer<MutableList<Reaction>>) : ReactionRepository
{

    private companion object
    {
        @JvmStatic val LOG = LoggerFactory.getLogger(this::class.java)!!
    }

    override fun saveReactionsForUser(userId: String, reactions: List<Reaction>)
    {
        checkUserId(userId)
        saveReactionsForOwner(userId, reactions)
    }


    override fun getReactionsForUser(userId: String): MutableList<Reaction>
    {
        checkUserId(userId)

        return getReactionsForOwner(userId)
    }


    override fun saveReactionsForApplication(appId: String, reactions: List<Reaction>)
    {
        checkAppId(appId)

        saveReactionsForOwner(appId, reactions)
    }

    override fun getReactionsForApplication(appId: String): MutableList<Reaction>
    {
        checkAppId(appId)

        return getReactionsForOwner(appId)
    }

    private fun getReactionsForOwner(ownerId: String): MutableList<Reaction>
    {
        val sql = Queries.SELECT_REACTION

        return try
        {
            database.queryForObject(sql, serializer, ownerId.toUUID()) ?: mutableListOf()
        }
        catch (ex: EmptyResultDataAccessException)
        {
            return mutableListOf()
        }
        catch (ex: Exception)
        {
            val message = "Failed to query reactions for reactions belonging to [$ownerId]"
            failWithMessage(message, ex)
        }
    }

    private fun saveReactionsForOwner(ownerId: String, reactions: List<Reaction>)
    {
        checkReactions(reactions)
        clearReactionsForOwner(ownerId)

        val sql = Inserts.REACTION
        val serialized = try
        {
            reactions.map(ThriftObjects::toJson)
                    .filter { !it.isNullOrEmpty() }
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to serialized Reactions", ex)
        }

        try
        {
            database.update(sql, { statement ->
                val array = statement.connection.createArrayOf("text", serialized.toTypedArray())
                statement.setObject(1, ownerId.toUUID())
                statement.setArray(2, array)
            })
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to save reactions", ex)
        }
    }

    private fun clearReactionsForOwner(ownerId: String)
    {
        val sql = Deletes.REACTIONS

        try
        {
            database.update(sql, ownerId.toUUID())
        }
        catch(ex: Exception)
        {
            LOG.warn("Failed to clear Reactions for [$ownerId]")
        }
    }

    private fun checkReactions(reactions: List<Reaction>)
    {
        reactions.forEach {

            checkThat(it)
                    .throwing(InvalidArgumentException::class.java)
                    .`is`(validReaction())
        }

    }
}