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

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.ActivityRepository
import tech.aroma.data.CredentialRepository
import tech.aroma.data.FollowerRepository
import tech.aroma.data.InboxRepository
import tech.aroma.data.MediaRepository
import tech.aroma.data.MessageRepository
import tech.aroma.data.ReactionRepository
import tech.aroma.data.TokenRepository
import tech.aroma.data.UserPreferencesRepository
import tech.aroma.data.UserRepository
import tech.aroma.data.bind
import tech.aroma.data.hasInstance
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
class ModuleSQLRepositoriesTest
{
    @Mock
    private lateinit var database: JdbcOperations

    private lateinit var instance: ModuleSQLRepositories
    private lateinit var injector: Injector

    val fakeModule = object : AbstractModule()
    {
        override fun configure()
        {
            binder().bind<JdbcOperations>().toInstance(database)
        }

    }

    @Before
    fun setup()
    {
        instance = ModuleSQLRepositories()

        injector = Guice.createInjector(instance, fakeModule)
    }

    @Test
    fun testActivityRepo()
    {
        assertTrue { injector.hasInstance<ActivityRepository>() }
    }

    @Test
    fun testAppRepo()
    {
        assertTrue { injector.hasInstance<UserRepository>() }
    }

    @Test
    fun testHasCredentialsRepo()
    {
        assertTrue { injector.hasInstance<CredentialRepository>() }
    }

    @Test
    fun testHasFollowerRepo()
    {
        assertTrue { injector.hasInstance<FollowerRepository>() }
    }

    @Test
    fun testInboxRepo()
    {
        assertTrue { injector.hasInstance<InboxRepository>() }
    }

    @Test
    fun testHasMediaRepo()
    {
        assertTrue { injector.hasInstance<MediaRepository>() }
    }

    @Test
    fun testMessageRepo()
    {
        assertTrue { injector.hasInstance<MessageRepository>() }
    }

    @Test
    fun testHasReactionRepo()
    {
        assertTrue { injector.hasInstance<ReactionRepository>() }
    }

    @Test
    fun testTokenRepo()
    {
        assertTrue { injector.hasInstance<TokenRepository>() }
    }

    @Test
    fun testUserRepo()
    {
        assertTrue { injector.hasInstance<UserRepository>() }
    }

    @Test
    fun testUserPreferencesRepo()
    {
        assertTrue { injector.hasInstance<UserPreferencesRepository>() }
    }

}