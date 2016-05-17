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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.CredentialRepository;
import tech.aroma.data.DeviceRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.InboxRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.MessageRepository;
import tech.aroma.data.ReactionRepository;
import tech.aroma.data.UserRepository;
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
        
        ActivityRepository eventRepo = injector.getInstance(ActivityRepository.class);
        assertThat(eventRepo, notNullValue());

        ApplicationRepository appRepo = injector.getInstance(ApplicationRepository.class);
        assertThat(appRepo, notNullValue());
        
        CredentialRepository credentialsRepo = injector.getInstance(CredentialRepository.class);
        assertThat(credentialsRepo, notNullValue());
        
        DeviceRepository deviceRepo = injector.getInstance(DeviceRepository.class);
        assertThat(deviceRepo, notNullValue());
     
        FollowerRepository followRepo = injector.getInstance(FollowerRepository.class);
        assertThat(followRepo, notNullValue());
        
        MediaRepository mediaRepo = injector.getInstance(MediaRepository.class);
        assertThat(mediaRepo, notNullValue());
        
        MessageRepository messageRepo = injector.getInstance(MessageRepository.class);
        assertThat(messageRepo, notNullValue());
        
        InboxRepository inboxRepo = injector.getInstance(InboxRepository.class);
        assertThat(inboxRepo, notNullValue());
        
        ReactionRepository reactionRepo = injector.getInstance(ReactionRepository.class);
        assertThat(reactionRepo, notNullValue());
        
        UserRepository userRepo = injector.getInstance(UserRepository.class);
        assertThat(userRepo, notNullValue());
     }

}
