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


import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.serializers.OrganizationSerializer
import tech.aroma.thrift.Organization
import tech.aroma.thrift.User
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.uuids
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import kotlin.test.*

/**
 * @author SirWellington
 */
@IntegrationTest
@RunWith(AlchemyTestRunner::class)
class SQLOrganizationRepositoryIT
{

    private companion object
    {

        private lateinit var database: JdbcOperations

        @JvmStatic
        @BeforeClass
        fun prepareClass()
        {
            database = TestingResources.connectToDatabase()
        }

    }

    @GeneratePojo
    private lateinit var org: Organization

    @GenerateString(UUID)
    private lateinit var orgId: String

    @GenerateString(UUID)
    private lateinit var userId: String

    private val user: User
        get() = User().setUserId(userId)

    private lateinit var userIds: List<String>

    private val users: List<User>
        get() = userIds.map { User().setUserId(it) }.toList()

    private lateinit var instance: SQLOrganizationRepository

    private val serializer: DatabaseSerializer<Organization> = OrganizationSerializer()

    @Before
    fun setUp()
    {
        userIds = CollectionGenerators.listOf(uuids, 10)
        org.owners = userIds
        org.organizationId = orgId
        org.unsetLogo()
        org.unsetTechStack()

        instance = SQLOrganizationRepository(database, serializer)
    }

    @After
    fun destroy()
    {
        try
        {
            instance.deleteOrganization(orgId)
        }
        catch (ex: Exception)
        {

        }

        try { instance.deleteAllMembers(orgId) } catch (ex: Exception) { }
    }

    @Test
    fun testSave()
    {
        instance.saveOrganization(org)
    }

    @Test
    fun testGet()
    {
        assertThrows {
            instance.getOrganization(orgId)
        }

        instance.saveOrganization(org)

        val result = instance.getOrganization(orgId)

        assertEquals(org, result)
    }

    @Test
    fun testDeleteOrg()
    {
        assertThrows { instance.deleteOrganization(orgId) }
    }

    @Test
    fun testContainsOrg()
    {
        assertFalse { instance.containsOrganization(orgId) }

        instance.saveOrganization(org)

        assertTrue { instance.containsOrganization(orgId) }
    }

    @Test
    fun testSearchByName()
    {
        val name = org.organizationName
        val subName = name.subSequence(1..5).toString()

        instance.saveOrganization(org)

        val results = instance.searchByName(subName)
        assertThat(results, hasElement(org))
    }

    @Test
    fun testSearchByNameWhenNone()
    {
        val name = org.organizationName
        val subName = name.subSequence(1..5).toString()

        val results = instance.searchByName(subName)
        assertThat(results, isEmpty)
    }

    @Test
    fun testGetOrganizationOwners()
    {
        instance.saveOrganization(org)

        val result = instance.getOrganizationOwners(orgId)

        assertEquals(users, result)
    }

    fun testGetOrganizationOwnersWhenNone()
    {
        val result = instance.getOrganizationOwners(orgId)

        assertThat(result, isEmpty)
    }

    @Test
    fun testSaveMemberInOrg()
    {
        instance.saveOrganization(org)

        val member = User().setUserId(userId)
        instance.saveMemberInOrganization(orgId, member)

        assertTrue { instance.isMemberInOrganization(orgId, userId) }
    }

    @Test
    fun testIsMember()
    {
        assertFalse { instance.isMemberInOrganization(orgId, userId) }

        instance.saveOrganization(org)
        assertFalse { instance.isMemberInOrganization(orgId, userId) }

        val user = User().setUserId(userId)
        instance.saveMemberInOrganization(orgId, user)

        assertTrue { instance.isMemberInOrganization(orgId, userId) }
    }

    @Test
    fun testGetOrganizationMembers()
    {
        instance.saveOrganization(org)

        val expected = userIds.map { User().setUserId(it) }

        expected.forEach { instance.saveMemberInOrganization(orgId, it) }

        val results = instance.getOrganizationMembers(orgId)

        assertThat(results, equalTo(expected))
    }

    fun testGetOrganizationMembersWhenNone()
    {
        val result = instance.getOrganizationMembers(orgId)
        assertThat(result, isEmpty)
    }

    @Test
    fun testDeleteMember()
    {
        instance.saveOrganization(org)
        instance.saveMemberInOrganization(orgId, user)

        assertTrue { instance.isMemberInOrganization(orgId, userId) }

        instance.deleteMember(orgId, userId)

        assertFalse { instance.isMemberInOrganization(orgId, userId) }
    }

    fun testDeleteMemberWhenNone()
    {
        instance.deleteMember(orgId, userId)
    }

    @Test
    fun testDeleteAllMembers()
    {
        users.forEach { instance.saveMemberInOrganization(orgId, it) }

        users.forEach {
            assertTrue { instance.isMemberInOrganization(orgId, it.userId) }
        }

        instance.deleteAllMembers(orgId)

        val members = instance.getOrganizationMembers(orgId)
        assertThat(members, isEmpty)

        users.forEach {
            assertFalse { instance.isMemberInOrganization(orgId, it.userId) }
        }
    }

    fun testDeleteAllMembersWhenNone()
    {
        instance.deleteAllMembers(orgId)
    }
}