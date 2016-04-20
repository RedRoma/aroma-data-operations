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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
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

import static org.mockito.Answers.RETURNS_MOCKS;
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
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraReactionRepositoryTest
{

    @Mock(answer = RETURNS_MOCKS)
    private Cluster cluster;

    @Mock
    private Session cassandra;

    private QueryBuilder queryBuilder;

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
    private ResultSet results;

    @Mock
    private Row row;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();

        instance = new CassandraReactionRepository(cassandra, queryBuilder, reactionsMapper);
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
        queryBuilder = new QueryBuilder(cluster);

        when(results.one()).thenReturn(row);
        when(reactionsMapper.apply(row)).thenReturn(reactions);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new CassandraReactionRepository(null, queryBuilder, reactionsMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraReactionRepository(cassandra, null, reactionsMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraReactionRepository(cassandra, queryBuilder, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSaveReactionsForUser() throws Exception
    {
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
    }

    @Test
    public void testGetReactionsForApplicationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getReactionsForApplication(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

}
