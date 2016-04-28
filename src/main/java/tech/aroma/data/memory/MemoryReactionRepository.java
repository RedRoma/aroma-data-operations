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

package tech.aroma.data.memory;

import java.util.List;
import java.util.Map;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.data.ReactionRepository;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

/**
 *
 * @author SirWellington
 */
@Internal
final class MemoryReactionRepository implements ReactionRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(MemoryReactionRepository.class);
    
    private final Map<String, List<Reaction>> database = Maps.createSynchronized();
    
    @Override
    public void saveReactionsForUser(String userId, List<Reaction> reactions) throws TException
    {
        checkUserId(userId);
        
        if (Lists.isEmpty(reactions))
        {
            database.remove(userId);
            return;
        }
        
        database.put(userId, reactions);
    }
    
    @Override
    public List<Reaction> getReactionsForUser(String userId) throws TException
    {
        checkUserId(userId);
        
        return database.getOrDefault(userId, Lists.emptyList());
    }
    
    @Override
    public void saveReactionsForApplication(String appId, List<Reaction> reactions) throws TException
    {
        checkAppId(appId);
        
        if (Lists.isEmpty(reactions))
        {
            database.remove(appId);
            return;
        }
        
        database.put(appId, reactions);
    }
    
    @Override
    public List<Reaction> getReactionsForApplication(String appId) throws TException
    {
        checkAppId(appId);
        
        return database.getOrDefault(appId, Lists.emptyList());
    }
    
    private void checkUserId(String userId) throws InvalidArgumentException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(validUserId());
    }
    
    private void checkAppId(String appId) throws InvalidArgumentException
    {
        checkThat(appId)
            .throwing(InvalidArgumentException.class)
            .is(validApplicationId());
    }
    
}
