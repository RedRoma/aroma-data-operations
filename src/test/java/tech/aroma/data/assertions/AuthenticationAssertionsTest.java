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

package tech.aroma.data.assertions;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.TokenRepository;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class AuthenticationAssertionsTest
{

    @GeneratePojo
    private AuthenticationToken token;

    private String tokenId;
    
    @Mock
    private TokenRepository repo;
    
    @Before
    public void setUp() throws TException
    {
        tokenId = token.tokenId;
        
        when(repo.containsToken(tokenId))
            .thenReturn(true);
    }

    @Test
    public void testTokenInRepository() throws Exception
    {
        AlchemyAssertion<String> instance = AuthenticationAssertions.tokenInRepository(repo);
        assertThat(instance, notNullValue());

        instance.check(tokenId);

        when(repo.containsToken(tokenId))
            .thenReturn(false);
        
        assertThrows(() -> instance.check(tokenId))
            .isInstanceOf(FailedAssertionException.class);

    }

    @Test
    public void testCompleteToken()
    {
        AlchemyAssertion<AuthenticationToken> instance = AuthenticationAssertions.completeToken();
        assertThat(instance, notNullValue());
        
        instance.check(token);
        
        
        AuthenticationToken tokenWithoutTokenId = new AuthenticationToken(token);
        tokenWithoutTokenId.unsetTokenId();
        
        assertThrows(() -> instance.check(tokenWithoutTokenId))
            .isInstanceOf(FailedAssertionException.class);
        
        AuthenticationToken tokenWithoutOwnerId = new AuthenticationToken(token);
        tokenWithoutOwnerId.unsetOwnerId();
        
        assertThrows(() -> instance.check(tokenWithoutOwnerId))
            .isInstanceOf(FailedAssertionException.class);
        
        AuthenticationToken tokenWithoutTokenType = new AuthenticationToken(token);
        tokenWithoutTokenType.unsetTokenType();
        
        assertThrows(() -> instance.check(tokenWithoutTokenType))
            .isInstanceOf(FailedAssertionException.class);
    }

}
