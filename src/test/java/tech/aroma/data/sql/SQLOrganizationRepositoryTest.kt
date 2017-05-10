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

import com.nhaarman.mockito_kotlin.*
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.SQLStatements.Inserts
import tech.aroma.thrift.Organization
import tech.aroma.thrift.User
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.OperationFailedException
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
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

    private lateinit var instance: SQLOrganizationRepository

    @Before
    fun setUp()
    {
        organization.organizationId = orgId
        organization.owners?.clear()

        instance = SQLOrganizationRepository(database = database, serializer = serializer)
    }

    @Test
    fun testSaveOrganization()
    {
        val statement = Inserts.ORGANIZATION

        instance.saveOrganization(organization)

        verify(serializer).save(organization, null, statement, database)
    }

    @DontRepeat
    @Test
    fun testSaveOrganizationWhenSerializerFails()
    {
        val statement = Inserts.ORGANIZATION

        Mockito.doThrow(RuntimeException())
                .whenever(serializer)
                .save(organization, null, statement, database)

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

}