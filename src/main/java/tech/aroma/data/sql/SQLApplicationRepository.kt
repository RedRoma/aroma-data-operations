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

import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.ApplicationRepository
import tech.aroma.data.assertions.RequestAssertions.validApplication
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.Application
import tech.aroma.thrift.exceptions.DoesNotExistException
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.*
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
internal class SQLApplicationRepository
@Inject constructor(val database: JdbcOperations,
                    val serializer: DatabaseSerializer<Application>) : ApplicationRepository
{

    private val LOG = LoggerFactory.getLogger(this.javaClass)

    override fun saveApplication(application: Application)
    {
        checkThat(application)
                .throwing(InvalidArgumentException::class.java)
                .isA(validApplication())

        val insertApp = Inserts.APPLICATION

        try
        {
            serializer.save(application, insertApp, database)
        }
        catch (ex: Exception)
        {
            failWithMessage("Failed to save Application [$application] in Database", ex)
        }

        updateAppOwnersFor(application)
    }

    private fun updateAppOwnersFor(app: Application)
    {
        val appId = app.applicationId

        app.owners.forEach { this.insertOwner(appId, it) }

        val deleteNonOwnersSQL = Deletes.APPLICATION_NON_OWNERS

        try
        {
            database.update(deleteNonOwnersSQL, appId.toUUID(), app.owners.toCommaSeparatedList())
        }
        catch(ex: Exception)
        {
            val message = "Failed to remove all non-owners for App [$appId]"
            LOG.warn(message, ex)
        }


    }

    private fun insertOwner(appId: String, owner: String)
    {
        val insertOwner = Inserts.APPLICATION_OWNER

        try
        {
            database.update(insertOwner, appId.toUUID(), owner.toUUID())
        }
        catch(ex: Exception)
        {
            val message = "Failed to save Owner [$owner] for App [$appId]"
            LOG.warn(message, ex)
        }
    }

    override fun deleteApplication(applicationId: String)
    {
        checkAppId(applicationId)

        val app = this.getById(applicationId)

        val deleteAppSQL = Deletes.APPLICATION
        val deleteOwnersSQL = Deletes.APPLICATION_OWNERS
        val appId = applicationId.toUUID()

        try
        {
            database.update(deleteAppSQL, appId)
            database.update(deleteOwnersSQL, appId)
        }
        catch (ex: Exception)
        {
            tryToRestoreApp(app)

            val message = "Failed to delete Application: [$appId]"
            failWithMessage(message, ex)
        }
    }

    private fun tryToRestoreApp(app: Application)
    {
        try
        {
            saveApplication(app)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to restore App [$app]", ex)
        }
    }

    override fun getById(applicationId: String): Application
    {
        checkAppId(applicationId)

        val query = Queries.SELECT_APPLICATION
        val appId = applicationId.toUUID()

        return try
        {
            database.queryForObject(query, serializer, appId)
        }
        catch (ex: EmptyResultDataAccessException)
        {
            val message = "No such App with ID: $appId"
            LOG.warn(message, ex)
            throw DoesNotExistException("$message | ${ex.message}")
        }
        catch(ex: Exception)
        {
            val message = "Failed to find an App by ID [$appId]"
            failWithMessage(message, ex)
        }
    }

    override fun containsApplication(applicationId: String): Boolean
    {
        checkAppId(applicationId)

        val query = Queries.CHECK_APPLICATION
        val appId = applicationId.toUUID()

        return try
        {
            database.queryForObject(query, Boolean::class.java, appId)
        }
        catch (ex: Exception)
        {
            val message = "Failed to check if App exists: [$appId]"
            failWithMessage(message, ex)
        }

    }

    override fun getApplicationsOwnedBy(userId: String): MutableList<Application>
    {
        checkUserId(userId)

        val query = Queries.SELECT_APPLICATION_BY_OWNER

        return try
        {
            database.query(query, serializer, userId.toUUID())
        }
        catch(ex: Exception)
        {
            val message = "Could not query database for apps owned by [$userId]"
            failWithMessage(message, ex)
        }
    }


    override fun getApplicationsByOrg(orgId: String): MutableList<Application>
    {
        checkOrgId(orgId)

        val query = Queries.SELECT_APPLICATION_BY_ORGANIZATION

        return try
        {
            database.query(query, serializer, orgId.toUUID())
        }
        catch(ex: Exception)
        {
            val message = "Failed to get applications by Org: [$orgId]"
            failWithMessage(message, ex)
        }
    }

    override fun searchByName(searchTerm: String): MutableList<Application>
    {
        checkSearchTerm(searchTerm)

        val query = Queries.SEARCH_APPLICATION_BY_NAME
        val token = "%$searchTerm%"

        return try
        {
            database.query(query, serializer, token)
        }
        catch (ex: Exception)
        {
            val message = "Failed to search for Apps with term: [$searchTerm]"
            failWithMessage(message, ex)
        }

    }

    override fun getRecentlyCreated(): MutableList<Application>
    {
        val query = Queries.SELECT_RECENT_APPLICATION

        return try
        {
            database.query(query, serializer)
        }
        catch (ex: Exception)
        {
            val message = "Failed to query for recently created apps"
            failWithMessage(message, ex)
        }

    }

    private fun checkOrgId(orgId: String)
    {
        checkThat(orgId)
                .throwing(InvalidArgumentException::class.java)
                .usingMessage("Invalid Org ID: " + orgId)
                .isA(nonEmptyString())
                .isA(validUUID())
    }

    private fun checkSearchTerm(searchTerm: String)
    {
        checkThat(searchTerm)
                .throwing(InvalidArgumentException::class.java)
                .usingMessage("Search term cannot be empty")
                .isA(nonEmptyString())
                .usingMessage("Search term must have at least 2 characters")
                .isA(stringWithLengthGreaterThanOrEqualTo(2))
    }

}