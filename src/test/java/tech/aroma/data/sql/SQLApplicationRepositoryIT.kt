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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isEmpty
import org.junit.*
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.test.junit.runners.*

import org.junit.Assert.*
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.AromaGenerators.Applications
import tech.aroma.data.sql.serializers.ApplicationSerializer
import tech.aroma.thrift.Application
import tech.aroma.thrift.exceptions.DoesNotExistException
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
class SQLApplicationRepositoryIT
{
    companion object
    {
        private lateinit var database: JdbcOperations

        @JvmStatic
        @BeforeClass
        fun setupClass()
        {
            database = TestingResources.connectToDatabase()
        }
    }

    private val serializer = ApplicationSerializer()

    @GenerateString(UUID)
    private lateinit var ownerId: String

    private lateinit var app: Application
    private lateinit var appId: String
    private lateinit var apps: List<Application>

    private lateinit var instance: SQLApplicationRepository


    @Before
    fun setup()
    {
        instance = SQLApplicationRepository(database, serializer)

        app = Applications.application
        appId = app.applicationId
        app.owners.add(ownerId)

        app.totalMessagesSent = 0
        app.unsetTotalMessagesSent()
        app.followers = mutableSetOf()

        apps = CollectionGenerators.listOf({ Applications.application }, 5)
        apps.forEach { it.owners.add(ownerId) }
    }

    @After
    fun destroy()
    {
        try
        {
            instance.deleteApplication(appId)
        }
        catch (ex: Exception)
        {

        }

        try
        {
            apps.map(Application::applicationId).forEach(instance::deleteApplication)
        }
        catch (ex: Exception)
        {

        }
    }

    @Test
    fun testSave()
    {
        instance.saveApplication(app)

        assertTrue { instance.containsApplication(appId) }
    }

    @Test
    fun testDeleteApp()
    {
        instance.saveApplication(app)

        instance.deleteApplication(appId)

        assertFalse { instance.containsApplication(appId) }
    }

    @Test
    fun testDeleteAppWhenDoesNotExist()
    {
        assertThrows {
            instance.deleteApplication(appId)
        }.isInstanceOf(DoesNotExistException::class.java)
    }

    @Test
    fun testGetById()
    {
        instance.saveApplication(app)
        assertTrue { instance.containsApplication(appId) }

        val result = instance.getById(appId)
        //Time of provisioning is determined by back end
        app.timeOfProvisioning = result.timeOfProvisioning

        assertEquals(app, result)
    }

    fun testGetByIdWhenNotExists()
    {
        assertThrows { instance.getById(appId) }
                .isInstanceOf(DoesNotExistException::class.java)
    }

    @Test
    fun testContainsApp()
    {
        assertFalse { instance.containsApplication(appId) }

        instance.saveApplication(app)

        assertTrue { instance.containsApplication(appId) }
    }

    @Test
    fun testGetAppsOwnedBy()
    {
        apps.forEach(instance::saveApplication)
        apps.forEach { it.timeOfProvisioning = 0L}
        apps.forEach { it.followers = mutableSetOf() }
        apps.forEach { it.totalMessagesSent = 0L }

        val results = instance.getApplicationsOwnedBy(ownerId)
        results.forEach { it.timeOfProvisioning = 0L }
        results.forEach { it.followers = mutableSetOf() }

        assertEquals(apps.toSet(), results.toSet())
    }

    fun testGetAppsOwnedByWhenEmpty()
    {
        val result = instance.getApplicationsOwnedBy(ownerId)
        assertThat(result, isEmpty)
    }
}