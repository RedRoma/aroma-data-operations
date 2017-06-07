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

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.thrift.TException;
import org.junit.*;
import org.junit.runner.RunWith;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.exceptions.InvalidTokenException;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraTokenRepositoryIT
{

    private static Session session;

    @BeforeClass
    public static void begin()
    {
        session = TestCassandraProviders.getTestSession();
    }

    private final Function<Row, AuthenticationToken> organizationMapper = Mappers.tokenMapper();

    private CassandraTokenRepository instance;

    @GeneratePojo
    private AuthenticationToken token;

    @GenerateString(UUID)
    private String orgId;

    @GenerateString(UUID)
    private String ownerId;

    @GenerateString(UUID)
    private String tokenId;

    @Before
    public void setUp()
    {
        instance = new CassandraTokenRepository(session, organizationMapper);
        token.tokenId = tokenId;
        token.ownerId = ownerId;
        token.organizationId = orgId;
        //Org name isn't stored
        token.unsetOrganizationName();
    }

    @Test
    public void testContainsToken() throws Exception
    {
        boolean result = instance.containsToken(tokenId);
        
        assertThat(result, is(false));
        
        instance.saveToken(token);
        
        result = instance.containsToken(tokenId);
        
        assertThat(result, is(true));
    }

    @Test
    public void testGetToken() throws Exception
    {
        instance.saveToken(token);
        
        AuthenticationToken result = instance.getToken(tokenId);
        assertThat(result, is(token));
    }
    
    @Test
    public void testGetTokenWithoutOrgId() throws Exception
    {
        token.unsetOrganizationId();
        token.unsetOrganizationName();
        
        instance.saveToken(token);
        
        AuthenticationToken result = instance.getToken(tokenId);
        assertThat(result, is(token));
    }
 
    @DontRepeat
    @Test
    public void testGetTokenWhenNotExists() throws Exception
    {
        assertThrows(() -> instance.getToken(tokenId))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testSaveToken() throws Exception
    {
        instance.saveToken(token);
        
        assertThat(instance.containsToken(tokenId), is(true));
    }

    @DontRepeat
    @Test
    public void testGetTokensBelongingTo() throws Exception
    {
        Set<AuthenticationToken> expected = createTokens();

        try
        {
            saveTokens(expected);
        }
        finally
        {
            deleteTokens(expected);
        }
    }

    @Test
    public void testDeleteToken() throws Exception
    {
        assertThrows(() -> instance.deleteToken(tokenId))
            .isInstanceOf(InvalidTokenException.class);
        
        instance.saveToken(token);
        assertThat(instance.containsToken(tokenId), is(true));
        
        instance.deleteToken(tokenId);
        assertThat(instance.containsToken(tokenId), is(false));
    }

    private void saveTokens(Collection<AuthenticationToken> tokens) throws TException
    {
        
        for(AuthenticationToken token : tokens)
        {
            instance.saveToken(token);
        }
    }
    
    private void deleteTokens(Collection<AuthenticationToken> tokens) throws TException
    {
        List<String> tokenIds = tokens
            .stream()
            .map(AuthenticationToken::getTokenId)
            .collect(toList());
        
        instance.deleteTokens(tokenIds);
    }
 
    @DontRepeat
    @Test
    public void testDeleteTokensBelongingTo() throws Exception
    {
        Set<AuthenticationToken> tokens = createTokens();
        saveTokens(tokens);
        
        instance.deleteTokensBelongingTo(ownerId);
        
        List<AuthenticationToken> result = instance.getTokensBelongingTo(ownerId);
        assertThat(result, is(empty()));
    }

    private Set<AuthenticationToken> createTokens()
    {
        AlchemyGenerator<AuthenticationToken> generator = pojos(AuthenticationToken.class);

        return listOf(generator)
            .stream()
            .map(t -> t.setTokenId(one(uuids)))
            .map(t -> t.setOwnerId(ownerId))
            .map(t -> t.setOrganizationId(orgId))
            .map(t -> t.setOrganizationName(null))
            .collect(toSet());
    }

}
