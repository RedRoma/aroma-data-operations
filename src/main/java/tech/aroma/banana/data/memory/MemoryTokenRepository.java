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

package tech.aroma.banana.data.memory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.data.TokenRepository;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;
import tech.sirwellington.alchemy.arguments.assertions.TimeAssertions;

import static java.util.stream.Collectors.toList;
import static tech.aroma.banana.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.banana.thrift.assertions.BananaAssertions.legalToken;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.keyInMap;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = CONCRETE_BEHAVIOR)
final class MemoryTokenRepository implements TokenRepository, ExpirationListener<String, AuthenticationToken>
{

    private final static Logger LOG = LoggerFactory.getLogger(MemoryTokenRepository.class);

    private final ExpiringMap<String, AuthenticationToken> tokens = ExpiringMap.builder()
        .variableExpiration()
        .expirationListener(this)
        .build();

    private final Map<String, List<AuthenticationToken>> tokensByOwner = ExpiringMap.builder()
        .variableExpiration()
        .build();

    @Override
    public boolean containsToken(String tokenId) throws TException
    {
        checkThat(tokenId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("missing tokenId")
            .is(nonEmptyString());

        return tokens.containsKey(tokenId);
    }

    @Override
    public AuthenticationToken getToken(String tokenId) throws TException, InvalidTokenException
    {
        checkThat(tokenId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("missing tokenId")
            .is(nonEmptyString())
            .throwing(InvalidTokenException.class)
            .usingMessage("token does not exists")
            .is(keyInMap(tokens));

        return tokens.get(tokenId);
    }

    @Override
    public void saveToken(AuthenticationToken token) throws TException
    {
        checkThat(token)
            .usingMessage("attempting to save null token")
            .throwing(InvalidArgumentException.class)
            .is(legalToken());

        checkThat(token.ownerId)
            .usingMessage("token missing ownerId")
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        checkThat(token.tokenType)
            .usingMessage("tokenType is required")
            .throwing(InvalidArgumentException.class)
            .is(notNull());
        
        Instant expirationTime = Instant.ofEpochMilli(token.timeOfExpiration);

        checkThat(expirationTime)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Token expiration time must be in the future: " + expirationTime)
            .is(TimeAssertions.inTheFuture());

        Instant now = Instant.now();

        long timeToLiveSeconds = now.until(expirationTime, ChronoUnit.SECONDS);

        this.tokens.put(token.tokenId, token, timeToLiveSeconds, TimeUnit.SECONDS);

        addTokenForOwner(token);
    }

    @Override
    public List<AuthenticationToken> getTokensBelongingTo(String ownerId) throws TException
    {
        checkThat(ownerId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("ownerId missing")
            .is(nonEmptyString());

        return tokensByOwner.getOrDefault(ownerId, Lists.emptyList());
    }

    @Override
    public void deleteToken(String tokenId) throws TException
    {
        checkThat(tokenId)
            .usingMessage("missing tokenId")
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());

        AuthenticationToken token = tokens.remove(tokenId);

        if (token == null)
        {
            return;
        }

        deleteTokenFromOwner(token);
    }

    @Override
    public void expired(String key, AuthenticationToken value)
    {
        String ownerId = value.ownerId;

        if (isNullOrEmpty(ownerId))
        {
            return;
        }

        this.deleteTokenFromOwner(value);

        List<AuthenticationToken> ownerTokens = this.tokensByOwner.getOrDefault(value.ownerId, Lists.emptyList());
        
        if (Lists.isEmpty(ownerTokens))
        {
            tokensByOwner.remove(ownerId);
        }
    }

    private void addTokenForOwner(AuthenticationToken token)
    {
        List<AuthenticationToken> ownerTokens = this.tokensByOwner.getOrDefault(token.ownerId, Lists.create());
        ownerTokens.add(token);
        this.tokensByOwner.put(token.ownerId, ownerTokens);
    }

    private void deleteTokenFromOwner(AuthenticationToken token)
    {
        String ownerId = token.ownerId;
        List<AuthenticationToken> ownerTokens = tokensByOwner.getOrDefault(ownerId, Lists.emptyList());

        ownerTokens = ownerTokens.stream()
            .filter(t -> !Objects.equals(t.tokenId, token.tokenId))
            .collect(toList());

        this.tokensByOwner.put(ownerId, ownerTokens);
    }

}
