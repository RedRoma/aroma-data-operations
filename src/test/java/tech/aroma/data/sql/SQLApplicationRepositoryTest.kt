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
import tech.aroma.data.AromaGenerators
import tech.aroma.data.sql.SQLStatements.Inserts
import tech.aroma.thrift.Application
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat

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
}