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
import tech.aroma.data.assertions.RequestAssertions.validApplication
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.data.sql.serializers.Columns.Applications
import tech.aroma.data.sql.toCommaSeparatedList
import tech.aroma.data.sql.toTimestamp
import tech.aroma.data.sql.toUUID
import tech.aroma.thrift.Application
import tech.aroma.thrift.ProgrammingLanguage
import tech.aroma.thrift.Tier
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.*
import java.sql.ResultSet
import java.util.UUID


/**
 *
 * @author SirWellington
 */
internal class ApplicationSerializer : DatabaseSerializer<Application>
{
    private val LOG = LoggerFactory.getLogger(this.javaClass)

    override fun save(app: Application, statement: String, database: JdbcOperations)
    {
        checkThat(app).isA(validApplication())
        checkThat(statement).isA(nonEmptyString())

        val appId = app.applicationId.toUUID()

        val owners = app.owners
                .map(UUID::fromString)
                .filterNotNull()
                .toCommaSeparatedList()

        database.update(statement,
                        appId,
                        app.name,
                        app.applicationDescription,
                        app.organizationId.toUUID(),
                        app.programmingLanguage.toString(),
                        app.tier.toString(),
                        app.timeOfTokenExpiration.toTimestamp(),
                        app.applicationIconMediaId.toUUID(),
                        owners)
    }

    override fun deserialize(row: ResultSet): Application
    {
        val app = Application()

        val appId = row.getString(Applications.APP_ID)
        val orgId = row.getString(Applications.ORG_ID)
        val appName = row.getString(Applications.APP_NAME)
        val appDescription = row.getString(Applications.APP_DESCRIPTION)
        val programmingLanguage = row.getString(Applications.PROGRAMMING_LANGUAGE).toProgrammingLanguage()
        val timeProvisioned = row.getTimestamp(Applications.TIME_PROVISIONED)
        val timeOfTokenExpiration = row.getTimestamp(Applications.TIME_OF_TOKEN_EXPIRATION)
        val tier = row.getString(Applications.TIER).toTier()
        val appIconId = row.getString(Applications.ICON_MEDIA_ID)
        val owners = row.getArray(Applications.OWNERS)?.array

        app.setApplicationId(appId)
                .setOrganizationId(orgId)
                .setName(appName)
                .setApplicationDescription(appDescription)
                .setProgrammingLanguage(programmingLanguage)
                .setTier(tier)
                .setApplicationIconMediaId(appIconId)


        if (timeProvisioned != null)
        {
            app.setTimeOfProvisioning(timeProvisioned.time)
        }

        if (timeOfTokenExpiration != null)
        {
            app.setTimeOfTokenExpiration(timeOfTokenExpiration.time)
        }

        if (owners != null && owners is Array<*>)
        {
            app.owners = owners.filterNotNull()
                               .map { "$it" }
                               .toSet()
        }

        return app
    }

    private fun String?.toTier(): Tier?
    {
        if (this == null)
        {
            return null
        }

        return try
        {
            Tier.valueOf(this)
        }
        catch (ex: RuntimeException)
        {
            LOG.error("Failed to extract Tier from [$this]")
            return null
        }
    }

    private fun String?.toProgrammingLanguage(): ProgrammingLanguage?
    {
        if (this == null)
        {
            return null
        }

        return try
        {
            ProgrammingLanguage.valueOf(this)
        }
        catch(ex: RuntimeException)
        {
            LOG.error("Failed to ascertain Programming Language from [$this]")
            return null
        }
    }

}