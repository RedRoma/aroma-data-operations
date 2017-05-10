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
import org.springframework.jdbc.core.JdbcOperations
import sir.wellington.alchemy.collections.lists.Lists
import tech.aroma.data.OrganizationRepository
import tech.aroma.data.assertions.RequestAssertions.validOrgId
import tech.aroma.data.assertions.RequestAssertions.validOrganization
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.Organization
import tech.aroma.thrift.User
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.OperationFailedException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import javax.inject.Inject

/**
 *
 * @author SirWellington
 */
internal class SQLOrganizationRepository : OrganizationRepository
{
    private val LOG = LoggerFactory.getLogger(this.javaClass)

    private val database: JdbcOperations
    private val serializer: DatabaseSerializer<Organization>

    @Inject
    constructor(database: JdbcOperations, serializer: DatabaseSerializer<Organization>)
    {
        this.database = database
        this.serializer = serializer
    }

    override fun saveOrganization(organization: Organization?)
    {
        checkThat(organization)
                .throwing(InvalidArgumentException::class.java)
                .`is`(notNull())
                .`is`(validOrganization())

        val organization = organization!!
        val statement = Inserts.ORGANIZATION

        try
        {
            serializer.save(organization, null, statement, database)
        }
        catch(ex: Exception)
        {
            LOG.error("Failed to save organization to database: [{}]", organization, ex)
            throw OperationFailedException(ex.message)
        }

    }

    override fun getOrganization(organizationId: String?): Organization
    {
        checkOrgID(organizationId)

        val orgId = organizationId!!
        val query = Queries.SELECT_ORGANIZATION

        try
        {
            return database.queryForObject(query, serializer, orgId.asUUID())
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to get organization with ID [{}]", orgId, ex)
            throw OperationFailedException("Could not get org with ID [$orgId] | ${ex.message}")
        }

    }

    override fun deleteOrganization(organizationId: String?)
    {
        checkOrgID(organizationId)

        val orgId = organizationId!!

        //Save in case a roll-back is needed
        val org = getOrganization(orgId)
        val owners = getOrganizationOwners(orgId)
        val members = getOrganizationMembers(orgId)

        val deleteOrg = Deletes.ORGANIZATION
        val deleteOrgOwners = Deletes.ORGANIZATION_ALL_OWNERS
        val deleteOrgMembers = Deletes.ORGANIZATION_ALL_MEMBERS

        try
        {
            database.update(deleteOrgMembers, orgId.asUUID())
            database.update(deleteOrgOwners, orgId.asUUID())
            database.update(deleteOrg, orgId.asUUID())
        }
        catch(ex: Exception)
        {
            LOG.error("Failed to delete organization [{}]. Rolling back operation.", org, ex)

            //Rollback
            saveOrganization(org)
            members.forEach { this.saveMemberInOrganization(orgId, it) }

            throw OperationFailedException("Could not delete organization: $org | ${ex.message}")
        }

    }

    override fun containsOrganization(organizationId: String?): Boolean
    {
        checkOrgID(organizationId)

        val orgId = organizationId!!
        val query = Queries.CHECK_ORGANIZATION

        try
        {
            return database.queryForObject(query, Boolean::class.java, orgId.asUUID())
        }
        catch(ex: Exception)
        {
            LOG.error("Failed to check if org [{}] exists.", orgId, ex)
            throw OperationFailedException("Failed to check if $orgId exists | ${ex.message}")
        }
    }

    override fun searchByName(searchTerm: String?): MutableList<Organization>
    {
        checkThat(searchTerm)
                .throwing(InvalidArgumentException::class.java)
                .`is`(nonEmptyString())

        var searchTerm = searchTerm!!
        searchTerm = "%$searchTerm%"

        val query = Queries.SEARCH_ORGANIZATION_BY_NAME

        var result = mutableListOf<Organization>()

        try
        {
            result = database.query(query, serializer, searchTerm)
        }
        catch(ex: Exception)
        {
            LOG.warn("Could not find Organizations with name [{}].", searchTerm, ex)
        }

        return result
    }

    override fun getOrganizationOwners(organizationId: String?): MutableList<User>
    {
        checkOrgID(organizationId)

        val orgId = organizationId!!
        val query = Queries.SELECT_ORGANIZATION_OWNERS

        try
        {
            return database
                    .queryForList(query, String::class.java, orgId.asUUID())
                    .map { User().setUserId(it) }
                    .toMutableList()
        }
        catch (ex: Exception)
        {
            LOG.warn("Failed to get a list of owners for org $orgId", ex)
            return mutableListOf()
        }
    }

    override fun saveMemberInOrganization(organizationId: String?, user: User?)
    {
        checkOrgID(organizationId)

        val statement = Inserts.ORGANIZATION_MEMBER
    }

    override fun isMemberInOrganization(organizationId: String?, userId: String?): Boolean
    {
        checkOrgID(organizationId)

        val query = Queries.CHECK_ORGANIZATION_HAS_MEMBER

        return false
    }

    override fun getOrganizationMembers(organizationId: String?): MutableList<User>
    {
        checkOrgID(organizationId)

        val query = Queries.SELECT_ORGANIZATION_MEMBERS

        return Lists.emptyList()
    }

    override fun deleteMember(organizationId: String?, userId: String?)
    {
        checkOrgID(organizationId)

        val statement = Deletes.ORGANIZATION_MEMBER
    }

    override fun deleteAllMembers(organizationId: String?)
    {
        checkOrgID(organizationId)

        val statement = Deletes.ORGANIZATION_ALL_MEMBERS
    }

    private fun checkOrgID(orgId: String?)
    {
        checkThat(orgId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validOrgId())
    }

}