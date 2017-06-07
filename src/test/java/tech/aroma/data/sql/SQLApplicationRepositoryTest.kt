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
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcOperations
import sir.wellington.alchemy.collections.lists.Lists
import tech.aroma.data.AromaGenerators
import tech.aroma.data.AromaGenerators.Applications
import tech.aroma.data.invalidArg
import tech.aroma.data.operationError
import tech.aroma.data.sql.SQLStatements.Deletes
import tech.aroma.data.sql.SQLStatements.Inserts
import tech.aroma.data.sql.SQLStatements.Queries
import tech.aroma.thrift.Application
import tech.aroma.thrift.exceptions.DoesNotExistException
import tech.sirwellington.alchemy.generator.AlchemyGenerator
import tech.sirwellington.alchemy.generator.BooleanGenerators.Companion.booleans
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.uuids
import tech.sirwellington.alchemy.generator.one
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import kotlin.test.assertEquals

@RunWith(AlchemyTestRunner::class)
@Repeat
class SQLApplicationRepositoryTest
{
    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var serializer: DatabaseSerializer<Application>

    private lateinit var app: Application
    private lateinit var appId: String
    private lateinit var orgId: String
    private lateinit var apps: List<Application>

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

        apps = CollectionGenerators.listOf(AlchemyGenerator { Applications.application }, 10)
    }

    @Test
    fun testSaveApp()
    {
        val statement = Inserts.APPLICATION

        instance.saveApplication(app)

        verify(serializer).save(app, statement, database)

        val sqlToDeleteNonOwners = Deletes.APPLICATION_NON_OWNERS
        verify(database).update(sqlToDeleteNonOwners, appId.toUUID(), app.owners.toCommaSeparatedList())

        app.owners.forEach { owner ->
            val insertOwner = Inserts.APPLICATION_OWNER
            verify(database).update(insertOwner, appId.toUUID(), owner.toUUID())
        }
    }

    @DontRepeat
    @Test
    fun testSaveAppWhenDatabaseFails()
    {
        val statement = Inserts.APPLICATION

        whenever(serializer.save(app, statement, database))
                .thenThrow(RuntimeException())

        assertThrows {
            instance.saveApplication(app)
        }.isInstanceOf(TException::class.java)
    }

    @Test
    fun testSaveAppOwnerWhenDatabaseFails()
    {
        val owners = app.owners.toMutableList()

        database.setupForFailure()

        instance.saveApplication(app)

        val insertApp = Inserts.APPLICATION
        verify(serializer).save(app, insertApp, database)

        owners.forEach { owner ->
            val sql = Inserts.APPLICATION_OWNER

            verify(database).update(sql, appId.toUUID(), owner.toUUID())
        }
    }

    @DontRepeat
    @Test
    fun testSaveAppWithBadArgs()
    {
        assertThrows {
            val emptyApp = Application()
            instance.saveApplication(emptyApp)
        }.invalidArg()

        assertThrows {
            val appWithInvalidId = Application(app)
                    .setApplicationId(one(alphabeticStrings()))

            instance.saveApplication(appWithInvalidId)
        }

        assertThrows {
            val appWithoutOwners = Application(app)
            appWithoutOwners.owners.clear()

            instance.saveApplication(appWithoutOwners)
        }

        assertThrows {
            val appWithInvalidOwners = Application(app)
            appWithInvalidOwners.owners = CollectionGenerators.listOf(alphabeticStrings()).toSet()
            instance.saveApplication(appWithInvalidOwners)
        }
    }

    @Test
    fun testDeleteApp()
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
        }.invalidArg()

        assertThrows {
            instance.deleteApplication(badId)
        }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testDeleteAppWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.deleteApplication(appId) }
                .operationError()
    }

    @Test
    fun testGetAppById()
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
        assertThrows { instance.getById("") }.invalidArg()
        assertThrows { instance.getById(badId) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testGetByIdWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.getById(appId) }.operationError()
    }

    @Test
    fun testGetByIdWhenAppDoesNotExist()
    {
        val sql = Queries.SELECT_APPLICATION

        whenever(database.queryForObject(sql, serializer, appId.toUUID()))
                .thenThrow(EmptyResultDataAccessException(0))

        assertThrows { instance.getById(appId) }
                .isInstanceOf(DoesNotExistException::class.java)
    }

    @Test
    fun testContainsApp()
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
        assertThrows { instance.containsApplication("") }.invalidArg()
        assertThrows { instance.containsApplication(badId) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testContainsAppWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.containsApplication(appId) }
                .operationError()
    }

    @Test
    fun testGetAppsOwnedBy()
    {
        val query = Queries.SELECT_APPLICATION_BY_OWNER
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
        assertThrows { instance.getApplicationsOwnedBy("") }.invalidArg()
        assertThrows { instance.getApplicationsOwnedBy(badId) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testGetAppsOwnedByWhenDatabaseFails()
    {
        database.setupForFailure()

        val owner = one(uuids)

        assertThrows { instance.getApplicationsOwnedBy(owner) }
                .operationError()
    }

    @Test
    fun testGetAppsByOrg()
    {
        val query = Queries.SELECT_APPLICATION_BY_ORGANIZATION

        whenever(database.query(query, serializer, orgId.toUUID()))
                .thenReturn(apps)

        val result = instance.getApplicationsByOrg(orgId)
        assertEquals(apps, result)
    }

    @DontRepeat
    @Test
    fun testGetApplicationsByOrgWithBadArgs()
    {
        assertThrows { instance.getApplicationsByOrg("") }.invalidArg()
        assertThrows { instance.getApplicationsByOrg(badId) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testGetAppsByOrgWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.getApplicationsByOrg(orgId) }
                .operationError()
    }

    @Test
    fun testSearchByName()
    {
        val query = Queries.SEARCH_APPLICATION_BY_NAME

        val searchTerm = one(alphabeticStrings())

        whenever(database.query(query, serializer, "%$searchTerm%"))
                .thenReturn(apps)

        val result = instance.searchByName(searchTerm)
        assertEquals(apps, result)
    }

    @DontRepeat
    @Test
    fun testSearchByNameWithBadArgs()
    {
        assertThrows { instance.searchByName("") }.invalidArg()
        assertThrows { instance.searchByName("2") }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testSearchByNameWhenDatabaseFails()
    {
        database.setupForFailure()

        val term = badId

        assertThrows { instance.searchByName(term) }.operationError()
    }

    @Test
    fun testGetRecentlyCreated()
    {
        val query = Queries.SELECT_RECENT_APPLICATION

        whenever(database.query(query, serializer)).thenReturn(apps)

        val result = instance.recentlyCreated

        assertEquals(apps, result)
    }

    @DontRepeat
    @Test
    fun testGetRecentlyCreatedWhenDatabaseFails()
    {
        database.setupForFailure()

        assertThrows { instance.recentlyCreated }.operationError()
    }
}