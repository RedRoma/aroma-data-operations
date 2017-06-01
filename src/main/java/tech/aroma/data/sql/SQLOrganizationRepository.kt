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
import tech.aroma.data.assertions.RequestAssertions.*
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
            serializer.save(organization, statement, database)
        }
        catch(ex: Exception)
        {
            failWithMessage("Failed to save organization to database: [$organization]", ex)
        }

    }

    override fun getOrganization(organizationId: String?): Organization
    {
        checkOrgID(organizationId)

        val orgId = organizationId!!.toUUID()!!
        val query = Queries.SELECT_ORGANIZATION

        try
        {
            return database.queryForObject(query, serializer, orgId)
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not get org with ID [$orgId]", ex)
        }

    }

    override fun deleteOrganization(organizationId: String?)
    {
        checkOrgID(organizationId)

        val orgId = organizationId!!

        //Save in case a roll-back is needed
        val org = getOrganization(orgId)
        val members = getOrganizationMembers(orgId)

        val deleteOrg = Deletes.ORGANIZATION
        val deleteOrgMembers = Deletes.ORGANIZATION_ALL_MEMBERS

        try
        {
            database.update(deleteOrgMembers, orgId.toUUID())
            database.update(deleteOrg, orgId.toUUID())
        }
        catch(ex: Exception)
        {
            //Rollback
            saveOrganization(org)
            members.forEach { this.saveMemberInOrganization(orgId, it) }

            failWithMessage("Failed to delete organization [$org]. Rolling back operation.", ex)
        }

    }

    override fun containsOrganization(organizationId: String?): Boolean
    {
        checkOrgID(organizationId)

        val orgId = organizationId!!.toUUID()
        val query = Queries.CHECK_ORGANIZATION

        try
        {
            return database.queryForObject(query, Boolean::class.java, orgId)
        }
        catch(ex: Exception)
        {
            failWithMessage("Failed to check if $orgId exists ", ex)
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

        return try
        {
            this.getOrganization(organizationId)
                    .owners
                    .map { User().setUserId(it) }
                    .toMutableList()
        }
        catch (ex: Exception)
        {
            return mutableListOf()
        }
    }

    override fun saveMemberInOrganization(organizationId: String?, user: User?)
    {
        checkOrgID(organizationId)

        checkThat(user?.userId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validUserId())

        val statement = Inserts.ORGANIZATION_MEMBER
        val orgId = organizationId!!.toUUID()
        val userId = user!!.userId!!.toUUID()

        try
        {
            database.update(statement, orgId, userId)
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to save user $userId in Org [$orgId]", ex)
        }
    }


    override fun isMemberInOrganization(organizationId: String?, userId: String?): Boolean
    {
        checkOrgID(organizationId)
        checkThat(userId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validUserId())

        val query = Queries.CHECK_ORGANIZATION_HAS_MEMBER
        val orgId = organizationId!!.toUUID()
        val userId = userId!!.toUUID()

        try
        {
            return database.queryForObject(query, Boolean::class.java, orgId, userId)
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to check if [$userId] is a member of [$orgId]", ex)
        }
    }

    override fun getOrganizationMembers(organizationId: String?): MutableList<User>
    {
        checkOrgID(organizationId)

        val query = Queries.SELECT_ORGANIZATION_MEMBERS
        val orgId = organizationId!!.toUUID()

        try
        {
            return database
                    .queryForList(query, String::class.java, orgId)
                    .map { User().setUserId(it) }
                    .toMutableList()
        }
        catch (ex: Exception)
        {
            LOG.warn("Failed to retrieve organizations member: [{}]", orgId, ex)
            return Lists.emptyList()
        }
    }

    override fun deleteMember(organizationId: String?, userId: String?)
    {
        checkOrgID(organizationId)
        checkThat(userId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validUserId())

        val statement = Deletes.ORGANIZATION_MEMBER
        val orgId = organizationId!!.toUUID()
        val userId = userId!!.toUUID()

        try
        {
            database.update(statement, orgId, userId)
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to remove member $userId from $organizationId", ex)
        }
    }

    override fun deleteAllMembers(organizationId: String?)
    {
        checkOrgID(organizationId)

        val statement = Deletes.ORGANIZATION_ALL_MEMBERS
        val orgId = organizationId!!.toUUID()

        try
        {
            database.update(statement, orgId)
        }
        catch(ex: Exception)
        {
            failWithMessage("Failed to remove all members for Org: [$orgId]", ex)
        }
    }

    private fun checkOrgID(orgId: String?)
    {
        checkThat(orgId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validOrgId())
    }

}