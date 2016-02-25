/*
 * Copyright 2016 RedRoma.
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

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.List;
import java.util.function.Function;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraFollowerRepositoryIT 
{
    
    private static Session session;
    private static QueryBuilder queryBuilder;

    @BeforeClass
    public static void begin()
    {
        session = TestCassandraProviders.getTestSession();
        queryBuilder = TestCassandraProviders.getQueryBuilder();
    }

    private CassandraFollowerRepository instance;
    
    private Function<Row, User> userMapper;
    private Function<Row, Application>  appMapper;
    
    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(UUID)
    private String userId;
    
    @GeneratePojo
    private Application app;
    
    @GeneratePojo
    private User user;
    
    @Before
    public void setUp() throws Exception
    {
        userMapper = Mappers.userMapper();
        appMapper = Mappers.appMapper();
        
        instance = new CassandraFollowerRepository(session, queryBuilder, userMapper, appMapper);
        
        setupData();
        setupMocks();
    }
    
    @After
    public void cleanUp() throws Exception
    {
        instance.deleteFollowing(userId, appId);
    }

    private void setupData() throws Exception
    {
        app.applicationId = appId;
        user.userId = userId;
        
        app.unsetOrganizationId();
        
    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testSaveFollowing() throws Exception
    {
        instance.saveFollowing(user, app);
        
        assertThat(instance.followingExists(userId, appId), is(true));
    }

    @Test
    public void testDeleteFollowing() throws Exception
    {
        instance.saveFollowing(user, app);
        
        instance.deleteFollowing(userId, appId);
        assertThat(instance.followingExists(userId, appId), is(false));
    }   
    
    @Test
    public void testDeleteFollowingWhenNoneExist() throws Exception
    {
        instance.deleteFollowing(userId, appId);
    }

    @Test
    public void testFollowingExists() throws Exception
    {
        assertThat(instance.followingExists(userId, appId), is(false));
        
        instance.saveFollowing(user, app);
        
        assertThat(instance.followingExists(userId, appId), is(true));
    }

    @Test
    public void testGetApplicationsFollowedBy() throws Exception
    {
        instance.saveFollowing(user, app);
        
        List<Application> apps = instance.getApplicationsFollowedBy(userId);
        assertThat(apps, notNullValue());
        assertThat(apps, not(empty()));
        assertThat(apps.size(), is(1));
        
        Application savedApp = Lists.oneOf(apps);
        assertThat(savedApp.applicationId, is(appId));
        assertThat(savedApp.name, is(app.name));
    }
    
    
    @Test
    public void testGetApplicationsFollowedByWhenNone() throws Exception
    {
        List<Application> apps = instance.getApplicationsFollowedBy(userId);
        assertThat(apps, notNullValue());
        assertThat(apps, is(empty()));
    }

    @Test
    public void testGetApplicationFollowers() throws Exception
    {
        instance.saveFollowing(user, app);
        
        List<User> followers = instance.getApplicationFollowers(appId);
        assertThat(followers, notNullValue());
        assertThat(followers, not(empty()));
        assertThat(followers.size(), is(1));
        
        User follower = Lists.oneOf(followers);
        
        assertThat(follower.userId, is(userId));
        assertThat(follower.firstName, is(user.firstName));
    }

    @Test
    public void testGetApplicationFollowersWhenNone() throws Exception
    {
        List<User> followers = instance.getApplicationFollowers(appId);
        assertThat(followers, notNullValue());
        assertThat(followers, is(empty()));
    }

}