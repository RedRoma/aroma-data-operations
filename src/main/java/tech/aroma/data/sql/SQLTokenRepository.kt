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
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.assertions.AromaAssertions.legalToken
import tech.aroma.thrift.authentication.AuthenticationToken
import tech.aroma.thrift.exceptions.*
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
class SQLTokenRepository
@Inject constructor(val database: JdbcOperations, val serializer: DatabaseSerializer<AuthenticationToken>)
    : TokenRepository
{

    companion object
    {
        @JvmStatic private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    override fun containsToken(tokenId: String?): Boolean
    {
        val tokenId = checkTokenId(tokenId)

        val query = Queries.CHECK_TOKEN

        return try
        {
            database.queryForObject(query, Boolean::class.java, tokenId.toUUID())
        }
        catch (ex: Exception)
        {
            handleErrorFor(tokenId)(ex)
        }
    }


    override fun getToken(tokenId: String?): AuthenticationToken
    {
        val tokenId = checkTokenId(tokenId)
        val query = Queries.SELECT_TOKEN

        return try
        {
            database.queryForObject(query, serializer, tokenId.toUUID())
        }
        catch (ex: Exception)
        {
            handleErrorFor(tokenId)(ex)
        }

    }

    override fun saveToken(token: AuthenticationToken?)
    {
        checkThat(token)
                .throwing(InvalidArgumentException::class.java)
                .`is`(legalToken())

        val tokenId = checkTokenId(token?.tokenId)


        val statement = Inserts.TOKEN

        try
        {
            serializer.save(token!!, statement, database)
        }
        catch (ex: Exception)
        {
            handleErrorFor(tokenId)(ex)
        }
    }

    override fun getTokensBelongingTo(ownerId: String?): MutableList<AuthenticationToken>
    {
        checkThat(ownerId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(nonEmptyString())
                .`is`(validUUID())

        val ownerId = ownerId!!
        val query = Queries.SELECT_TOKENS_FOR_OWNER

        return try
        {
            database.query(query, serializer, ownerId.toUUID())
        }
        catch (ex: Exception)
        {
            handleErrorFor(ownerId)(ex)
        }
    }

    override fun deleteToken(tokenId: String?)
    {
        val tokenId = checkTokenId(tokenId)
        val statement = Deletes.TOKEN

        try
        {
            database.update(statement, tokenId.toUUID())
        }
        catch (ex: Exception)
        {
            handleErrorFor(tokenId)(ex)
        }
    }



    private fun checkTokenId(tokenId: String?): String
    {
        checkThat(tokenId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(nonEmptyString())
                .`is`(validUUID())

        return tokenId!!
    }

    private fun handleErrorFor(token: String): ((Exception) -> Nothing)
    {

        return { ex ->

            LOG.error("Failed to execute SQL for token $token", ex)

            if (ex is EmptyResultDataAccessException)
            {
                throw InvalidTokenException("Token does not exist: [$token] | ${ex.message}")
            }

            throw OperationFailedException("Error operating on Token: [$token] | ${ex.message}")
        }

    }
}