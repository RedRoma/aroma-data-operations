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
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.ApplicationRepository
import tech.aroma.data.assertions.RequestAssertions.*
import tech.aroma.data.sql.SQLStatements.Inserts
import tech.aroma.thrift.Application
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.OperationFailedException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthGreaterThanOrEqualTo
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
                .`is`(validApplication())

        val insertApp = Inserts.APPLICATION

        try
        {
            serializer.save(application, null, insertApp, database)
        }
        catch (ex: Exception)
        {
            val message = "Failed to save Application [$application] in Database"
            LOG.error(message, ex)
            throw OperationFailedException("$message | ${ex.message}")
        }

        val appId = application.applicationId
        application.owners.forEach { this.insertOwner(appId, it) }

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

    }

    override fun getById(applicationId: String): Application
    {
        checkAppId(applicationId)

        return Application()
    }

    override fun containsApplication(applicationId: String): Boolean
    {
        checkAppId(applicationId)

        return false
    }

    override fun getApplicationsOwnedBy(userId: String): MutableList<Application>
    {
        checkUserId(userId)

        return mutableListOf()
    }


    override fun getApplicationsByOrg(orgId: String): MutableList<Application>
    {
        checkOrgId(orgId)

        return mutableListOf()
    }


    override fun searchByName(searchTerm: String): MutableList<Application>
    {
        checkSearchTerm(searchTerm)

        return mutableListOf()
    }

    override fun getRecentlyCreated(): MutableList<Application>
    {
        return mutableListOf()
    }

    private fun checkAppId(appId: String)
    {
        checkThat(appId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validApplicationId())
    }

    private fun checkOrgId(orgId: String)
    {
        checkThat(orgId)
                .usingMessage("Invalid Org ID: " + orgId)
                .`is`(StringAssertions.validUUID())
    }

    private fun checkSearchTerm(searchTerm: String)
    {
        checkThat(searchTerm)
                .throwing(InvalidArgumentException::class.java)
                .usingMessage("Search term cannot be empty")
                .`is`(nonEmptyString())
                .usingMessage("Search term must have at least 2 characters")
                .`is`(stringWithLengthGreaterThanOrEqualTo(2))
    }

    private fun checkUserId(userId: String)
    {
        checkThat(userId)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validUserId())
    }
}