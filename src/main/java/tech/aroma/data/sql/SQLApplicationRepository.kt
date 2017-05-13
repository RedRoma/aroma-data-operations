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
import tech.aroma.data.assertions.RequestAssertions.validApplication
import tech.aroma.data.sql.SQLStatements.Inserts
import tech.aroma.thrift.Application
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.aroma.thrift.exceptions.OperationFailedException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
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


    }

    override fun deleteApplication(applicationId: String)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getById(applicationId: String): Application
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun containsApplication(applicationId: String): Boolean
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getApplicationsOwnedBy(userId: String): MutableList<Application>
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getApplicationsByOrg(orgId: String): MutableList<Application>
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchByName(searchTerm: String): MutableList<Application>
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRecentlyCreated(): MutableList<Application>
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}