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
import java.util.Set;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.MessageDoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static sir.wellington.alchemy.collections.sets.Sets.toSet;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MemoryMessageRepositoryTest
{

    @GeneratePojo
    private Message message;

    @GenerateString(UUID)
    private String messageId;

    @GenerateString
    private String title;

    @GenerateString
    private String hostname;

    @GenerateString(UUID)
    private String applicationId;

    private MemoryMessageRepository instance;

    @GenerateList(Message.class)
    private List<Message> messages;

    @Before
    public void setUp()
    {
        message.messageId = messageId;
        message.applicationId = applicationId;

        instance = new MemoryMessageRepository();

        messages = messages.stream()
            .map(m -> m.setMessageId(one(uuids)))
            .collect(toList());
    }

    private void saveMessages(List<Message> messages) throws TException
    {
        for (Message message : messages)
        {
            instance.saveMessage(message);
        }
    }

    @Test
    public void testSaveMessage() throws Exception
    {
        instance.saveMessage(message);

        assertThat(instance.containsMessage(applicationId, messageId), is(true));

        assertThrows(() -> instance.saveMessage(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.saveMessage(new Message()))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetMessage() throws Exception
    {
        instance.saveMessage(message);

        Message result = instance.getMessage(applicationId, messageId);
        assertThat(result, is(message));
    }

    @DontRepeat
    @Test
    public void testGetMessageWhenIdDoesNotExist() throws Exception
    {
        assertThrows(() -> instance.getMessage(applicationId, messageId))
            .isInstanceOf(MessageDoesNotExistException.class);
    }

    @DontRepeat
    @Test
    public void testGetMessageWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getMessage("", messageId))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getMessage(applicationId, ""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteMessage() throws Exception
    {
        //Should be ok if message does not exist
        instance.deleteMessage(applicationId, messageId);

        instance.saveMessage(message);
        assertThat(instance.containsMessage(applicationId, messageId), is(true));

        instance.deleteMessage(applicationId, messageId);
        assertThat(instance.containsMessage(applicationId, messageId), is(false));
    }

    @Test
    public void testContainsMessage() throws Exception
    {
        assertThat(instance.containsMessage(applicationId, messageId), is(false));

        instance.saveMessage(message);

        assertThat(instance.containsMessage(applicationId, messageId), is(true));
    }

    @Test
    public void testGetByHostname() throws Exception
    {
        messages.forEach(msg -> msg.setHostname(hostname));
        saveMessages(messages);

        List<Message> result = instance.getByHostname(hostname);
        assertThat(result, is(messages));
    }

    @DontRepeat
    @Test
    public void testGetByHostnameWhenEmpty() throws Exception
    {
        List<Message> result = instance.getByHostname(hostname);
        assertThat(result, is(empty()));
    }

    @DontRepeat
    @Test
    public void testGetByHostnameWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getByHostname(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getByHostname(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetByApplication() throws Exception
    {
        messages.forEach(msg -> msg.setApplicationId(applicationId));
        saveMessages(messages);

        Set<Message> result = Sets.toSet(instance.getByApplication(applicationId));
        assertThat(result, is(toSet(messages)));
    }

    @DontRepeat
    @Test
    public void testGetByApplicationWhenEmpty() throws Exception
    {
        List<Message> result = instance.getByApplication(applicationId);
        assertThat(result, is(empty()));
    }

    @DontRepeat
    @Test
    public void testGetByApplicationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getByApplication(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getByApplication(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetByTitle() throws Exception
    {
        messages.forEach(msg -> msg.setTitle(title));
        messages.forEach(msg -> msg.setApplicationId(applicationId));
        saveMessages(messages);

        Set<Message> result = Sets.toSet(instance.getByTitle(applicationId, title));
        assertThat(result, is(toSet(messages)));
    }

    @Test
    public void testGetByTitleWhenApplicationIdDoesNotMatch() throws Exception
    {
        messages.forEach(msg -> msg.setTitle(title));
        saveMessages(messages);

        List<Message> result = instance.getByTitle(applicationId, title);
        assertThat(result, is(empty()));
    }

    @Test
    public void testGetByTitleWhenTitleDoesNotMatch() throws Exception
    {
        messages.forEach(msg -> msg.setApplicationId(applicationId));
        saveMessages(messages);

        List<Message> result = instance.getByTitle(applicationId, title);
        assertThat(result, is(empty()));
    }

    @DontRepeat
    @Test
    public void testGetByTitleWhenEmpty() throws Exception
    {
        List<Message> result = instance.getByTitle(applicationId, title);
        assertThat(result, is(empty()));
    }

    @DontRepeat
    @Test
    public void testGetByTitleWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getByTitle(applicationId, ""))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getByTitle("", title))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getByTitle(null, null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetCountByApplication() throws Exception
    {
        messages.forEach(msg -> msg.setApplicationId(applicationId));
        saveMessages(messages);
        long result = instance.getCountByApplication(applicationId);
        long expected = messages.size();
        assertThat(result, is(expected));
    }

    @DontRepeat
    @Test
    public void testGetCountByApplicationWhenEmpty() throws Exception
    {
        long result = instance.getCountByApplication(applicationId);
        assertThat(result, is(0L));
    }

}
