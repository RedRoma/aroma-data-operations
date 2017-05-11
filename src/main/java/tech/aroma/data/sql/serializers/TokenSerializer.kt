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
import tech.aroma.data.sql.*
import tech.aroma.data.sql.serializers.Tables.Tokens
import tech.aroma.thrift.assertions.AromaAssertions.legalToken
import tech.aroma.thrift.authentication.*
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID
import java.sql.ResultSet
import java.time.Duration


/**
 *
 * @author SirWellington
 */
internal class TokenSerializer : DatabaseSerializer<AuthenticationToken>
{
    override fun save(`object`: AuthenticationToken, timeToLive: Duration?, statement: String, database: JdbcOperations)
    {
        checkThat(statement).`is`(nonEmptyString())
        checkThat(`object`).`is`(legalToken())
        checkThat(`object`.tokenId).`is`(validUUID())

        val token = `object`

        database.update(statement,
                        token.tokenId.asUUID(),
                        token.ownerId.asUUID(),
                        token.organizationId.asUUID(),
                        token.ownerName,
                        token.timeOfCreation.toTimestamp(),
                        token.timeOfExpiration.toTimestamp(),
                        token.tokenType?.toString(),
                        token.status?.toString())
    }

    override fun deserialize(resultSet: ResultSet): AuthenticationToken
    {
        val token = AuthenticationToken()
        val results = resultSet

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