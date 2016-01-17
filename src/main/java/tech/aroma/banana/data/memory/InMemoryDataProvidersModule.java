/*
 * Copyright 2016 Aroma Tech.
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

 
package tech.aroma.banana.data.memory;


import com.google.inject.AbstractModule;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.FollowerRepository;
import tech.aroma.banana.data.InboxRepository;
import tech.aroma.banana.data.MessageRepository;

/**
 * Provides Guice bindings for the Banana Data Repositories that are in-memory.
 * 
 * @author SirWellington
 */
public final class InMemoryDataProvidersModule extends AbstractModule
{
    private final static Logger LOG = LoggerFactory.getLogger(InMemoryDataProvidersModule.class);

    @Override
    protected void configure()
    {
        bind(ApplicationRepository.class).to(ApplicationRepositoryInMemory.class).in(Singleton.class);
        bind(FollowerRepository.class).to(FollowerRepositoryInMemory.class).in(Singleton.class);
        bind(InboxRepository.class).to(InboxRepositoryInMemory.class).in(Singleton.class);
        bind(MessageRepository.class).to(MessageRepositoryInMemory.class).in(Singleton.class);
    }

}
