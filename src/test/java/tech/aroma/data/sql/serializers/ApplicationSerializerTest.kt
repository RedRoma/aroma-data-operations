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

import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import sir.wellington.alchemy.collections.lists.Lists
import tech.aroma.data.AromaGenerators.Applications
import tech.aroma.data.sql.asUUID
import tech.aroma.data.sql.toTimestamp
import tech.aroma.thrift.Application
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import java.sql.ResultSet
import java.util.*

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

        app = Applications.application
        appId = app.applicationId
        orgId = app.organizationId
        owners = app.owners

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
}