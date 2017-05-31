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

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.*
import tech.aroma.data.AromaGenerators.Applications
import tech.aroma.data.sql.serializers.ApplicationSerializer
import tech.aroma.data.sql.serializers.UserSerializer
import tech.aroma.thrift.Application
import tech.aroma.thrift.User
import tech.aroma.thrift.generators.UserGenerators.users
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.CollectionGenerators.listOf
import tech.sirwellington.alchemy.generator.StringGenerators.uuids
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
@IntegrationTest
class SQLFollowerRepositoryIT
{

    private companion object
    {
        @JvmStatic lateinit var database: JdbcOperations

        @JvmStatic
        @BeforeClass
        fun setupTest()
        {
            database = TestingResources.connectToDatabase()
        }
    }

    private lateinit var user: User
    private lateinit var app: Application

    private val userId get() = user.userId
    private val appId get() = app.applicationId

    private lateinit var appIds: List<String>
    private lateinit var userIds: List<String>

    private val userSerializer = UserSerializer()
    private val appSerializer = ApplicationSerializer()

    private lateinit var appRepo: SQLApplicationRepository
    private lateinit var userRepo: SQLUserRepository
    private lateinit var instance: SQLFollowerRepository

    @Before
    fun setUp()
    {

        setupData()
        setupMocks()

        instance = SQLFollowerRepository(database, appSerializer, userSerializer)

    }

    @After
    fun tearDown()
    {
        instance.deleteFollowing(userId, appId)

        userIds.forEach { instance.deleteFollowing(it, appId) }
        appIds.forEach { instance.deleteFollowing(userId, it) }

        userRepo.deleteUser(userId)
        appRepo.deleteApplication(appId)
    }

    @Test
    fun testSaveFollowing()
    {
        instance.saveFollowing(user, app)

        assertTrue { instance.followingExists(userId, appId) }
    }

    @Test
    fun testSaveFollowingTwice()
    {
        instance.saveFollowing(user, app)

        assertThrows { instance.saveFollowing(user, app) }.operationError()
        assertTrue { instance.followingExists(userId, appId) }
    }

    @Test
    fun testDeleteFollowing()
    {
        instance.saveFollowing(user, app)
        instance.deleteFollowing(userId, appId)

        assertFalse { instance.followingExists(userId, appId) }
    }

    @Test
    fun testDeleteFollowingWhenNone()
    {
        instance.deleteFollowing(userId, appId)
    }

    @Test
    fun testFollowingExists()
    {
        assertFalse { instance.followingExists(userId, appId) }

        instance.saveFollowing(user, app)

        assertTrue { instance.followingExists(userId, appId) }
    }

    @Test
    fun testGetApplicationsFollowedBy()
    {
        instance.saveFollowing(user, app)

        val results = instance.getApplicationsFollowedBy(userId)
                .map { it.timeOfProvisioning = 0; it }

        assertThat(results, notNull and notEmpty)
        assertThat(results, hasElement(app))
    }

    @Test
    fun testGetApplicationsFollowedByWithMultiple()
    {
        appIds.map { Application().setApplicationId(it) }
                .forEach { instance.saveFollowing(user, it) }

        val results = instance.getApplicationsFollowedBy(userId)

        assertThat(results, notEmpty)
        assertThat(results.size, equalTo(appIds.size))

        val resultIds = results.map { it.applicationId }.toSet()
        val expectedIds = appIds.toSet()
        assertThat(resultIds, equalTo(expectedIds))
    }

    @Test
    fun testGetApplicationsFollowedByWhenNone()
    {
        val result = instance.getApplicationsFollowedBy(userId)
        assertThat(result, notNull and isEmpty)
    }

    @Test
    fun testGetApplicationFollowers()
    {
        instance.saveFollowing(user, app)

        val results = instance.getApplicationFollowers(appId)

        assertThat(results, notNull and notEmpty)
        assertThat(results, hasElement(user))
    }

    @Test
    fun testGetApplicationFollowersWithMultiple()
    {
        userIds.map { User().setUserId(it) }
                .forEach { instance.saveFollowing(it, app) }

        val results = instance.getApplicationFollowers(appId)

        val expectedIds = userIds.toSet()
        val resultIds = results.map(User::userId).toSet()

        assertThat(resultIds, equalTo(expectedIds))
    }

    @Test
    fun testGetApplicationFollowersWhenNone()
    {
        val results = instance.getApplicationFollowers(appId)
        assertThat(results, notNull and isEmpty)
    }

    private fun setupData()
    {
        user = one(users())
        app = Applications.application
        app.totalMessagesSent = 0
        app.unsetTotalMessagesSent()
        app.followers = mutableSetOf()
        app.unsetTimeOfProvisioning()
        app.timeOfProvisioning = 0

        userIds = listOf(uuids(), 10)
        appIds = listOf(uuids(), 10)
    }

    private fun setupMocks()
    {
        appRepo = SQLApplicationRepository(database, appSerializer)
        userRepo = SQLUserRepository(database, userSerializer)

        appRepo.saveApplication(app)
        userRepo.saveUser(user)
    }

}