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

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
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
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.positiveLongs;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraTokenRepositoryTest
{

    @Mock
    private Session cassandra;

    @Mock(answer = RETURNS_MOCKS)
    private Cluster cluster;

    private QueryBuilder queryBuilder;

    @Mock
    private Function<Row, AuthenticationToken> tokenMapper;

    private CassandraTokenRepository instance;

    @GeneratePojo
    private AuthenticationToken token;

    @GenerateString(UUID)
    private String ownerId;

    @GenerateString(UUID)
    private String orgId;

    @GenerateString(UUID)
    private String tokenId;

    @GenerateString(ALPHABETIC)
    private String badId;

    @Mock
    private ResultSet results;

    @Mock
    private Row row;

    @Captor
    private ArgumentCaptor<Statement> captor;

    @Before
    public void setUp()
    {
        queryBuilder = new QueryBuilder(cluster);

        instance = new CassandraTokenRepository(cassandra, queryBuilder, tokenMapper);

        token.tokenId = tokenId;
        token.ownerId = ownerId;
        token.organizationId = orgId;

        when(cassandra.execute(Mockito.any(Statement.class))).thenReturn(results);
        when(results.one()).thenReturn(row);
        when(tokenMapper.apply(row)).thenReturn(token);
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new CassandraTokenRepository(null, queryBuilder, tokenMapper));
        assertThrows(() -> new CassandraTokenRepository(cassandra, null, tokenMapper));
        assertThrows(() -> new CassandraTokenRepository(cassandra, queryBuilder, null));
    }

    @Test
    public void testContainsToken() throws Exception
    {
        when(row.getLong(0)).thenReturn(0L);

        assertThat(instance.containsToken(tokenId), is(false));

        long count = one(positiveLongs());
        when(row.getLong(0)).thenReturn(count);

        assertThat(instance.containsToken(tokenId), is(true));
    }

    @Test
    public void testGetToken() throws Exception
    {
        AuthenticationToken result = instance.getToken(tokenId);
        assertThat(result, is(token));
    }

    @DontRepeat
    @Test
    public void testGetTokenWhenFails() throws Exception
    {
        setupForFailure();

        assertThrows(() -> instance.getToken(tokenId))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testGetTokenWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getToken(""))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getToken(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testSaveToken() throws Exception
    {
        instance.saveToken(token);

        verify(cassandra).execute(captor.capture());

        Statement statement = captor.getValue();

        assertThat(statement, notNullValue());
        assertThat(statement, instanceOf(BatchStatement.class));
    }

    @DontRepeat
    @Test
    public void testSaveTokenWhenFails() throws Exception
    {
        setupForFailure();

        assertThrows(() -> instance.saveToken(token))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testSaveTokenWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveToken(null))
            .isInstanceOf(InvalidArgumentException.class);

        AuthenticationToken missingTokenId = new AuthenticationToken(token);
        missingTokenId.unsetTokenId();

        assertThrows(() -> instance.saveToken(missingTokenId))
            .isInstanceOf(InvalidArgumentException.class);

        AuthenticationToken missingOwnerId = new AuthenticationToken(token);
        missingOwnerId.unsetOwnerId();

        assertThrows(() -> instance.saveToken(missingOwnerId))
            .isInstanceOf(InvalidArgumentException.class);

        AuthenticationToken missingTokenType = new AuthenticationToken(token);
        missingTokenType.unsetTokenType();

        assertThrows(() -> instance.saveToken(missingTokenType))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetTokensBelongingTo() throws Exception
    {
        AlchemyGenerator<AuthenticationToken> generator = pojos(AuthenticationToken.class);
        Set<AuthenticationToken> expected = listOf(generator)
            .stream()
            .map(t -> t.setOwnerId(ownerId))
            .map(t -> t.setTokenId(one(uuids)))
            .map(t -> t.setOrganizationId(one(uuids)))
            .collect(toSet());
        
        Map<AuthenticationToken, Row> rows = Maps.create();
        
        for(AuthenticationToken t : expected)
        {
            Row mockRow = mock(Row.class);
            when(tokenMapper.apply(mockRow)).thenReturn(t);
            rows.put(t, mockRow);
        }
        
        when(results.iterator()).thenReturn(rows.values().iterator());
        
        Set<AuthenticationToken> result = Sets.toSet(instance.getTokensBelongingTo(ownerId));

        assertThat(result, is(expected));
    }

    @DontRepeat
    @Test
    public void testGetTokensBelongingToWhenFails() throws Exception
    {
        setupForFailure();
        
        assertThrows(() -> instance.getTokensBelongingTo(ownerId))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testGetTokensBelongingToWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getTokensBelongingTo(""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getTokensBelongingTo(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteToken() throws Exception
    {
    }

    private void setupForFailure()
    {

        when(cassandra.execute(any(Statement.class)))
            .thenThrow(new IllegalArgumentException());
    }

}
