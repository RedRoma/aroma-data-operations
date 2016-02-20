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

package tech.aroma.data.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.InboxRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.MessageRepository;
import tech.aroma.data.OrganizationRepository;
import tech.aroma.data.TokenRepository;
import tech.aroma.data.UserRepository;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;


/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class ModuleCassandraDataRepositoriesTest 
{
    
    @Mock(answer = RETURNS_MOCKS)
    private Cluster cluster;
    
    @Mock
    private Session mockSession;
    
    private AbstractModule binder = new AbstractModule()
    {
        @Override
        protected void configure()
        {
        }
        
        @Provides
        Session provideSession()
        {
            return mockSession;
        }
        
        @Provides
        Cluster provideCluster()
        {
            return cluster;
        }
        
        @Provides
        QueryBuilder provideQueryBuilder()
        {
            return new QueryBuilder(cluster);
        }
        
    };
    
    private ModuleCassandraDataRepositories instance;

    @Before
    public void setUp()
    {
        instance = new ModuleCassandraDataRepositories();
        
    }

    @Test
    public void testConfigure()
    {
        Injector injector = Guice.createInjector(binder, instance);
        assertThat(injector, notNullValue());
        
        ApplicationRepository appRepo = injector.getInstance(ApplicationRepository.class);
        assertThat(appRepo, notNullValue());
        
        FollowerRepository followerRepo = injector.getInstance(FollowerRepository.class);
        assertThat(followerRepo, notNullValue());
        
        InboxRepository inboxRepo = injector.getInstance(InboxRepository.class);
        assertThat(inboxRepo, notNullValue());
        
        MediaRepository mediaRepo = injector.getInstance(MediaRepository.class);
        assertThat(mediaRepo, notNullValue());
        
        MessageRepository messageRepo = injector.getInstance(MessageRepository.class);
        assertThat(messageRepo, notNullValue());
        
        OrganizationRepository orgRepo = injector.getInstance(OrganizationRepository.class);
        assertThat(orgRepo, notNullValue());
        
        TokenRepository tokenRepo = injector.getInstance(TokenRepository.class);
        assertThat(tokenRepo, notNullValue());
        
        UserRepository userRepo = injector.getInstance(UserRepository.class);
        assertThat(userRepo, notNullValue());
    }

}
