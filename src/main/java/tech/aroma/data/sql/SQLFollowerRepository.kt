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

import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.FollowerRepository
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.Application
import tech.aroma.thrift.User
import tech.aroma.thrift.exceptions.InvalidArgumentException
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
internal class SQLFollowerRepository
@Inject constructor(val database: JdbcOperations,
                    val userSerializer: DatabaseSerializer<User>,
                    val appSerializer: DatabaseSerializer<Application>) : FollowerRepository
{
    override fun saveFollowing(user: User, application: Application)
    {
        checkUser(user)
        checkApplication(application)

        val userId = user.userId?.toUUID() ?: throw InvalidArgumentException("Invalid userId: $user")
        val appId = application.applicationId?.toUUID() ?: throw InvalidArgumentException("Invalid appId: $application")
        val sql = Inserts.FOLLOWING

        try
        {
            database.update(sql, appId, userId)
        }
        catch (ex: Exception)
        {
            failWithError("Could not save following for User[$userId] and App[$appId]", ex)
        }
    }

    override fun deleteFollowing(userId: String, applicationId: String)
    {
        checkUserId(userId)
        checkApplicationId(applicationId)

        val sql = Deletes.FOLLOWING

        try
        {
            database.update(sql, applicationId.toUUID(), userId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithError("Could not delete following for User[$userId] and App[$applicationId", ex)
        }
    }

    override fun followingExists(userId: String, applicationId: String): Boolean
    {
        checkUserId(userId)
        checkApplicationId(applicationId)

        val sql = Queries.CHECK_FOLLOWING_EXISTS

        return try
        {
            database.queryForObject(sql, Boolean::class.java, applicationId.toUUID(), userId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithError("Could not check if user [$userId] follows App[$applicationId]", ex)
        }

    }

    override fun getApplicationsFollowedBy(userId: String): MutableList<Application>
    {
        checkUserId(userId)

        val sql = Queries.SELECT_APPS_FOLLOWING

        return try
        {
            database.query(sql, appSerializer, userId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithError("Could not determine apps being followed by User[$userId]", ex)
        }
    }

    override fun getApplicationFollowers(applicationId: String): MutableList<User>
    {
        checkApplicationId(applicationId)

        val sql = Queries.SELECT_APP_FOLLOWERS

        return try
        {
            database.query(sql, userSerializer, applicationId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithError("Could not determine which ")
        }
    }

}