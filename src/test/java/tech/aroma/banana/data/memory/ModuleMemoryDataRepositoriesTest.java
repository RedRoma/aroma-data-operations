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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.CredentialRepository;
import tech.aroma.banana.data.FollowerRepository;
import tech.aroma.banana.data.InboxRepository;
import tech.aroma.banana.data.MediaRepository;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.data.UserRepository;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class ModuleMemoryDataRepositoriesTest 
{

    private ModuleMemoryDataRepositories instance;
    
    @Before
    public void setUp()
    {
        instance = new ModuleMemoryDataRepositories();
    }

    @Test
    public void testConfigure()
    {
        Injector injector = Guice.createInjector(instance);
        
        ApplicationRepository appRepo = injector.getInstance(ApplicationRepository.class);
        assertThat(appRepo, notNullValue());
        
        CredentialRepository credentialsRepo = injector.getInstance(CredentialRepository.class);
        assertThat(credentialsRepo, notNullValue());
        
        FollowerRepository followRepo = injector.getInstance(FollowerRepository.class);
        assertThat(followRepo, notNullValue());
        
        MediaRepository mediaRepo = injector.getInstance(MediaRepository.class);
        assertThat(mediaRepo, notNullValue());
        
        MessageRepository messageRepo = injector.getInstance(MessageRepository.class);
        assertThat(messageRepo, notNullValue());
        
        InboxRepository inboxRepo = injector.getInstance(InboxRepository.class);
        assertThat(inboxRepo, notNullValue());
        
        UserRepository userRepo = injector.getInstance(UserRepository.class);
        assertThat(userRepo, notNullValue());
     }

}
