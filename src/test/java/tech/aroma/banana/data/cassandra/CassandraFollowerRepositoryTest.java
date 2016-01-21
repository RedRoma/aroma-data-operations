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

package tech.aroma.banana.data.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sir.wellington.alchemy.collections.sets.Sets.toSet;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.positiveLongs;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraFollowerRepositoryTest
{

    @Mock(answer = RETURNS_MOCKS)
    private Cluster cluster;

    @Mock
    private Session cassandra;
    
    @Captor
    private ArgumentCaptor<Statement> statementCaptor;
    
    private QueryBuilder queryBuilder;
    
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

    @GeneratePojo
    private Application application;

    @GenerateString(UUID)
    private String userId;

    @GenerateString(UUID)
    private String appId;
    
    @GenerateList(Application.class)
    private List<Application> apps;
    
    @GenerateList(User.class)
    private List<User> followers;
    
    private CassandraFollowerRepository instance;
    
    @Before
    public void setUp()
    {
        queryBuilder = new QueryBuilder(cluster);
        
        createStubs();
        
        instance = new CassandraFollowerRepository(cassandra, queryBuilder, userMapper, applicationMapper);
        
        application.applicationId = appId;
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
        assertThrows(() -> new CassandraFollowerRepository(null, queryBuilder, userMapper, applicationMapper))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new CassandraFollowerRepository(cassandra, null, userMapper, applicationMapper))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new CassandraFollowerRepository(cassandra, queryBuilder, null, applicationMapper))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new CassandraFollowerRepository(cassandra, queryBuilder, userMapper, null))
            .isInstanceOf(IllegalArgumentException.class);
        
    }

    @Test
    public void testSaveFollowing() throws Exception
    {
        instance.saveFollowing(user, application);
        
        verify(cassandra).execute(statementCaptor.capture());
        Statement statementMade = statementCaptor.getValue();
        assertThat(statementMade, notNullValue());
    }

    @Test
    public void testSaveFollowingWhenFails() throws Exception
    {
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenThrow(new IllegalArgumentException());

        assertThrows(() -> instance.saveFollowing(user, application))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testSaveFollowingWithBadArgs() throws Exception
    {

        User emptyUser = new User();
        Application emptyApp = new Application();

        assertThrows(() -> instance.saveFollowing(emptyUser, application))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.saveFollowing(user, emptyApp))
            .isInstanceOf(InvalidArgumentException.class);
        
        User userWithBadId = user.setUserId(one(alphabeticString()));
        Application appWithBadId = application.setApplicationId(one(alphabeticString()));

        assertThrows(() -> instance.saveFollowing(userWithBadId, application))
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
        
        String badId = one(alphabeticString());
        
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
        String badId = one(alphabeticString());

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
        
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenReturn(results);
        
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
        String badId = one(alphabeticString());
        
        assertThrows(() -> instance.getApplicationsFollowedBy(""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getApplicationsFollowedBy(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetApplicationFollowers() throws Exception
    {
    }

}
