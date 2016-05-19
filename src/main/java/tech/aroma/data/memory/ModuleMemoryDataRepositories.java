/*
 * Copyright 2016 RedRoma, Inc.
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

 
package tech.aroma.data.memory;


import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.CredentialRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.InboxRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.MessageRepository;
import tech.aroma.data.OrganizationRepository;
import tech.aroma.data.ReactionRepository;
import tech.aroma.data.TokenRepository;
import tech.aroma.data.UserPreferencesRepository;
import tech.aroma.data.UserRepository;

/**
 * Provides Guice bindings for the Banana Data Repositories that are in-memory.
 * 
 * @author SirWellington
 */
public final class ModuleMemoryDataRepositories extends AbstractModule
{
    private final static Logger LOG = LoggerFactory.getLogger(ModuleMemoryDataRepositories.class);

    @Override
    protected void configure()
    {
        bind(ActivityRepository.class).to(MemoryActivityRepository.class).asEagerSingleton();
        bind(ApplicationRepository.class).to(MemoryApplicationRepository.class).asEagerSingleton();
        bind(CredentialRepository.class).to(MemoryCredentialsRepository.class).asEagerSingleton();
        bind(FollowerRepository.class).to(MemoryFollowerRepository.class).asEagerSingleton();
        bind(InboxRepository.class).to(MemoryInboxRepository.class).asEagerSingleton();
        bind(MediaRepository.class).to(MemoryMediaRepository.class).asEagerSingleton();
        bind(MessageRepository.class).to(MemoryMessageRepository.class).asEagerSingleton();
        bind(OrganizationRepository.class).to(MemoryOrganizationRepository.class).asEagerSingleton();
        bind(ReactionRepository.class).to(MemoryReactionRepository.class).asEagerSingleton();
        bind(TokenRepository.class).to(MemoryTokenRepository.class).asEagerSingleton();
        bind(UserRepository.class).to(MemoryUserRepository.class).asEagerSingleton();
        bind(UserPreferencesRepository.class).to(MemoryUserPreferencesRepository.class).asEagerSingleton();
    }

}
