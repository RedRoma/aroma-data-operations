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

package tech.aroma.data.memory;

import com.google.common.collect.Sets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.InvalidTokenException;
import tech.aroma.thrift.generators.TokenGenerators;
import tech.sirwellington.alchemy.annotations.testing.TimeSensitive;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.time.Instant.now;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.generator.TimeGenerators.futureInstants;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MemoryTokenRepositoryTest
{

    private MemoryTokenRepository repository;

    private AuthenticationToken token;

    @GenerateList(AuthenticationToken.class)
    private List<AuthenticationToken> tokens;

    private String tokenId;
    private String ownerId;

    @Before
    public void setUp()
    {
        repository = new MemoryTokenRepository();

        token =  TokenGenerators.authenticationTokens().get();
        tokenId = token.getTokenId();
        ownerId = token.getOwnerId();

        tokens.forEach(t -> t.setOwnerId(ownerId));

        Instant timeOfExpiration = one(futureInstants()).plus(1, ChronoUnit.HOURS);
        token.setTimeOfExpiration(timeOfExpiration.toEpochMilli());
        tokens.forEach(t -> t.setTimeOfExpiration(timeOfExpiration.toEpochMilli()));
    }

    @Test
    public void testDoesTokenExist() throws Exception
    {
        assertThat(repository.containsToken(tokenId), is(false));

        repository.saveToken(token);
        assertThat(repository.containsToken(tokenId), is(true));
    }

    @DontRepeat
    @Test
    public void testDoesTokenExistWithBadArgs() throws Exception
    {
        assertThrows(() -> repository.containsToken(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

//    @Repeat(10)
    @DontRepeat
    @TimeSensitive
    @Test
    public void testDoesTokenExistWhenTokenExpired() throws Exception
    {
        token.setTimeOfExpiration(now().plusSeconds(1).toEpochMilli());
        repository.saveToken(token);

        Thread.sleep(1000);
        assertThat(repository.containsToken(tokenId), is(false));
    }

    @Test
    public void testGetToken() throws Exception
    {
        assertThrows(() -> repository.getToken(tokenId))
            .isInstanceOf(InvalidTokenException.class);

        repository.saveToken(token);

        AuthenticationToken result = repository.getToken(tokenId);
        assertThat(result, is(token));
    }

    @DontRepeat
    @Test
    public void testGetTokenWithBadArgs() throws Exception
    {
        assertThrows(() -> repository.getToken(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @DontRepeat
    @TimeSensitive
    @Test
    public void testGetTokenWhenTokenExpired() throws Exception
    {
        token.setTimeOfExpiration(now().plusSeconds(1).toEpochMilli());
        repository.saveToken(token);

        Thread.sleep(1500);
        assertThrows(() -> repository.getToken(tokenId))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testSaveToken() throws Exception
    {
        repository.saveToken(token);

        AuthenticationToken result = repository.getToken(tokenId);
        assertThat(result, is(token));
    }

    @DontRepeat
    @Test
    public void testSaveTokenWithBadArgs() throws Exception
    {
        assertThrows(() -> repository.saveToken(null))
            .isInstanceOf(InvalidTokenException.class);

        //Missing Owner ID
        token.setOwnerId("");

        assertThrows(() -> repository.saveToken(token))
            .isInstanceOf(InvalidTokenException.class);

        //Missing Token ID
        token.setOwnerId(ownerId);
        token.setTokenId("");

        assertThrows(() -> repository.saveToken(token))
            .isInstanceOf(InvalidTokenException.class);

        //Missing Token Type
        token.setTokenId(tokenId);
        token.setTokenType(null);

        assertThrows(() -> repository.saveToken(token))
            .isInstanceOf(InvalidTokenException.class);

    }

    @Test
    public void testGetTokensBelongingTo() throws Exception
    {
        for (AuthenticationToken token : tokens)
        {
            repository.saveToken(token);
        }

        List<AuthenticationToken> result = repository.getTokensBelongingTo(ownerId);
        Set<AuthenticationToken> expected = Sets.newHashSet(tokens);
        Set<AuthenticationToken> resultSet = Sets.newHashSet(result);
        assertThat(resultSet, is(expected));
    }

    @Test
    public void testGetTokensWhenNoneExistForOwner() throws Exception
    {
        String ownerId = one(hexadecimalString(10));
        List<AuthenticationToken> result = repository.getTokensBelongingTo(ownerId);
        assertThat(result, notNullValue());
        assertThat(result.isEmpty(), is(true));
    }

    @DontRepeat
    @Test
    public void testGetTokensBelongingToWithBadArgs() throws Exception
    {
        assertThrows(() -> repository.getTokensBelongingTo(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteToken() throws Exception
    {
        repository.saveToken(token);
        assertThat(repository.containsToken(tokenId), is(true));

        repository.deleteToken(tokenId);
        assertThat(repository.containsToken(tokenId), is(false));
    }

    @DontRepeat
    @Test
    public void testDeleteTokenWithBadArgs() throws Exception
    {
        assertThrows(() -> repository.deleteToken(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteTokenWhenTokenDoesNotExist() throws Exception
    {
        repository.deleteToken(tokenId);
    }

    @Test
    public void testDoesTokenBelongTo() throws Exception
    {
        repository.saveToken(token);

        boolean result = repository.doesTokenBelongTo(tokenId, ownerId);
        assertThat(result, is(true));
    }

    @Test
    public void testDoesTokenBelongToWhenTokenDoesNotExist() throws Exception
    {
        assertThrows(() -> repository.doesTokenBelongTo(tokenId, ownerId))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testDoesTokenBelongToWhenNoMatch() throws Exception
    {
        repository.saveToken(token);

        String otherOwnerId = one(hexadecimalString(20));
        boolean result = repository.doesTokenBelongTo(tokenId, otherOwnerId);
        assertThat(result, is(false));
    }

    @DontRepeat
    @Test
    public void testDoesTokenBelongToWithBadArgs() throws Exception
    {
        assertThrows(() -> repository.doesTokenBelongTo("", ownerId))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> repository.doesTokenBelongTo(tokenId, ""))
            .isInstanceOf(InvalidArgumentException.class);

    }

    @Test
    public void testDeleteTokens() throws Exception
    {
        for (AuthenticationToken token : tokens)
        {
            repository.saveToken(token);
        }

        List<String> tokenIds = tokens.stream()
            .map(AuthenticationToken::getTokenId)
            .collect(Collectors.toList());

        for (String tokenId : tokenIds)
        {
            assertThat(repository.containsToken(tokenId), is(true));
        }

        repository.deleteTokens(tokenIds);

        for (String tokenId : tokenIds)
        {
            assertThat(repository.containsToken(tokenId), is(false));
        }

        List<AuthenticationToken> tokensBelongingTo = repository.getTokensBelongingTo(ownerId);
        assertThat(tokensBelongingTo, is(empty()));
    }

    @DontRepeat
    @Test
    public void testDeleteTokensWithBadArgs() throws Exception
    {
        assertThrows(() -> repository.deleteTokens(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

}
