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
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class CassandraReactionRepositoryIT
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
    private String ownerId;
    
    private Function<Row, List<Reaction>> reactionsMapper = Mappers.reactionsMapper();
    
    private CassandraReactionRepository instance;
    
    private List<Reaction> reactions;
    
    @Before
    public void setUp() throws Exception
    {
        instance = new CassandraReactionRepository(session, queryBuilder, reactionsMapper);
        
        setupData();
        setupMocks();
    }
    
    @After
    public void cleanUp() throws TException
    {
        try
        {
            instance.saveReactionsForApplication(ownerId, null);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to delete reactions for: " + ownerId + " | " + ex.getMessage());
        }
    }
    
    private void setupData() throws Exception
    {
        reactions = listOf(reactions(), 15);
    }
    
    private void setupMocks() throws Exception
    {
        
    }
    
    @Test
    public void testSaveReactionsForUser() throws Exception
    {
        instance.saveReactionsForUser(ownerId, reactions);
        
        List<Reaction> results = instance.getReactionsForUser(ownerId);
        assertThat(results, is(reactions));
    }
    
    @Test
    public void testSaveReactionsForUserWhenDeleting() throws Exception
    {
        instance.saveReactionsForUser(ownerId, reactions);
        instance.saveReactionsForUser(ownerId, null);
        
        List<Reaction> results = instance.getReactionsForUser(ownerId);
        assertThat(results, empty());
    }
    
    @Test
    public void testGetReactionsForUser() throws Exception
    {
        instance.saveReactionsForUser(ownerId, reactions);
        
        List<Reaction> results = instance.getReactionsForUser(ownerId);
        assertThat(results, is(reactions));
    }
    
    @Test
    public void testGetReactionsForUserWhenEmpty() throws Exception
    {
        List<Reaction> results = instance.getReactionsForUser(ownerId);
        assertThat(results, notNullValue());
        assertThat(results, empty());
    }
    
    @Test
    public void testSaveReactionsForApplication() throws Exception
    {
        instance.saveReactionsForApplication(ownerId, reactions);
        
        List<Reaction> results = instance.getReactionsForApplication(ownerId);
        assertThat(results, is(reactions));
    }
    
    @Test
    public void testSaveReactionsForApplicationWheEmpty() throws Exception
    {
        instance.saveReactionsForApplication(ownerId, reactions);
        instance.saveReactionsForApplication(ownerId, null);
        
        List<Reaction> results = instance.getReactionsForApplication(ownerId);
        assertThat(results, notNullValue());
        assertThat(results, empty());
        
    }
    
    @Test
    public void testGetReactionsForApplication() throws Exception
    {
        instance.saveReactionsForApplication(ownerId, reactions);
        
        List<Reaction> results = instance.getReactionsForApplication(ownerId);
        assertThat(results, is(reactions));
    }
    
    @Test
    public void testGetReactionsForApplicationWhenEmpty() throws Exception
    {
        List<Reaction> results = instance.getReactionsForApplication(ownerId);
        assertThat(results, notNullValue());
        assertThat(results, empty());
    }
    
}
