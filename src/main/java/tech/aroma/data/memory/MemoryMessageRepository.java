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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.MessageRepository;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.MessageDoesNotExistException;
import tech.aroma.thrift.functions.TimeFunctions;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validMessage;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.equalTo;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.keyInMap;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@Internal
final class MemoryMessageRepository implements MessageRepository, ExpirationListener<String, Message>
{
    
    private final static Logger LOG = LoggerFactory.getLogger(MemoryMessageRepository.class);
    
    private final ExpiringMap<String, Message> messages = ExpiringMap.builder()
        .variableExpiration()
        .expirationListener(this)
        .build();
    
    private final ExpiringMap<String, Set<String>> messagesByApplication = ExpiringMap.builder()
        .variableExpiration()
        .build();
    
    @Override
    public void saveMessage(Message message, LengthOfTime lifetime) throws TException
    {
        checkThat(message)
            .throwing(InvalidArgumentException.class)
            .is(validMessage());
        
        checkThat(lifetime)
            .usingMessage("lifetime cannot be missing")
            .throwing(InvalidArgumentException.class)
            .is(notNull());
        
        long seconds = TimeFunctions.toSeconds(lifetime);
        
        String msgId = message.messageId;
        messages.put(msgId, message, seconds, TimeUnit.SECONDS);
        
        String appId = message.applicationId;
        
        Set<String> existing = messagesByApplication.getOrDefault(appId, Sets.create());
        existing.add(msgId);
        messagesByApplication.put(appId, existing);
    }
    
    @Override
    public Message getMessage(String applicationId, String messageId) throws TException
    {
        checkThat(applicationId, messageId)
            .throwing(InvalidArgumentException.class)
            .are(nonEmptyString());
        
        checkThat(messageId)
            .throwing(MessageDoesNotExistException.class)
            .is(keyInMap(messages));
        
        Message message = messages.get(messageId);
        
        checkThat(message.applicationId)
            .throwing(MessageDoesNotExistException.class)
            .usingMessage("Message does not correspond to the given App ID")
            .is(equalTo(applicationId));
        
        return message;
    }
    
    @Override
    public void deleteMessage(String applicationId, String messageId) throws TException
    {
        checkThat(applicationId, messageId)
            .throwing(InvalidArgumentException.class)
            .are(nonEmptyString());
        
        if (!messages.containsKey(messageId))
        {
            return;
        }
        
        Message message = messages.remove(messageId);
        
        String appId = message.applicationId;
        Set<String> appMessages = messagesByApplication.getOrDefault(appId, Sets.create());
        appMessages.remove(messageId);
        messagesByApplication.put(appId, appMessages);
    }
    
    @Override
    public boolean containsMessage(String applicationId, String messageId) throws TException
    {
        checkThat(applicationId, messageId)
            .throwing(InvalidArgumentException.class)
            .are(nonEmptyString());
        
        return messages.containsKey(messageId);
    }
    
    @Override
    public List<Message> getByHostname(String hostname) throws TException
    {
        checkThat(hostname)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        return messages.values()
            .stream()
            .filter(m -> Objects.equals(m.hostname, hostname))
            .collect(toList());
    }
    
    @Override
    public List<Message> getByApplication(String applicationId) throws TException
    {
        checkThat(applicationId)
            .throwing(InvalidArgumentException.class)
            .is(validApplicationId());
        
        return messagesByApplication.getOrDefault(applicationId, Sets.emptySet())
            .stream()
            .map(id -> messages.get(id))
            .filter(Objects::nonNull)
            .collect(toList());
    }
    
    @Override
    public List<Message> getByTitle(String applicationId, String title) throws TException
    {
        checkThat(applicationId)
            .throwing(InvalidArgumentException.class)
            .is(validApplicationId());
        
        checkThat(title)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        return messagesByApplication.getOrDefault(applicationId, Sets.emptySet())
            .stream()
            .map(id -> messages.get(id))
            .filter(Objects::nonNull)
            .filter(m -> Objects.equals(m.title, title))
            .collect(toList());
    }
    
    @Override
    public long getCountByApplication(String applicationId) throws TException
    {
        return messagesByApplication.getOrDefault(applicationId, Sets.emptySet())
            .size();
    }
    
    @Override
    public void expired(String key, Message value)
    {
        if(isNullOrEmpty(key))
        {
            return;
        }
        
        if(Objects.isNull(value))
        {
            return;
        }
        
        String appId = value.applicationId;
        
        Set<String> appMessages = messagesByApplication.getOrDefault(appId, Sets.create());
        appMessages.remove(key);
        messagesByApplication.put(appId, appMessages);
    }
    
}
