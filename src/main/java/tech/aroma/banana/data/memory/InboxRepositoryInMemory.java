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

 
package tech.aroma.banana.data.memory;


import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.banana.data.InboxRepository;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;

import static tech.aroma.banana.data.assertions.DataAssertions.validMessage;
import static tech.aroma.banana.data.assertions.DataAssertions.validUser;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
final class InboxRepositoryInMemory implements InboxRepository
{
    private final static Logger LOG = LoggerFactory.getLogger(InboxRepositoryInMemory.class);
    
    private final Map<String, List<Message>> messagesForUser = Maps.createSynchronized();
    

    @Override
    public void saveMessageForUser(Message message, User user) throws TException
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
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
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
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        messagesForUser.remove(userId);
    }

    @Override
    public int countInboxForUser(String userId) throws TException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());

        return messagesForUser.getOrDefault(userId, Lists.emptyList()).size();
    }


}
