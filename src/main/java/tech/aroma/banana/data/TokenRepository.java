/*
 * Copyright 2015 Aroma Tech.
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

package tech.aroma.banana.data;

import java.util.List;
import java.util.Objects;
import org.apache.thrift.TException;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.InvalidTokenException;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.aroma.banana.data.assertions.RequestAssertions.tokenContainingOwnerId;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

/**
 * This interface is responsible for the storage and retrieval of Tokens.
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
public interface TokenRepository
{

    boolean containsToken(@NonEmpty String tokenId) throws TException;

    AuthenticationToken getToken(@NonEmpty String tokenId) throws TException, InvalidTokenException;

    void saveToken(@Required AuthenticationToken token) throws TException;

    List<AuthenticationToken> getTokensBelongingTo(@NonEmpty String ownerId) throws TException;

    void deleteToken(@NonEmpty String tokenId) throws TException;

    default boolean doesTokenBelongTo(@NonEmpty String tokenId, @NonEmpty String ownerId) throws InvalidTokenException, TException
    {
        checkThat(tokenId, ownerId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("tokenId and ownerId are required")
            .are(nonEmptyString());

        AuthenticationToken token = this.getToken(tokenId);

        checkThat(token)
            .throwing(InvalidTokenException.class)
            .is(tokenContainingOwnerId());

        return Objects.equals(ownerId, token.ownerId);
    }

    default void deleteTokens(@Required List<String> tokenIds) throws TException
    {
        checkThat(tokenIds)
            .throwing(InvalidArgumentException.class)
            .is(notNull());
        
        List<TException> exceptions = Lists.create();
        
        tokenIds
            .parallelStream()
            .forEach(tokenId ->
            {
                try
                {
                    deleteToken(tokenId);
                }
                catch (TException ex)
                {
                    exceptions.add(ex);
                }
            });
        
        if (!Lists.isEmpty(exceptions))
        {
            throw Lists.oneOf(exceptions);
        }
    }
    
    default void deleteTokensBelongingTo(@Required String ownerId) throws TException
    {
        checkThat(ownerId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        List<TException> exceptions = Lists.create();
        
        this.getTokensBelongingTo(ownerId)
            .parallelStream()
            .map(AuthenticationToken::getTokenId)
            .forEach(id ->
            {
                try
                {
                    this.deleteToken(id);
                }
                catch (TException ex)
                {
                    exceptions.add(ex);
                }
            });
        
        if(!Lists.isEmpty(exceptions))
        {
            throw Lists.oneOf(exceptions);
        }
        
    }

}
