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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmpty
import com.nhaarman.mockito_kotlin.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.invalidArg
import tech.aroma.data.operationError
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.Organization
import tech.aroma.thrift.User
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.BooleanGenerators.booleans
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.ObjectGenerators.pojos
import tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString
import tech.sirwellington.alchemy.generator.StringGenerators.uuids
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@Repeat
class SQLOrganizationRepositoryTest
{
    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var serializer: DatabaseSerializer<Organization>

    @Mock
    private lateinit var userSerializer: DatabaseSerializer<User>

    @GeneratePojo
    private lateinit var organization: Organization

    @GenerateString(UUID)
    private lateinit var orgId: String

    @GenerateString(UUID)
    private lateinit var userId: String

    private lateinit var userIds: List<String>

    @GenerateString(ALPHABETIC)
    private lateinit var alphabetic: String

    private lateinit var instance: SQLOrganizationRepository

    @Before
    fun setUp()
    {
        organization.organizationId = orgId

        userIds = CollectionGenerators.listOf(uuids, 10)
        organization.owners = userIds

        userIds = CollectionGenerators.listOf(uuids, 4)

        instance = SQLOrganizationRepository(database = database, serializer = serializer)
    }

    @Test
    fun testSaveOrganization()
    {
        val statement = Inserts.ORGANIZATION

        instance.saveOrganization(organization)

        verify(serializer).save(organization, statement, database)
    }

    @DontRepeat
    @Test
    fun testSaveOrganizationWhenSerializerFails()
    {
        val statement = Inserts.ORGANIZATION

        Mockito.doThrow(RuntimeException())
                .whenever(serializer)
                .save(organization, statement, database)

        assertThrows {
            instance.saveOrganization(organization)
        }.operationError()
    }

    @DontRepeat
    @Test
    fun testSaveOrganizationWithBadArgs()
    {
        assertThrows {
            instance.saveOrganization(null)
        }.invalidArg().hasNoCause()

        assertThrows {
            val invalidOrg = organization.deepCopy().setOrganizationId("")
            instance.saveOrganization(invalidOrg)
        }.invalidArg().hasNoCause()
    }

    @Test
    fun testDeleteOrganization()
    {
        val getOrg = Queries.SELECT_ORGANIZATION
        whenever(database.queryForObject(getOrg, serializer, orgId.toUUID()))
                .thenReturn(organization)

        val statementToDeleteMembers = Deletes.ORGANIZATION_ALL_MEMBERS
        val statementToDeleteOrg = Deletes.ORGANIZATION

        instance.deleteOrganization(orgId)

        val inOrder = inOrder(database)

        inOrder.verify(database).update(statementToDeleteMembers, orgId.toUUID())
        inOrder.verify(database).update(statementToDeleteOrg, orgId.toUUID())
    }

    @DontRepeat
    @Test
    fun testDeleteOrganizationWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.deleteOrganization(orgId) }.operationError()
    }

    @DontRepeat
    @Test
    fun testDeleteOrganizationWithBadArgs()
    {
        assertThrows { instance.deleteOrganization(null) }
                .invalidArg()
                .hasNoCause()

        assertThrows { instance.deleteOrganization("") }
                .invalidArg()
                .hasNoCause()

        assertThrows {
            val emptyOrg = Organization()
            instance.saveOrganization(emptyOrg)
        }.invalidArg()

        assertThrows {
            val invalidOrg = organization.deepCopy().setOrganizationId(alphabetic)
            instance.saveOrganization(invalidOrg)
        }.invalidArg()
    }

    @Test
    fun testContainsOrganization()
    {
        val expected = one(booleans())!!
        val query = Queries.CHECK_ORGANIZATION

        whenever(database.queryForObject(query, Boolean::class.java, orgId.toUUID()))
                .thenReturn(expected)

        val result = instance.containsOrganization(orgId)

        assertEquals(expected, result)
    }

    @DontRepeat
    @Test
    fun testContainsOrganizationWithBadArgs()
    {
        assertThrows { instance.containsOrganization(null) }
                .invalidArg()
                .hasNoCause()

        assertThrows { instance.containsOrganization("") }
                .invalidArg()
                .hasNoCause()

        assertThrows { instance.containsOrganization(alphabetic) }
                .invalidArg()
                .hasNoCause()
    }

    @Test
    fun testContainsOrganizationWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.containsOrganization(orgId) }.operationError()
    }


    @Test
    fun testSearchByName()
    {
        val query = Queries.SEARCH_ORGANIZATION_BY_NAME
        val name = alphabetic
        val searchTerm = "%$name%"
        val orgs = CollectionGenerators.listOf(pojos(Organization::class.java))

        whenever(database.query(query, serializer, searchTerm))
                .thenReturn(orgs)

        val result = instance.searchByName(name)
        assertThat(result, equalTo(orgs))
    }

    @DontRepeat
    @Test
    fun testSearchByNameWithBadArgs()
    {
        assertThrows { instance.searchByName(null) }
                .invalidArg()

        assertThrows { instance.searchByName("") }
                .invalidArg()

    }

    @DontRepeat
    @Test
    fun testSearchByNameWhenDatabaseFails()
    {
        database.setupForFailure()

        val name = alphabetic
        val result = instance.searchByName(name)

        assertThat(result, isEmpty)
    }

    @Test
    fun testGetOrganizationOwners()
    {
        val query = Queries.SELECT_ORGANIZATION

        whenever(database.queryForObject(query, serializer, orgId.toUUID()))
                .thenReturn(organization)

        val expected = organization.owners.map { User().setUserId(it) }

        val result = instance.getOrganizationOwners(orgId)

        assertThat(result, equalTo(expected))
    }

    @DontRepeat
    @Test
    fun testGetOrganizationOwnersWithBadArgs()
    {
        assertThrows { instance.getOrganizationOwners(null) }
                .invalidArg()
                .hasNoCause()

        assertThrows { instance.getOrganizationOwners("") }
                .invalidArg()
                .hasNoCause()

        assertThrows { instance.getOrganizationOwners(alphabetic) }
                .invalidArg()
                .hasNoCause()
    }

    @DontRepeat
    @Test
    fun testGetOrganizationOwnersWhenDatabaseFails()
    {
        database.setupForFailure()

        val result = instance.getOrganizationOwners(orgId)
        assertThat(result, isEmpty)
    }

    @Test
    fun testSaveMemberInOrg()
    {
        val statement = Inserts.ORGANIZATION_MEMBER
        val user = pojos(User::class.java).get().setUserId(userId)

        instance.saveMemberInOrganization(orgId, user)

        val argumentsCaptor = ArgumentCaptor.forClass(Any::class.java)

        verify(database).update(eq(statement), argumentsCaptor.capture())

        val arguments = argumentsCaptor.allValues

        assertEquals(orgId.toUUID(), arguments[0])
        assertEquals(userId.toUUID(), arguments[1])
    }

    @DontRepeat
    @Test
    fun testSaveMemberInOrgWithBadArgs()
    {
        val user = User().setUserId(userId)
        val emptyUser = User()
        val invalidUser = User().setUserId(alphabetic)

        assertThrows { instance.saveMemberInOrganization(null, user) }.invalidArg()
        assertThrows { instance.saveMemberInOrganization("", user) }.invalidArg()
        assertThrows { instance.saveMemberInOrganization(alphabetic, user) }.invalidArg()

        assertThrows { instance.saveMemberInOrganization(orgId, null) }.invalidArg()
        assertThrows { instance.saveMemberInOrganization(orgId, emptyUser) }.invalidArg()
        assertThrows { instance.saveMemberInOrganization(orgId, invalidUser) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testSaveMemberInOrgWhenDatabaseFails()
    {
        database.setupForFailure()

        val user = User().setUserId(userId)

        assertThrows { instance.saveMemberInOrganization(orgId, user) }
                .operationError()
    }

    @Test
    fun testIsMemberInOrganization()
    {
        val expected = one(booleans())
        val query = Queries.CHECK_ORGANIZATION_HAS_MEMBER

        whenever(database.queryForObject(query, Boolean::class.java, orgId.toUUID(), userId.toUUID()))
                .thenReturn(expected)

        val result = instance.isMemberInOrganization(orgId, userId)

        assertThat(result, equalTo(expected))
    }

    @DontRepeat
    @Test
    fun testIsMemberInOrganizationWithBadArgs()
    {
        assertThrows { instance.isMemberInOrganization(null, userId) }.invalidArg()
        assertThrows { instance.isMemberInOrganization("", userId) }.invalidArg()
        assertThrows { instance.isMemberInOrganization(alphabetic, userId) }.invalidArg()

        assertThrows { instance.isMemberInOrganization(orgId, null) }.invalidArg()
        assertThrows { instance.isMemberInOrganization(orgId, "") }.invalidArg()
        assertThrows { instance.isMemberInOrganization(orgId, alphabetic) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testIsMemberInOrganizationWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.isMemberInOrganization(orgId, userId) }
                .operationError()
    }

    @Test
    fun testGetOrganizationMembers()
    {
        val query = Queries.SELECT_ORGANIZATION_MEMBERS
        val members = CollectionGenerators.listOf(alphabeticString(), 10)

        whenever(database.queryForList(query, String::class.java, orgId.toUUID()))
                .thenReturn(members)

        val result = instance.getOrganizationMembers(orgId)
        assertThat(result.map { it.userId }, equalTo(members))
    }

    @DontRepeat
    @Test
    fun testGetOrganizationMembersWithBadArgs()
    {
        assertThrows { instance.getOrganizationMembers(null) }.invalidArg()
        assertThrows { instance.getOrganizationMembers("") }.invalidArg()
        assertThrows { instance.getOrganizationMembers(alphabetic) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testGetOrganizationMembersWhenDatabaseFails()
    {
        database.setupForFailure()

        val results = instance.getOrganizationMembers(orgId)
        assertThat(results, isEmpty)
    }

    @Test
    fun testDeleteMember()
    {
        val query = Deletes.ORGANIZATION_MEMBER

        instance.deleteMember(orgId, userId)

        verify(database).update(query, orgId.toUUID(), userId.toUUID())
    }

    @DontRepeat
    @Test
    fun testDeleteMemberWithBadArgs()
    {
        assertThrows { instance.deleteMember(null, "") }.invalidArg()
        assertThrows { instance.deleteMember("", "") }.invalidArg()
        assertThrows { instance.deleteMember(alphabetic, "") }.invalidArg()

        assertThrows { instance.deleteMember(orgId, null) }.invalidArg()
        assertThrows { instance.deleteMember(orgId, "") }.invalidArg()
        assertThrows { instance.deleteMember(orgId, alphabetic) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testDeleteMemberWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.deleteMember(orgId, userId) }
                .operationError()
    }

    @Test
    fun testDeleteAllMembers()
    {
        val statement = Deletes.ORGANIZATION_ALL_MEMBERS

        instance.deleteAllMembers(orgId)

        verify(database).update(statement, orgId.toUUID())
    }

    @DontRepeat
    @Test
    fun testDeleteAllMembersWithBadArgs()
    {
        assertThrows { instance.deleteAllMembers(null) }.invalidArg()
        assertThrows { instance.deleteAllMembers("") }.invalidArg()
        assertThrows { instance.deleteAllMembers(alphabetic) }.invalidArg()

    }

    @DontRepeat
    @Test
    fun testDeleteAllMembersWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.deleteAllMembers(orgId) }
                .operationError()
    }
}