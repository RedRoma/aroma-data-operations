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
import tech.aroma.data.assertions.RequestAssertions.validOrganization
import tech.aroma.data.sql.*
import tech.aroma.data.sql.serializers.Tables.Organizations
import tech.aroma.thrift.*
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import java.sql.ResultSet
import java.time.Duration


/**
 * Responsible for the serialization of [Organizations][Organization]
 * @author SirWellington
 */
internal class OrganizationSerializer : DatabaseSerializer<Organization>
{
    override fun save(`object`: Organization?, statement: String?, database: JdbcOperations?)
    {
        checkThat(`object`, statement, database)
                .are(notNull())

        val org = `object`!!
        checkThat(org)
                .`is`(validOrganization())

        checkThat(statement)
                .`is`(nonEmptyString())

        database?.update(statement,
                         org.organizationId.toUUID(),
                         org.organizationName,
                         org.owners.toCommaSeparatedList(),
                         org.logoLink,
                         org.industry?.toString(),
                         org.organizationEmail,
                         org.githubProfile,
                         org.stockMarketSymbol,
                         org.tier?.toString(),
                         org.organizationDescription,
                         org.website)

    }

    override fun deserialize(resultSet: ResultSet?): Organization
    {
        checkThat(resultSet).`is`(notNull())

        val org = Organization()
        val results = resultSet ?: return org


        val orgId = results.getString(Organizations.ORG_ID)
        val orgName = results.getString(Organizations.ORG_NAME)
        val orgDescription = results.getString(Organizations.DESCRIPTION)
        val industry = results.getString(Organizations.INDUSTRY).asIndustry()
        val tier = results.getString(Organizations.TIER).asTier()
        val website = results.getString(Organizations.WEBSITE)
        val stock = results.getString(Organizations.STOCK_NAME)
        val iconLink = results.getString(Organizations.ICON_LINK)
        val github = results.getString(Organizations.GITHUB_PROFILE)
        val email = results.getString(Organizations.EMAIL)

        val ownersArray = results.getArray(Organizations.OWNERS)

        if (ownersArray != null && ownersArray.array is Array<*>)
        {
            val owners = ownersArray.array as Array<*>

            if (owners.isArrayOf<String>())
            {
                org.setOwners(owners.map { it.toString() })
            }
        }

        return org
                .setOrganizationId(orgId)
                .setOrganizationName(orgName)
                .setOrganizationDescription(orgDescription)
                .setIndustry(industry)
                .setTier(tier)
                .setWebsite(website)
                .setStockMarketSymbol(stock)
                .setLogoLink(iconLink)
                .setGithubProfile(github)
                .setOrganizationEmail(email)
    }

    private fun String.asTier(): Tier?
    {
        return try
        {
            Tier.valueOf(this)
        }
        catch (ex: Exception)
        {
            return null
        }
    }

    private fun String.asIndustry(): Industry?
    {
        return try
        {
            Industry.valueOf(this)
        }
        catch (ex: Exception)
        {
            return null
        }
    }

}