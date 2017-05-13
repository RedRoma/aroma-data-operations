package tech.aroma.data.sql.serializers

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

import com.nhaarman.mockito_kotlin.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.jdbc.core.JdbcOperations
import sir.wellington.alchemy.collections.lists.Lists
import tech.aroma.data.AromaGenerators
import tech.aroma.data.sql.serializers.Tables.Applications
import tech.aroma.data.sql.toTimestamp
import tech.aroma.data.sql.toUUID
import tech.aroma.thrift.Application
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import java.lang.IllegalArgumentException
import java.sql.ResultSet
import java.sql.SQLException

@RunWith(AlchemyTestRunner::class)
@Repeat
class ApplicationSerializerTest
{

    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var results: ResultSet

    @GenerateString
    private lateinit var query: String

    private lateinit var app: Application
    private lateinit var appId: String
    private lateinit var orgId: String
    private lateinit var ownerId: String
    private lateinit var owners: Set<String>

    private lateinit var instance: ApplicationSerializer

    @Before
    fun setup()
    {
        instance = ApplicationSerializer()

        app = AromaGenerators.Applications.application
        appId = app.applicationId
        orgId = app.organizationId
        owners = app.owners

        app.totalMessagesSent = 0
        app.unsetTotalMessagesSent()
        app.unsetFollowers()
        app.followers = emptySet()

        ownerId = Lists.oneOf(app.owners.toList())
    }


    @Test
    fun `Test - save`()
    {
        instance.save(app, null, query, database)

        val owners = this.owners.joinToString(separator = ",")

        verify(database).update(query,
                                appId.toUUID(),
                                app.name,
                                app.applicationDescription,
                                orgId.toUUID(),
                                app.programmingLanguage.toString(),
                                app.tier.toString(),
                                app.timeOfTokenExpiration.toTimestamp(),
                                app.applicationIconMediaId.toUUID(),
                                owners)
    }

    @DontRepeat
    @Test
    fun `Test - save when database fails`()
    {
        whenever(database.update(any<String>(), Mockito.anyVararg<Any>()))
                .thenThrow(UncategorizedSQLException::class.java)

        assertThrows {
            instance.save(app, null, query, database)
        }.isInstanceOf(DataAccessException::class.java)
    }

    @Test
    fun `Test - save with bad args`()
    {
        assertThrows {
            val emptyApp = Application()
            instance.save(emptyApp, null, query, database)
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThrows {
            val emptySQL = ""
            instance.save(app, null, emptySQL, database)
        }

        assertThrows {
            val appWithoutId = app.deepCopy()
            appWithoutId.unsetApplicationId()
            instance.save(appWithoutId, null, query, database)
        }

        assertThrows {
            val appWithNoOwners = app.deepCopy().setOwners(emptySet())
            instance.save(appWithNoOwners, null, query, database)
        }

        assertThrows {
            val appWithInvalidOwners = app.deepCopy()
            val owners = CollectionGenerators.listOf(alphabeticString(), 10)
            appWithInvalidOwners.owners = owners.toMutableSet()

            instance.save(appWithInvalidOwners, null, query, database)
        }
    }

    @Test
    fun `Test - deserialize`()
    {
        results.prepareFor(app)

        val result = instance.deserialize(results)

        Assert.assertEquals(app, result)
    }

    @DontRepeat
    @Test
    fun `Test - deserialize when database fails`()
    {
        whenever(results.getString(any<String>()))
                .thenThrow(SQLException())

        assertThrows {
            instance.deserialize(results)
        }.isInstanceOf(SQLException::class.java)
    }

    private fun ResultSet.prepareFor(app: Application)
    {
        whenever(this.getString(Applications.APP_ID)).thenReturn(appId)
        whenever(this.getString(Applications.ORG_ID)).thenReturn(orgId)
        whenever(this.getString(Applications.ICON_MEDIA_ID)).thenReturn(app.applicationIconMediaId)
        whenever(this.getString(Applications.APP_NAME)).thenReturn(app.name)
        whenever(this.getString(Applications.APP_DESCRIPTION)).thenReturn(app.applicationDescription)
        whenever(this.getString(Applications.TIER)).thenReturn(app.tier.toString())
        whenever(this.getString(Applications.PROGRAMMING_LANGUAGE)).thenReturn(app.programmingLanguage.toString())
        whenever(this.getTimestamp(Applications.TIME_OF_TOKEN_EXPIRATION)).thenReturn(app.timeOfTokenExpiration.toTimestamp())
        whenever(this.getTimestamp(Applications.TIME_PROVISIONED)).thenReturn(app.timeOfProvisioning.toTimestamp())

        val array = mock<java.sql.Array> {
            on { array }.thenReturn(owners.toTypedArray())
        }
        whenever(this.getArray(Applications.OWNERS)).thenReturn(array)
    }
}