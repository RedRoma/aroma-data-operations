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

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class CassandraCredentialsRepositoryIT
{

    private static Session session;
    private static QueryBuilder queryBuilder;

    @BeforeClass
    public static void begin()
    {
        queryBuilder = TestCassandraProviders.getQueryBuilder();
        session = TestCassandraProviders.getTestSession();
    }

    @GenerateString(UUID)
    private String userId;
    
    @GenerateString(HEXADECIMAL)
    private String password;


    private CassandraCredentialsRepository instance;


    @Before
    public void setUp() throws Exception
    {
        instance = new CassandraCredentialsRepository(session, queryBuilder);
        
        setupData();
        setupMocks();
    }

    @After
    public void cleanUp() throws TException
    {
        try
        {
            instance.deleteEncryptedPassword(userId);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to delete credentials: " + ex.getMessage());
        }
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {
        
    }

    @Test
    public void testSaveEncryptedPassword() throws Exception
    {
        instance.saveEncryptedPassword(userId, password);
        
        assertThat(instance.containsEncryptedPassword(userId), is(true));
    }

    @Test
    public void testContainsEncryptedPassword() throws Exception
    {
        assertThat(instance.containsEncryptedPassword(userId), is(false));
        
        instance.saveEncryptedPassword(userId, password);
        
        assertThat(instance.containsEncryptedPassword(userId), is(true));
    }

    @Test
    public void testGetEncryptedPassword() throws Exception
    {
        assertThrows(() -> instance.getEncryptedPassword(userId))
            .isInstanceOf(DoesNotExistException.class);

        instance.saveEncryptedPassword(userId, password);
        
        String result = instance.getEncryptedPassword(userId);
        assertThat(result, is(password));
    }

    @Test
    public void testDeleteEncryptedPassword() throws Exception
    {
        instance.deleteEncryptedPassword(userId);
        
        instance.saveEncryptedPassword(userId, password);
        
        instance.deleteEncryptedPassword(userId);
        assertThat(instance.containsEncryptedPassword(userId), is(false));
    }

}
