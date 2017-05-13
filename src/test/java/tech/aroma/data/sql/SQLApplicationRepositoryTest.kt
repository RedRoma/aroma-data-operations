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

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.apache.thrift.TException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import sir.wellington.alchemy.collections.lists.Lists
import tech.aroma.data.AromaGenerators
import tech.aroma.data.AromaGenerators.Applications
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.Application
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.BooleanGenerators.booleans
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC
import kotlin.test.assertEquals

@RunWith(AlchemyTestRunner::class)
class SQLApplicationRepositoryTest
{
    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var serializer: DatabaseSerializer<Application>

    private lateinit var app: Application
    private lateinit var appId: String
    private lateinit var orgId: String

    @GenerateString(ALPHABETIC)
    private lateinit var badId: String

    private lateinit var instance: SQLApplicationRepository

    @Before
    fun setup()
    {
        instance = SQLApplicationRepository(database, serializer)

        app = AromaGenerators.Applications.application
        appId = app.applicationId
        orgId = app.organizationId
    }

    @Test
    fun `save application`()
    {
        val statement = Inserts.APPLICATION

        instance.saveApplication(app)

        verify(serializer).save(app, null, statement, database)

        app.owners.forEach { owner ->
            val insertOwner = Inserts.APPLICATION_OWNER
            verify(database).update(insertOwner, appId.toUUID(), owner.toUUID())
        }
    }

    @DontRepeat
    @Test
    fun `save application when database fails`()
    {
        val statement = Inserts.APPLICATION

        whenever(serializer.save(app, null, statement, database))
                .thenThrow(RuntimeException())

        assertThrows {
            instance.saveApplication(app)
        }.isInstanceOf(TException::class.java)
    }

    @Test
    fun `save application owner when database fails`()
    {
        val owners = app.owners.toMutableList()
        val failingOwner = owners.removeAt(0)
        val statement = Inserts.APPLICATION_OWNER

        whenever(database.update(statement, appId.toUUID(), failingOwner.toUUID()))
                .thenThrow(RuntimeException())

        instance.saveApplication(app)

        val insertApp = Inserts.APPLICATION
        verify(serializer).save(app, null, insertApp, database)

        owners.forEach { owner ->
            val sql = Inserts.APPLICATION_OWNER

            verify(database).update(sql, appId.toUUID(), owner.toUUID())
        }
    }

    @DontRepeat
    @Test
    fun `save application with bad args`()
    {
        assertThrows {
            val emptyApp = Application()
            instance.saveApplication(emptyApp)
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            val appWithInvalidId = Application(app)
                    .setApplicationId(one(alphabeticString()))

            instance.saveApplication(appWithInvalidId)
        }

        assertThrows {
            val appWithoutOwners = Application(app)
            appWithoutOwners.owners.clear()

            instance.saveApplication(appWithoutOwners)
        }

        assertThrows {
            val appWithInvalidOwners = Application(app)
            appWithInvalidOwners.owners = CollectionGenerators.listOf { alphabeticString().get() }.toSet()

            instance.saveApplication(appWithInvalidOwners)
        }
    }

    @Test
    fun `delete application`()
    {
        val deleteAppSQL = Deletes.APPLICATION
        val deleteOwnersSQL = Deletes.APPLICATION_OWNERS
        val selectAppSQL = Queries.SELECT_APPLICATION

        whenever(database.queryForObject(selectAppSQL, serializer, appId.toUUID()))
                .thenReturn(app)

        instance.deleteApplication(appId)

        verify(database).update(deleteAppSQL, appId.toUUID())
        verify(database).update(deleteOwnersSQL, appId.toUUID())
    }

    @DontRepeat
    @Test
    fun testDeleteAppWithBadArgs()
    {
        assertThrows {
            instance.deleteApplication("")
        }.isInstanceOf(InvalidArgumentException::class.java)

        assertThrows {
            instance.deleteApplication(badId)
        }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @Test
    fun `get application by ID`()
    {
        val query = Queries.SELECT_APPLICATION

        whenever(database.queryForObject(query, serializer, appId.toUUID()))
                .thenReturn(app)

        val result = instance.getById(appId)

        assertEquals(app, result)
    }

    @DontRepeat
    @Test
    fun testGetByIdWithBadArgs()
    {
        assertThrows { instance.getById("") }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.getById(badId) }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @Test
    fun `contains app`()
    {
        val query = Queries.CHECK_APPLICATION
        val exists = one(booleans())

        whenever(database.queryForObject(query, Boolean::class.java, appId.toUUID()))
                .thenReturn(exists)

        val result = instance.containsApplication(appId)

        assertEquals(exists, result)
    }

    @DontRepeat
    @Test
    fun testContainsAppWithBadArgs()
    {
        assertThrows { instance.containsApplication("") }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.containsApplication(badId) }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @Test
    fun `get applications owned by`()
    {
        val query = Queries.SELECT_APPLICATION_BY_OWNER
        val apps = CollectionGenerators.listOf { Applications.application }
        val owner = Lists.oneOf(app.owners.toList())

        whenever(database.query(query, serializer, owner.toUUID()))
                .thenReturn(apps)

        val result = instance.getApplicationsOwnedBy(owner)

        assertEquals(apps, result)
    }

    @DontRepeat
    @Test
    fun testGetApplicationsOwnedByWithBadArgs()
    {
        assertThrows { instance.getApplicationsOwnedBy("") }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.getApplicationsOwnedBy(badId) }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @Test
    fun `get applications by org`()
    {
        val query = Queries.SELECT_APPLICATION_BY_ORGANIZATION
        val apps = CollectionGenerators.listOf { Applications.application }

        whenever(database.query(query, serializer, orgId.toUUID()))
                .thenReturn(apps)

        val result = instance.getApplicationsByOrg(orgId)
        assertEquals(apps, result)
    }

    @DontRepeat
    @Test
    fun testGetApplicationsByOrgWithBadArgs()
    {
        assertThrows { instance.getApplicationsByOrg("") }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.getApplicationsByOrg(badId) }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @Test
    fun `search by name`()
    {
        val query = Queries.SEARCH_APPLICATION_BY_NAME
        val apps = CollectionGenerators.listOf { Applications.application }

        val searchTerm = one(alphabeticString())

        whenever(database.query(query, serializer, "%$searchTerm%"))
                .thenReturn(apps)

        val result = instance.searchByName(searchTerm)
        assertEquals(apps, result)
    }

    @Test
    fun testSearchByNameWithBadArgs()
    {
        assertThrows { instance.searchByName("") }.isInstanceOf(InvalidArgumentException::class.java)
        assertThrows { instance.searchByName("2") }.isInstanceOf(InvalidArgumentException::class.java)
    }

    @Test
    fun `get recently created`()
    {
        val query = Queries.SELECT_RECENT_APPLICATION
        val apps = CollectionGenerators.listOf { Applications.application }

        whenever(database.query(query, serializer))
                .thenReturn(apps)

        val result = instance.recentlyCreated

        assertEquals(apps, result)
    }
}