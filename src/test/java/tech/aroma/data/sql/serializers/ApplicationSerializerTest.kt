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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.jdbc.core.JdbcOperations
import sir.wellington.alchemy.collections.lists.Lists
import tech.aroma.data.AromaGenerators
import tech.aroma.data.sql.serializers.Columns.Applications
import tech.aroma.data.sql.toCommaSeparatedList
import tech.aroma.data.sql.toTimestamp
import tech.aroma.data.sql.toUUID
import tech.aroma.thrift.Application
import tech.sirwellington.alchemy.generator.CollectionGenerators.Companion.listOf
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.Repeat
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
        app.followers = setOf()

        ownerId = Lists.oneOf(app.owners.toList())
    }


    @Test
    fun testSave()
    {
        instance.save(app, query, database)

        verify(database).update(query,
                                appId.toUUID(),
                                app.name,
                                app.applicationDescription,
                                orgId.toUUID(),
                                app.programmingLanguage.toString(),
                                app.tier.toString(),
                                app.timeOfTokenExpiration.toTimestamp(),
                                app.applicationIconMediaId.toUUID(),
                                owners.toCommaSeparatedList())
    }

    @DontRepeat
    @Test
    fun testSaveWhenDatabaseFails()
    {
        whenever(database.update(any<String>(), Mockito.anyVararg<Any>()))
                .thenThrow(UncategorizedSQLException::class.java)

        assertThrows {
            instance.save(app, query, database)
        }.isInstanceOf(DataAccessException::class.java)
    }

    @Test
    fun testSaveWithBadArgs()
    {
        assertThrows {
            val emptyApp = Application()
            instance.save(emptyApp, query, database)
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThrows {
            val emptySQL = ""
            instance.save(app, emptySQL, database)
        }

        assertThrows {
            val appWithoutId = app.deepCopy()
            appWithoutId.unsetApplicationId()
            instance.save(appWithoutId, query, database)
        }

        assertThrows {
            val appWithNoOwners = app.deepCopy().setOwners(setOf())
            instance.save(appWithNoOwners, query, database)
        }

        assertThrows {
            val appWithInvalidOwners = app.deepCopy()
            val owners = listOf(alphabeticStrings(), 10)
            appWithInvalidOwners.owners = owners.toMutableSet()

            instance.save(appWithInvalidOwners, query, database)
        }
    }

    @Test
    fun testDeserialize()
    {
        results.prepareFor(app)

        val result = instance.deserialize(results)

        Assert.assertEquals(app, result)
    }

    @DontRepeat
    @Test
    fun testDeserializeWhenDatabaseFails()
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