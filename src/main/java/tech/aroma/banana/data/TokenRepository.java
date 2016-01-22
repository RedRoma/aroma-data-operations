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
import org.apache.thrift.TException;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.aroma.banana.data.assertions.RequestAssertions.tokenContainingOwnerId;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

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
            .usingMessage("tokenId and ownerId are required")
            .are(nonEmptyString());

        AuthenticationToken token = this.getToken(tokenId);

        checkThat(token)
            .is(tokenContainingOwnerId());

        return ownerId.equals(token.ownerId);
    }

    default void deleteTokens(@Required List<String> tokenIds) throws TException
    {
        checkThat(tokenIds).is(notNull());

        for (String token : tokenIds)
        {
            deleteToken(token);
        }
    }

}