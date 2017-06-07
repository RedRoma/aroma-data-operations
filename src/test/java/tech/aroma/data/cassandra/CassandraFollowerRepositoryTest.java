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

import java.util.*;
import java.util.function.Function;

import com.datastax.driver.core.*;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static sir.wellington.alchemy.collections.sets.Sets.toSet;
import static tech.aroma.thrift.generators.ApplicationGenerators.applications;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.positiveLongs;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class CassandraFollowerRepositoryTest
{

    @Mock
    private Session cassandra;
    
    @Captor
    private ArgumentCaptor<Statement> statementCaptor;
    
    @Mock
    private Function<Row, User> userMapper;
    @Mock
    private Function<Row, Application> applicationMapper;

    @Mock
    private ResultSet results;

    @Mock
    private Row row;

    @GeneratePojo
    private User user;

    private Application app;
    private String appId;

    @GenerateString(UUID)
    private String userId;

    @GenerateList(Application.class)
    private List<Application> apps;
    
    @GenerateList(User.class)
    private List<User> followers;
    
    private CassandraFollowerRepository instance;
    
    @Before
    public void setUp()
    {
        createStubs();
        
        instance = new CassandraFollowerRepository(cassandra, userMapper, applicationMapper);

        app = one(applications());
        appId = app.applicationId;
        app.unsetOrganizationId();
        user.userId = userId;
    }
    
    private void createStubs()
    {
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenReturn(results);
        
        when(results.one())
            .thenReturn(row);
    }
    
    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new CassandraFollowerRepository(null, userMapper, applicationMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraFollowerRepository(cassandra, null, applicationMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraFollowerRepository(cassandra, userMapper, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSaveFollowing() throws Exception
    {
        instance.saveFollowing(user, app);
        
        verify(cassandra).execute(statementCaptor.capture());
        Statement statementMade = statementCaptor.getValue();
        assertThat(statementMade, notNullValue());
    }

    @Test
    public void testSaveFollowingWhenFails() throws Exception
    {
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenThrow(new IllegalArgumentException());

        assertThrows(() -> instance.saveFollowing(user, app))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testSaveFollowingWithBadArgs() throws Exception
    {

        User emptyUser = new User();
        Application emptyApp = new Application();

        assertThrows(() -> instance.saveFollowing(emptyUser, app))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.saveFollowing(user, emptyApp))
            .isInstanceOf(InvalidArgumentException.class);

        User userWithBadId = user.setUserId(one(alphabeticStrings()));
        Application appWithBadId = app.setApplicationId(one(alphabeticStrings()));

        assertThrows(() -> instance.saveFollowing(userWithBadId, app))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.saveFollowing(user, appWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
    }
    

    @Test
    public void testDeleteFollowing() throws Exception
    {
        instance.deleteFollowing(userId, appId);
        
        verify(cassandra).execute(statementCaptor.capture());
        
        Statement statementMade = statementCaptor.getValue();
        assertThat(statementMade, notNullValue());
    }
    
    @Test
    public void testDeleteFollowingWhenFails() throws Exception
    {
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenThrow(new IllegalArgumentException());
        
        assertThrows(() -> instance.deleteFollowing(userId, appId))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testDeleteFollowingWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteFollowing("", appId))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteFollowing(userId, ""))
            .isInstanceOf(InvalidArgumentException.class);

        String badId = one(alphabeticStrings());
        
        assertThrows(() -> instance.deleteFollowing(badId, appId))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteFollowing(userId, badId))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testFollowingExists() throws Exception
    {
        when(row.getLong(0))
            .thenReturn(0L);
        
        boolean result = instance.followingExists(userId, appId);
        assertThat(result, is(false));
        
        long count = one(positiveLongs());
        when(row.getLong(0)).thenReturn(count);
        result = instance.followingExists(userId, appId);
        assertThat(result, is(true));
    }
    
    @DontRepeat
    @Test
    public void testFollowingExistsWhenFails() throws Exception
    {
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenThrow(new RuntimeException());
        
        assertThrows(() -> instance.followingExists(userId, appId))
            .isInstanceOf(TException.class);
    }
    
    @Test
    public void testFollowingExistsWithBadArgs() throws Exception
    {
        String badId = one(alphabeticStrings());

        assertThrows(() -> instance.followingExists("", appId))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.followingExists(userId, ""))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.followingExists(badId, appId))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.followingExists(userId, badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetApplicationsFollowedBy() throws Exception
    {
        Map<String, Row> rows = Maps.create();
        
        for (Application app : apps)
        {
            Row mockRow = mock(Row.class);
            
            when(applicationMapper.apply(mockRow))
                .thenReturn(app);
            
            rows.put(app.applicationId, mockRow);
        }
        
        when(results.iterator())
            .thenReturn(rows.values().iterator());
        
        Set<Application> result = toSet(instance.getApplicationsFollowedBy(userId));
        assertThat(result, is(toSet(apps)));
    }

    @DontRepeat
    @Test
    public void testGetApplicationsFollowedByWhenFails() throws Exception
    {
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenThrow(new RuntimeException());
            
        assertThrows(() -> instance.getApplicationsFollowedBy(userId))
            .isInstanceOf(TException.class);
        
    }
    
    @Test
    public void testGetApplicationsFollowedByWithBadArgs() throws Exception
    {
        String badId = one(alphabeticStrings());
        
        assertThrows(() -> instance.getApplicationsFollowedBy(""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getApplicationsFollowedBy(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetApplicationFollowers() throws Exception
    {
        Map<String, Row> rows = Maps.create();

        for (User follower : followers)
        {
            Row mockRow = mock(Row.class);

            when(userMapper.apply(mockRow))
                .thenReturn(follower);

            rows.put(follower.userId, mockRow);
        }

        when(results.iterator())
            .thenReturn(rows.values().iterator());

        Set<User> result = toSet(instance.getApplicationFollowers(appId));
        assertThat(result, is(toSet(followers)));

    }

    @DontRepeat
    @Test
    public void testGetApplicationFollowersWhenFails() throws Exception
    {
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenThrow(new IllegalArgumentException());
        
        assertThrows(() -> instance.getApplicationFollowers(appId))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testGetApplicationFollowersWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getApplicationFollowers(""))
            .isInstanceOf(InvalidArgumentException.class);

        String badId = one(alphabeticStrings());
        assertThrows(() -> instance.getApplicationFollowers(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

}
