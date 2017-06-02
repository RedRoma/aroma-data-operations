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

import com.google.inject.AbstractModule
import tech.aroma.data.*
import tech.aroma.data.sql.serializers.ModuleSerializers


/**
 * Provides bindings for the SQL Repositories.
 * @author SirWellington
 */
public class ModuleSQLRepositories : AbstractModule()
{
    override fun configure()
    {
        install(ModuleSerializers())

        binder().bind<ActivityRepository>().to<SQLActivityRepository>()
        binder().bind<ApplicationRepository>().to<SQLApplicationRepository>()
        binder().bind<CredentialRepository>().to<SQLCredentialRepository>()
        binder().bind<FollowerRepository>().to<SQLFollowerRepository>()
        binder().bind<InboxRepository>().to<SQLInboxRepository>()
        binder().bind<MediaRepository>().to<SQLMediaRepository>()
        binder().bind<MessageRepository>().to<SQLMessageRepository>()
        binder().bind<OrganizationRepository>().to<SQLOrganizationRepository>()
        binder().bind<TokenRepository>().to<SQLTokenRepository>()
        binder().bind<UserRepository>().to<SQLUserRepository>()
        binder().bind<UserPreferencesRepository>().to<SQLUserPreferencesRepository>()
    }

}