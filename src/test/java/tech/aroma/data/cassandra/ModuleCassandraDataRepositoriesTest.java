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

package tech.aroma.data.cassandra;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.datastax.driver.core.*;
import com.google.inject.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.*;
import tech.aroma.thrift.*;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.channels.MobileDevice;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.reactions.Reaction;
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

        ActivityRepository activityRepo = injector.getInstance(ActivityRepository.class);
        assertThat(activityRepo, notNullValue());

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

        ReactionRepository reactionRepo = injector.getInstance(ReactionRepository.class);
        assertThat(reactionRepo, notNullValue());
        
        TokenRepository tokenRepo = injector.getInstance(TokenRepository.class);
        assertThat(tokenRepo, notNullValue());

        UserRepository userRepo = injector.getInstance(UserRepository.class);
        assertThat(userRepo, notNullValue());
        
        UserPreferencesRepository deviceRepo = injector.getInstance(UserPreferencesRepository.class);
        assertThat(deviceRepo, notNullValue());
        
    }

    @Test
    public void testProvideApplicationMapper()
    {
        Function<Row, Application> result = instance.provideApplicationMapper();

        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideEventMapper()
    {
        Function<Row, Event> result = instance.provideEventMapper();

        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideImageMapper()
    {
        Function<Row, Image> result = instance.provideImageMapper();
        
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideMessageMapper()
    {
        Function<Row, Message> result = instance.provideMessageMapper();
        
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideOrganizationMapper()
    {
        Function<Row, Organization> result = instance.provideOrganizationMapper();
        
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideTokenMapper()
    {
        Function<Row, AuthenticationToken> result = instance.provideTokenMapper();
        
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideUserMapper()
    {
        Function<Row, User> result = instance.provideUserMapper();
        
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideReactionsMapper()
    {
        Function<Row, List<Reaction>> result = instance.provideReactionsMapper();
        
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideMobileDeviceMapper()
    {
        Function<Row, Set<MobileDevice>> result = instance.provideMobileDeviceMapper();
        assertThat(result, notNullValue());
    }

}
