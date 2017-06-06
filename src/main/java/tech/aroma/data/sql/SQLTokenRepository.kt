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
import tech.aroma.data.TokenRepository
import tech.aroma.data.sql.SQLStatements.Deletes
import tech.aroma.data.sql.SQLStatements.Inserts
import tech.aroma.data.sql.SQLStatements.Queries
import tech.aroma.thrift.assertions.AromaAssertions.legalToken
import tech.aroma.thrift.authentication.AuthenticationToken
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.InvalidTokenException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.*
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
class SQLTokenRepository
@Inject constructor(val database: JdbcOperations,
                    val serializer: DatabaseSerializer<AuthenticationToken>): TokenRepository
{

    companion object
    {
        @JvmStatic private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override fun containsToken(tokenId: String): Boolean
    {
        val tokenId = checkTokenId(tokenId)

        val query = Queries.CHECK_TOKEN

        return try
        {
            database.queryForObject(query, Boolean::class.java, tokenId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not check if token exists: [$tokenId]", ex)
        }
    }


    override fun getToken(tokenId: String): AuthenticationToken
    {
        val tokenId = checkTokenId(tokenId)
        val query = Queries.SELECT_TOKEN

        return try
        {
            database.queryForObject(query, serializer, tokenId.toUUID())
        }
        catch (ex: EmptyResultDataAccessException)
        {
            val message = "Token does not exist: [$tokenId]"
            LOG.warn(message, ex)
            throw InvalidTokenException(message)
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not get token with ID [$tokenId]", ex)
        }

    }

    override fun saveToken(token: AuthenticationToken?)
    {
        checkThat(token)
                .throwing(InvalidArgumentException::class.java)
                .isA(legalToken())

        checkThat(token!!.tokenId)
                .throwing(InvalidArgumentException::class.java)
                .usingMessage("Token is invalid")
                .isA(validUUID())

        val statement = Inserts.TOKEN

        try
        {
            serializer.save(token, statement, database)
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to save Token [$token]", ex)
        }
    }

    override fun getTokensBelongingTo(ownerId: String): MutableList<AuthenticationToken>
    {
        checkThat(ownerId)
                .throwing(InvalidArgumentException::class.java)
                .isA(nonEmptyString())
                .isA(validUUID())

        val query = Queries.SELECT_TOKENS_FOR_OWNER

        return try
        {
            database.query(query, serializer, ownerId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not get tokens belonging to [$ownerId]", ex)
        }
    }

    override fun deleteToken(tokenId: String)
    {
        val tokenId = checkTokenId(tokenId)
        val statement = Deletes.TOKEN

        try
        {
            database.update(statement, tokenId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not remove token [$tokenId]", ex)
        }
    }


    private fun checkTokenId(tokenId: String): String
    {
        checkThat(tokenId)
                .throwing(InvalidArgumentException::class.java)
                .isA(nonEmptyString())
                .isA(validUUID())

        return tokenId
    }

}