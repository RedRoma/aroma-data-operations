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


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.thrift.LengthOfTime;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.MessageDoesNotExistException;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;

import static tech.aroma.banana.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.banana.data.assertions.RequestAssertions.validMessage;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.keyInMap;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
final class MessageRepositoryInMemory implements MessageRepository
{
    private final static Logger LOG = LoggerFactory.getLogger(MessageRepositoryInMemory.class);
    
    private final Map<String, Message> messages = Maps.createSynchronized();
    private final Map<String, List<String>> messagesByHostname = Maps.createSynchronized();
    private final Map<String, List<String>> messagesByApplication = Maps.createSynchronized();
    private final Map<String, List<String>> messagesByTitle = Maps.createSynchronized();
    

    @Override
    public void saveMessage(Message message, LengthOfTime lifetime) throws TException
    {
        checkThat(message)
            .throwing(InvalidArgumentException.class)
            .is(validMessage());
        
        checkThat(lifetime)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Invalid lifetime")
            .is(validMessageLifetime());
        
        String messageId = message.messageId;
        messages.put(messageId, message);
        
        if(!isNullOrEmpty(message.hostname))
        {
            String hostname = message.hostname;
            List<String> list = messagesByHostname.getOrDefault(hostname, Lists.create());
            list.add(messageId);
            messagesByHostname.put(hostname, list);
        }
        
        if(!isNullOrEmpty(message.title))
        {
            String title = message.title;
            List<String> list = messagesByTitle.getOrDefault(title, Lists.create());
            list.add(messageId);
            messagesByTitle.put(title, list);
        }
        
        if(!isNullOrEmpty(message.applicationId))
        {
            String appId = message.applicationId;
            List<String> list = messagesByApplication.getOrDefault(appId, Lists.create());
            list.add(messageId);
            messagesByApplication.put(appId, list);
        }
        
    }

    @Override
    public Message getMessage(String applicationId, String messageId) throws TException
    {
        checkThat(applicationId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        checkMessageId(messageId);

        return messages.get(messageId);
    }

    @Override
    public void deleteMessage(String messageId) throws TException
    {
        checkThat(messageId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        Message deletedMessage = messages.remove(messageId);
        
        if(deletedMessage == null)
        {
            return;
        }
        
        Predicate<String> doesNotMatchMessage = id -> !Objects.equals(id, messageId);
        
        String applicationId = deletedMessage.applicationId;
        String hostname = deletedMessage.hostname;
        String title = deletedMessage.title;
        
        if (!isNullOrEmpty(applicationId))
        {
            List<String> list = messagesByApplication.getOrDefault(applicationId, Lists.emptyList());
            list = list.stream()
                .filter(doesNotMatchMessage)
                .collect(Collectors.toList());
            messagesByApplication.put(applicationId, list);
        }
        
        if (!isNullOrEmpty(hostname))
        {
            List<String> list = messagesByHostname.getOrDefault(hostname, Lists.emptyList());
            list = list.stream()
                .filter(doesNotMatchMessage)
                .collect(Collectors.toList());
            messagesByHostname.put(hostname, list);
        }
        
        if (!isNullOrEmpty(title))
        {
            List<String> list = messagesByTitle.getOrDefault(title, Lists.emptyList());
            list = list.stream()
                .filter(doesNotMatchMessage)
                .collect(Collectors.toList());
            messagesByTitle.put(title, list);
        }
    }

    @Override
    public boolean containsMessage(String messageId) throws TException
    {
        checkThat(messageId)
            .usingMessage("messageId cannot be empty")
            .is(nonEmptyString())
            .is(nonEmptyString());
        
        return messages.containsKey(messageId);
    }

    @Override
    public List<Message> getByHostname(String hostname) throws TException
    {
        checkThat(hostname)
            .usingMessage("hostname cannot be empty")
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        return messagesByHostname.getOrDefault(hostname, Lists.emptyList())
            .stream()
            .map(id -> messages.get(id))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public List<Message> getByApplication(String applicationId) throws TException
    {
        checkThat(applicationId)
            .usingMessage("applicationId cannot be empty")
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        return messagesByApplication.getOrDefault(applicationId, Lists.emptyList())
            .stream()
            .map(id -> messages.get(id))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public List<Message> getByTitle(String applicationId, String title) throws TException
    {
        checkThat(applicationId, title)
            .throwing(InvalidArgumentException.class)
            .are(nonEmptyString());
        
        Predicate<Message> matchesApplicationId = msg -> Objects.equals(msg.applicationId, applicationId);
        
        return messagesByTitle.getOrDefault(title, Lists.emptyList())
            .stream()
            .map(id -> messages.get(id))
            .filter(Objects::nonNull)
            .filter(matchesApplicationId)
            .collect(Collectors.toList());
    }

    @Override
    public long getCountByApplication(String applicationId) throws TException
    {
        checkThat(applicationId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        return messagesByApplication.getOrDefault(applicationId, Lists.emptyList()).size();
    }

    private void checkMessageId(String messageId) throws TException
    {
        checkThat(messageId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("missing messageId")
            .is(nonEmptyString())
            .throwing(MessageDoesNotExistException.class)
            .is(keyInMap(messages));
    }

    private AlchemyAssertion<LengthOfTime> validMessageLifetime()
    {
        return time ->
        {
            checkThat(time).is(notNull());
            checkThat(time.value)
                .usingMessage("time value must be > 0")
                .is(greaterThan(0L));
            
            checkThat(time.unit)
                .usingMessage("missing time unit")
                .is(notNull());
        };
    }

}
