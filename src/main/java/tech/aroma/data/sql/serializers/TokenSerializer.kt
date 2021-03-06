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
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.data.sql.serializers.Columns.Tokens
import tech.aroma.data.sql.toTimestamp
import tech.aroma.data.sql.toUUID
import tech.aroma.thrift.assertions.AromaAssertions.legalToken
import tech.aroma.thrift.authentication.AuthenticationToken
import tech.aroma.thrift.authentication.TokenStatus
import tech.aroma.thrift.authentication.TokenType
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.*
import java.sql.ResultSet


/**
 *
 * @author SirWellington
 */
internal class TokenSerializer : DatabaseSerializer<AuthenticationToken>
{
    override fun save(`object`: AuthenticationToken, statement: String, database: JdbcOperations)
    {
        checkThat(statement).isA(nonEmptyString())
        checkThat(`object`).isA(legalToken())
        checkThat(`object`.tokenId).isA(validUUID())

        val token = `object`

        database.update(statement,
                        token.tokenId.toUUID(),
                        token.ownerId.toUUID(),
                        token.organizationId.toUUID(),
                        token.ownerName,
                        token.timeOfCreation.toTimestamp(),
                        token.timeOfExpiration.toTimestamp(),
                        token.tokenType?.toString(),
                        token.status?.toString())
    }

    override fun deserialize(row: ResultSet): AuthenticationToken
    {
        val token = AuthenticationToken()
        val results = row

        val tokenId = results.getString(Tokens.TOKEN_ID)
        val ownerId = results.getString(Tokens.OWNER_ID)
        val orgId = results.getString(Tokens.ORG_ID)
        val ownerName = results.getString(Tokens.OWNER_NAME)
        val timeOfCreation = results.getTimestamp(Tokens.TIME_OF_CREATION)?.time
        val timeOfExpiration = results.getTimestamp(Tokens.TIME_OF_EXPIRATION)?.time
        val tokenType = results.getString(Tokens.TOKEN_TYPE)?.asTokenType()
        val tokenStatus = results.getString(Tokens.TOKEN_STATUS)?.asStatus()

        token.setTokenId(tokenId)
             .setOwnerId(ownerId)
             .setOrganizationId(orgId)
             .setOwnerName(ownerName)
             .setTokenType(tokenType)
             .setStatus(tokenStatus)

        if (timeOfCreation != null)
        {
            token.setTimeOfCreation(timeOfCreation)
        }

        if (timeOfExpiration != null)
        {
            token.setTimeOfExpiration(timeOfExpiration)
        }

        return token
    }

    private fun String.asTokenType(): TokenType?
    {
        return try
        {
            TokenType.valueOf(this)
        }
        catch (ex: Exception)
        {
            return null
        }
    }

    private fun String.asStatus(): TokenStatus?
    {
        return try
        {
            TokenStatus.valueOf(this)
        }
        catch (ex: Exception)
        {
            return null
        }
    }

}