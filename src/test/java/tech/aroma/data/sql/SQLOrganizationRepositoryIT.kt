package tech.aroma.data.sql

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

/**
 * @author SirWellington
 */

import org.junit.*
import org.junit.runner.RunWith
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.serializers.OrganizationSerializer
import tech.aroma.thrift.Organization
import tech.aroma.thrift.User
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.uuids
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import kotlin.test.assertEquals

@RunWith(AlchemyTestRunner::class)
class SQLOrganizationRepositoryIT
{

    companion object
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
        instance.deleteOrganization(orgId)
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
}