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


import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Objects;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.data.InboxRepository;
import tech.aroma.thrift.*;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.aroma.data.assertions.RequestAssertions.validMessage;
import static tech.aroma.data.assertions.RequestAssertions.validUser;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 *
 * @author SirWellington
 */
final class MemoryInboxRepository implements InboxRepository
{
    private final static Logger LOG = LoggerFactory.getLogger(MemoryInboxRepository.class);
    
    private final Map<String, List<Message>> messagesForUser = Maps.createSynchronized();

    @Override
    public void saveMessageForUser(@Required User user, @Required Message message, @Required LengthOfTime lifetime) throws TException
    {
        checkThat(message)
            .throwing(InvalidArgumentException.class)
            .is(validMessage());
        
        checkThat(user)
            .throwing(InvalidArgumentException.class)
            .is(validUser());
        
        String userId = user.userId;
        
        List<Message> messages = messagesForUser.getOrDefault(userId, Lists.create());
        messages.add(message);
        messagesForUser.put(userId, messages);
    }

    @Override
    public List<Message> getMessagesForUser(String userId) throws TException
    {
        checkUserId(userId);
        
        return messagesForUser.getOrDefault(userId, Lists.emptyList());
    }

    @Override
    public void deleteMessageForUser(String userId, String messageId) throws TException
    {
        checkThat(userId, messageId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("empty arguments")
            .are(nonEmptyString());
        
        Predicate<Message> notEqualToMessageId = msg -> !Objects.equal(msg.messageId, messageId);
        
        List<Message> messages = messagesForUser.getOrDefault(userId, Lists.emptyList());
        
        
        messages = messages.stream()
            .filter(notEqualToMessageId)
            .collect(Collectors.toList());
        messagesForUser.put(userId, messages);
    }

    @Override
    public void deleteAllMessagesForUser(String userId) throws TException
    {
        checkUserId(userId);
        
        messagesForUser.remove(userId);
    }

    @Override
    public long countInboxForUser(String userId) throws TException
    {
        checkUserId(userId);

        return messagesForUser.getOrDefault(userId, Lists.emptyList()).size();
    }

    @Override
    public boolean containsMessageInInbox(String userId, Message message) throws TException
    {
        checkUserId(userId);
        
        checkThat(message)
            .throwing(InvalidArgumentException.class)
            .is(validMessage());
            
        String messageId = message.messageId;
        
        return this.messagesForUser.getOrDefault(userId, Lists.emptyList())
            .stream()
            .map(Message::getMessageId)
            .anyMatch(id -> Objects.equal(id, messageId));
        
    }

    private void checkUserId(String userId) throws InvalidArgumentException
    {
        checkThat(userId)
            .usingMessage("missing userId")
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
    }


}
