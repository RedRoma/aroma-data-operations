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

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import tech.aroma.data.OrganizationRepository
import tech.aroma.data.assertions.RequestAssertions.validOrgId
import tech.aroma.data.assertions.RequestAssertions.validOrganization
import tech.aroma.thrift.Organization
import tech.aroma.thrift.User
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import javax.inject.Inject

/**
 *
 * @author SirWellington
 */
internal class SQLOrganizationRepository: OrganizationRepository
{
    private val database: NamedParameterJdbcTemplate
    private val serializer: DatabaseSerializer<Organization>

    @Inject
    constructor(database: NamedParameterJdbcTemplate, serializer: DatabaseSerializer<Organization>)
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

    }

    override fun getOrganization(organizationId: String?): Organization
    {
        checkThat(organizationId).`is`(validOrgId())
    }

    override fun deleteOrganization(organizationId: String?)
    {
        checkThat(organizationId).`is`(validOrgId())
    }

    override fun containsOrganization(organizationId: String?): Boolean
    {
        checkThat(organizationId).`is`(validOrgId())

    }

    override fun searchByName(searchTerm: String?): MutableList<Organization>
    {
        checkThat(searchTerm).`is`(nonEmptyString())
    }

    override fun getOrganizationOwners(organizationId: String?): MutableList<User>
    {
        checkThat(organizationId).`is`(validOrgId())

    }

    override fun saveMemberInOrganization(organizationId: String?, user: User?)
    {
        checkThat(organizationId).`is`(validOrgId())
    }

    override fun isMemberInOrganization(organizationId: String?, userId: String?): Boolean
    {
        checkThat(organizationId).`is`(validOrgId())

    }

    override fun getOrganizationMembers(organizationId: String?): MutableList<User>
    {
        checkThat(organizationId).`is`(validOrgId())
    }

    override fun deleteMember(organizationId: String?, userId: String?)
    {
        checkThat(organizationId).`is`(validOrgId())
    }

    override fun deleteAllMembers(organizationId: String?)
    {
        checkThat(organizationId).`is`(validOrgId())
    }

}