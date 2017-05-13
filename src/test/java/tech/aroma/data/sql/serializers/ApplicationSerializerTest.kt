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
import org.springframework.jdbc.core.JdbcOperations
import sir.wellington.alchemy.collections.lists.Lists
import tech.aroma.data.AromaGenerators
import tech.aroma.data.sql.asUUID
import tech.aroma.data.sql.serializers.Tables.Applications
import tech.aroma.data.sql.toTimestamp
import tech.aroma.thrift.Application
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import java.sql.ResultSet
import java.util.*
import kotlin.test.assertEquals

@RunWith(AlchemyTestRunner::class)
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
    fun testSave()
    {
        instance.save(app, null, query, database)

        val owners = this.owners.map(UUID::fromString)

        verify(database).update(query,
                                appId.asUUID(),
                                app.name,
                                app.applicationDescription,
                                orgId.asUUID(),
                                app.programmingLanguage.toString(),
                                app.tier.toString(),
                                app.timeOfTokenExpiration.toTimestamp(),
                                app.applicationIconMediaId.asUUID(),
                                owners)
    }

    @Test
    fun testDeserialize()
    {
        results.prepareFor(app)

        val result = instance.deserialize(results)

        Assert.assertEquals(app, result)
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