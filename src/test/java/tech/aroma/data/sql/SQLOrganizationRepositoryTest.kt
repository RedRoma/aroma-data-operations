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
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.Organization
import tech.aroma.thrift.User
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.OperationFailedException
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
        }.isInstanceOf(OperationFailedException::class.java)
    }

    @DontRepeat
    @Test
    fun testSaveOrganizationWithBadArgs()
    {
        assertThrows {
            instance.saveOrganization(null)
        }.isInstanceOf(InvalidArgumentException::class.java).hasNoCause()

        assertThrows {
            val invalidOrg = organization.deepCopy().setOrganizationId("")
            instance.saveOrganization(invalidOrg)
        }.isInstanceOf(InvalidArgumentException::class.java).hasNoCause()
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

        val getOrg = Queries.SELECT_ORGANIZATION
        whenever(database.queryForObject(getOrg, serializer, orgId.toUUID()))
                .thenReturn(organization)

        Mockito.doThrow(RuntimeException())
                .whenever(database)
                .update(any(), eq(orgId.toUUID()))

        assertThrows { instance.deleteOrganization(orgId) }
                .isInstanceOf(OperationFailedException::class.java)
    }

    @DontRepeat
    @Test
    fun testDeleteOrganizationWithBadArgs()
    {
        assertThrows { instance.deleteOrganization(null) }
                .isInstanceOf(InvalidArgumentException::class.java)
                .hasNoCause()

        assertThrows { instance.deleteOrganization("") }
                .isInstanceOf(InvalidArgumentException::class.java)
                .hasNoCause()

        assertThrows {
            val emptyOrg = Organization()
            instance.saveOrganization(emptyOrg)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val invalidOrg = organization.deepCopy().setOrganizationId(alphabetic)
            instance.saveOrganization(invalidOrg)
        }.isInstanceOf(InvalidArgumentException::class.java)
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
                .isInstanceOf(InvalidArgumentException::class.java)
                .hasNoCause()

        assertThrows { instance.containsOrganization("") }
                .isInstanceOf(InvalidArgumentException::class.java)
                .hasNoCause()

        assertThrows { instance.containsOrganization(alphabetic) }
                .isInstanceOf(InvalidArgumentException::class.java)
                .hasNoCause()
    }

    @Test
    fun testContainsOrganizationWhenDatabaseFails()
    {
        val query = Queries.CHECK_ORGANIZATION

        Mockito.doThrow(RuntimeException())
                .whenever(database)
                .queryForObject(query, Boolean::class.java, orgId.toUUID())

        assertThrows { instance.containsOrganization(orgId) }
                .isInstanceOf(OperationFailedException::class.java)
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
                .isInstanceOf(InvalidArgumentException::class.java)

        assertThrows { instance.searchByName("") }
                .isInstanceOf(InvalidArgumentException::class.java)

    }

    @DontRepeat
    @Test
    fun testSearchByNameWhenDatabaseFails()
    {
        val query = Queries.SEARCH_ORGANIZATION_BY_NAME
        val name = alphabetic

        whenever(database.query(query, serializer, name))
                .thenThrow(RuntimeException())

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
                .isInstanceOf(InvalidArgumentException::class.java)
                .hasNoCause()

        assertThrows { instance.getOrganizationOwners("") }
                .isInstanceOf(InvalidArgumentException::class.java)
                .hasNoCause()

        assertThrows { instance.getOrganizationOwners(alphabetic) }
                .isInstanceOf(InvalidArgumentException::class.java)
                .hasNoCause()
    }

    @DontRepeat
    @Test
    fun testGetOrganizationOwnersWhenDatabaseFails()
    {
        val query = Queries.SELECT_ORGANIZATION

        whenever(database.queryForList(query, serializer, orgId.toUUID()))
                .thenThrow(RuntimeException())

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

        assertThrows { instance.saveMemberInOrganization(null, user) }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.saveMemberInOrganization("", user) }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.saveMemberInOrganization(alphabetic, user) }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows { instance.saveMemberInOrganization(orgId, null) }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.saveMemberInOrganization(orgId, emptyUser) }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.saveMemberInOrganization(orgId, invalidUser) }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @DontRepeat
    @Test
    fun testSaveMemberInOrgWhenDatabaseFails()
    {
        val statement = Inserts.ORGANIZATION_MEMBER
        val user = User().setUserId(userId)

        whenever(database.update(eq(statement), eq(orgId.toUUID()), eq(userId.toUUID())))
                .thenThrow(RuntimeException())

        assertThrows { instance.saveMemberInOrganization(orgId, user) }
                .isInstanceOf(OperationFailedException::class.java)
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
        assertThrows { instance.isMemberInOrganization(null, userId) }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.isMemberInOrganization("", userId) }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.isMemberInOrganization(alphabetic, userId) }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows { instance.isMemberInOrganization(orgId, null) }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.isMemberInOrganization(orgId, "") }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.isMemberInOrganization(orgId, alphabetic) }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @DontRepeat
    @Test
    fun testIsMemberInOrganizationWhenDatabaseFails()
    {
        val query = Queries.CHECK_ORGANIZATION_HAS_MEMBER

        whenever(database.queryForObject(query, Boolean::class.java, orgId.toUUID(), userId.toUUID()))
                .thenThrow(RuntimeException())

        assertThrows { instance.isMemberInOrganization(orgId, userId) }
                .isInstanceOf(OperationFailedException::class.java)
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
        assertThrows { instance.getOrganizationMembers(null) }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.getOrganizationMembers("") }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.getOrganizationMembers(alphabetic) }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @DontRepeat
    @Test
    fun testGetOrganizationMembersWhenDatabaseFails()
    {
        val query = Queries.SELECT_ORGANIZATION_MEMBERS

        whenever(database.queryForList(query, String::class.java, orgId.toUUID()))
                .thenThrow(RuntimeException())

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
        assertThrows { instance.deleteMember(null, "") }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.deleteMember("", "") }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.deleteMember(alphabetic, "") }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows { instance.deleteMember(orgId, null) }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.deleteMember(orgId, "") }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.deleteMember(orgId, alphabetic) }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @DontRepeat
    @Test
    fun testDeleteMemberWhenDatabaseFails()
    {
        val statement = Deletes.ORGANIZATION_MEMBER

        whenever(database.update(statement, orgId.toUUID(), userId.toUUID()))
                .thenThrow(RuntimeException())

        assertThrows { instance.deleteMember(orgId, userId) }
                .isInstanceOf(OperationFailedException::class.java)
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
        assertThrows { instance.deleteAllMembers(null) }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.deleteAllMembers("") }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.deleteAllMembers(alphabetic) }.isInstanceOf(InvalidArgumentException::class.java)

    }

    @DontRepeat
    @Test
    fun testDeleteAllMembersWhenDatabaseFails()
    {
        val statement = Deletes.ORGANIZATION_ALL_MEMBERS

        whenever(database.update(statement, orgId.toUUID()))
                .thenThrow(RuntimeException())

        assertThrows { instance.deleteAllMembers(orgId) }
                .isInstanceOf(OperationFailedException::class.java)
    }
}