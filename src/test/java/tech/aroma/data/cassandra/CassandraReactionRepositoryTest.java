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

package tech.aroma.data.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import java.util.List;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;
import tech.sirwellington.alchemy.thrift.ThriftObjects;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class CassandraReactionRepositoryTest
{

    @Mock
    private Session cassandra;

    @Mock
    private Function<Row, List<Reaction>> reactionsMapper;

    private CassandraReactionRepository instance;

    @GenerateString(UUID)
    private String ownerId;

    @GenerateString(ALPHABETIC)
    private String badId;

    private List<Reaction> reactions;

    private List<String> serializedReactions;

    @Captor
    private ArgumentCaptor<Statement> captor;

    @Mock
    private ResultSet resultSet;

    @Mock
    private Row row;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();

        instance = new CassandraReactionRepository(cassandra, reactionsMapper);
    }

    private void setupData() throws Exception
    {
        reactions = listOf(reactions(), 10);

        serializedReactions = Lists.create();

        for (Reaction reaction : reactions)
        {
            serializedReactions.add(ThriftObjects.toJson(reaction));
        }
    }

    private void setupMocks() throws Exception
    {
        when(resultSet.one()).thenReturn(row);
        when(reactionsMapper.apply(row)).thenReturn(reactions);
        when(cassandra.execute(any(Statement.class))).thenReturn(resultSet);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new CassandraReactionRepository(null, reactionsMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraReactionRepository(cassandra, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSaveReactionsForUser() throws Exception
    {
        instance.saveReactionsForUser(ownerId, reactions);
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, instanceOf(Insert.class));
    }

    @Test
    public void testSaveReactionsForUserWhenEmpty() throws Exception
    {
        instance.saveReactionsForUser(ownerId, Lists.emptyList());
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, instanceOf(Delete.Where.class));
    }

    @Test
    public void testSaveReactionsForUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveReactionsForUser(badId, reactions))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetReactionsForUser() throws Exception
    {
        List<Reaction> results = instance.getReactionsForUser(ownerId);
        assertThat(results, is(reactions));
    }

    @Test
    public void testGetReactionsForUserWhenEmpty() throws Exception
    {
        when(resultSet.one()).thenReturn(null);
        
        List<Reaction> results = instance.getReactionsForUser(ownerId);
        assertThat(results, notNullValue());
        assertThat(results, is(empty()));
    }

    @Test
    public void testGetReactionsForUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getReactionsForUser(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testSaveReactionsForApplication() throws Exception
    {
        instance.saveReactionsForApplication(ownerId, reactions);
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Insert.class)));
    }

    @Test
    public void testSaveReactionsForApplicationWhenEmpty() throws Exception
    {
        instance.saveReactionsForApplication(ownerId, null);
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, is(instanceOf(Delete.Where.class)));
    }

    @Test
    public void testSaveReactionsForApplicationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveReactionsForApplication(badId, reactions))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetReactionsForApplication() throws Exception
    {
        List<Reaction> results = instance.getReactionsForApplication(ownerId);
        
        assertThat(results, is(reactions));
    }

    @Test
    public void testGetReactionsForApplicationWheEmpty() throws Exception
    {
        when(resultSet.one()).thenReturn(null);
        
        List<Reaction> results = instance.getReactionsForApplication(ownerId);
        assertThat(results, notNullValue());
        assertThat(results, empty());
    }

    @Test
    public void testGetReactionsForApplicationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getReactionsForApplication(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

}
