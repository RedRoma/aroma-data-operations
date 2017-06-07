package tech.aroma.data.sql.serializers

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isEmpty
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.serializers.Columns.Organizations
import tech.aroma.data.sql.toCommaSeparatedList
import tech.aroma.data.sql.toUUID
import tech.aroma.thrift.Organization
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.uuids
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import java.sql.Array
import java.sql.ResultSet
import kotlin.test.assertEquals

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


@RunWith(AlchemyTestRunner::class)
class OrganizationSerializerTest
{

    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var resultSet: ResultSet

    @GeneratePojo
    private lateinit var org: Organization

    @GenerateString(UUID)
    private lateinit var orgId: String

    @GenerateString
    private lateinit var userId: String

    @GenerateString
    private lateinit var query: String

    private lateinit var owners: List<String>


    private lateinit var instance: OrganizationSerializer

    @Before
    fun setUp()
    {
        owners = CollectionGenerators.listOf(uuids, 14)
        org.organizationId = orgId
        org.owners = owners

        resultSet.initializeFrom(org)

        instance = OrganizationSerializer()
    }

    @Test
    fun testSave()
    {
        instance.save(org, query, database)

        val captor = ArgumentCaptor.forClass(Any::class.java)

        verify(database).update(eq(query), captor.capture())

        val arguments = captor.allValues
        assertThat(arguments, !isEmpty)

        assertEquals(orgId.toUUID(), arguments[0])
        assertEquals(org.organizationName, arguments[1])
        assertEquals(org.owners.toCommaSeparatedList(), arguments[2])
        assertEquals(org.logoLink, arguments[3])
        assertEquals(org.industry.toString(), arguments[4])
        assertEquals(org.organizationEmail, arguments[5])
        assertEquals(org.githubProfile, arguments[6])
        assertEquals(org.stockMarketSymbol, arguments[7])
        assertEquals(org.tier.toString(), arguments[8])
        assertEquals(org.organizationDescription, arguments[9])
        assertEquals(org.website, arguments[10])
    }

    @DontRepeat
    @Test
    fun testSaveWhenDatabaseFails()
    {
        whenever(database.update(eq(query), Mockito.anyVararg<Any>()))
                .thenThrow(RuntimeException())

        assertThrows { instance.save(org, query, database) }
                .isInstanceOf(RuntimeException::class.java)
    }

    @DontRepeat
    @Test
    fun testSaveWithBadArgs()
    {
        assertThrows {
            val emptyOrg = Organization()
            instance.save(emptyOrg, query, database)
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThrows {
            val invalidOrg = org.deepCopy().setOrganizationId(query)
            instance.save(invalidOrg, query, database)
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThrows { instance.save(org, "", database) }.isInstanceOf(IllegalArgumentException::class.java)

    }


    private fun ResultSet.initializeFrom(org: Organization)
    {
        whenever(this.getString(Organizations.ORG_ID)).thenReturn(org.organizationId)
        whenever(this.getString(Organizations.ORG_NAME)).thenReturn(org.organizationName)
        whenever(this.getString(Organizations.DESCRIPTION)).thenReturn(org.organizationDescription)
        whenever(this.getString(Organizations.EMAIL)).thenReturn(org.organizationEmail)

        whenever(this.getString(Organizations.GITHUB_PROFILE)).thenReturn(org.githubProfile)
        whenever(this.getString(Organizations.ICON_LINK)).thenReturn(org.logoLink)
        whenever(this.getString(Organizations.STOCK_NAME)).thenReturn(org.stockMarketSymbol)
        whenever(this.getString(Organizations.INDUSTRY)).thenReturn(org.industry.toString())
        whenever(this.getString(Organizations.TIER)).thenReturn(org.tier.toString())
        whenever(this.getString(Organizations.WEBSITE)).thenReturn(org.website)

        val mockArray = mock<Array>
        {
            on { array }.thenReturn(owners.toTypedArray())
        }

        whenever(this.getArray(Organizations.OWNERS)).thenReturn(mockArray)
    }

    @Test
    fun testDeserialize()
    {
        val expected = Organization(org)
        expected.unsetTechStack()
        expected.unsetLogo()

        val result = instance.deserialize(resultSet)
        assertEquals(expected, result)
    }

    @DontRepeat
    @Test
    fun testDeserializeWithBadArgs()
    {
        assertThrows { instance.deserialize(null) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }
}