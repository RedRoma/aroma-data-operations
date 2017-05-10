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
        checkThat(organizationId).`is`(validOrgId())

        val query = Queries.SELECT_ORGANIZATION

        return Organization()
    }

    override fun deleteOrganization(organizationId: String?)
    {
        checkThat(organizationId).`is`(validOrgId())

        val statement = Deletes.ORGANIZATION
        val statementToDeleteOwners = Deletes.ORGANIZATION_ALL_OWNERS
        val statementToDeleteMembers = Deletes.ORGANIZATION_ALL_MEMBERS


    }

    override fun containsOrganization(organizationId: String?): Boolean
    {
        checkThat(organizationId).`is`(validOrgId())

        val query = Queries.CHECK_ORGANIZATION

        return false
    }

    override fun searchByName(searchTerm: String?): MutableList<Organization>
    {
        checkThat(searchTerm).`is`(nonEmptyString())

        val query = Queries.SEARCH_ORGANIZATION_BY_NAME

        return Lists.emptyList()
    }

    override fun getOrganizationOwners(organizationId: String?): MutableList<User>
    {
        checkThat(organizationId).`is`(validOrgId())

        val query = Queries.SELECT_ORGANIZATION_OWNERS

        return Lists.emptyList()

    }

    override fun saveMemberInOrganization(organizationId: String?, user: User?)
    {
        checkThat(organizationId).`is`(validOrgId())

        val statement = Inserts.ORGANIZATION_MEMBER
    }

    override fun isMemberInOrganization(organizationId: String?, userId: String?): Boolean
    {
        checkThat(organizationId).`is`(validOrgId())

        val query = Queries.CHECK_ORGANIZATION_HAS_MEMBER

        return false
    }

    override fun getOrganizationMembers(organizationId: String?): MutableList<User>
    {
        checkThat(organizationId).`is`(validOrgId())

        val query = Queries.SELECT_ORGANIZATION_MEMBERS

        return Lists.emptyList()
    }

    override fun deleteMember(organizationId: String?, userId: String?)
    {
        checkThat(organizationId).`is`(validOrgId())

        val statement = Deletes.ORGANIZATION_MEMBER
    }

    override fun deleteAllMembers(organizationId: String?)
    {
        checkThat(organizationId).`is`(validOrgId())

        val statement = Deletes.ORGANIZATION_ALL_MEMBERS
    }

}