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
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.MessageDoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;



/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class MessageRepositoryInMemoryTest 
{

    @GeneratePojo
    private Message message;
    private String messageId;
       
    @GenerateString
    private String title;
    
    @GenerateString
    private String hostname;
       
    @GenerateString
    private String applicationId;
    
    private MessageRepositoryInMemory instance;
    
    @GenerateList(Message.class)
    private List<Message> messages;
    
    @Before
    public void setUp()
    {
        messageId = message.messageId;
        
        instance = new MessageRepositoryInMemory();
    }
    
    private void saveMessages(List<Message> messages) throws TException
    {
        for(Message message : messages)
        {
            instance.saveMessage(message);
        }
    }

    @Test
    public void testSaveMessage() throws Exception
    {
        instance.saveMessage(message);
        
        assertThat(instance.containsMessage(messageId), is(true));
        
        assertThrows(() -> instance.saveMessage(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveMessage(new Message()))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetMessage() throws Exception
    {
        instance.saveMessage(message);
        
        Message result = instance.getMessage(messageId);
        assertThat(result, is(message));
    }
    
    @DontRepeat
    @Test
    public void testGetMessageWhenIdDoesNotExist() throws Exception
    {
        assertThrows(() -> instance.getMessage(messageId))
            .isInstanceOf(MessageDoesNotExistException.class);
    }

    @Test
    public void testDeleteMessage() throws Exception
    {
        //Should be ok if message does not exist
        instance.deleteMessage(messageId);
        
        instance.saveMessage(message);
        assertThat(instance.containsMessage(messageId), is(true));
        
        instance.deleteMessage(messageId);
        assertThat(instance.containsMessage(messageId), is(false));
    }

    @Test
    public void testContainsMessage() throws Exception
    {
        assertThat(instance.containsMessage(messageId), is(false));
        
        instance.saveMessage(message);
        
        assertThat(instance.containsMessage(messageId), is(true));
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
        
        List<Message> result = instance.getByApplication(applicationId);
        assertThat(result, is(messages));
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
        
        List<Message> result = instance.getByTitle(applicationId, title);
        assertThat(result, is(messages));
    }

    @DontRepeat
    @Test
    public void testGetByTitleWhenEmpty() throws Exception
    {
        List<Message> result = instance.getByTitle(applicationId, title);
        assertThat(result, is(empty()));
    }
    
    @Test
    public void testGetCountByApplication() throws Exception
    {
    }

}