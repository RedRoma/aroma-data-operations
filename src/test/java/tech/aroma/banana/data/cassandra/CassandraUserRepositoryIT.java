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
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.List;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.thrift.User;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraUserRepositoryIT
{

    private static Cluster cluster;
    private static Session session;
    private static QueryBuilder queryBuilder;

    @BeforeClass
    public static void begin()
    {
        cluster = TestSessions.createTestCluster();
        session = TestSessions.createTestSession(cluster);
        queryBuilder = TestSessions.createQueryBuilder(cluster);
    }

    @AfterClass
    public static void end()
    {
        session.close();
        cluster.close();
    }

    @GeneratePojo
    private User user;

    @GenerateString(UUID)
    private String userId;

    @GenerateList(User.class)
    private List<User> users;

    private final Function<Row, User> userMapper = Mappers.userMapper();
    private CassandraUserRepository instance;

    @Before
    public void setUp()
    {
        user.userId = userId;

        instance = new CassandraUserRepository(session, queryBuilder, userMapper);
    }

    @After
    public void cleanUp() throws TException
    {
        try
        {
            instance.deleteUser(userId);
        }
        catch (Exception ex)
        {
            System.out.println("Could not delete User: " + userId);
        }
    }

    private void saveUsers(List<User> users) throws TException
    {
        for (User user : users)
        {
            instance.saveUser(user);
        }
    }

    private void deleteUsers(List<User> users) throws TException
    {
        for (User user : users)
        {
            try
            {
                instance.deleteUser(user.userId);
            }
            catch (Exception ex)
            {
                System.out.println("Could not delete User: " + user.userId);
            }
        }
    }

    @Test
    public void testSaveUser() throws Exception
    {
        instance.saveUser(user);

        assertThat(instance.containsUser(userId), is(true));
    }

    @Test
    public void testGetUser() throws Exception
    {
    }

    @Test
    public void testDeleteUser() throws Exception
    {
    }

    @Test
    public void testContainsUser() throws Exception
    {
    }

    @Test
    public void testGetUserByEmail() throws Exception
    {
    }

    @Test
    public void testFindByGithubProfile() throws Exception
    {
    }

}
