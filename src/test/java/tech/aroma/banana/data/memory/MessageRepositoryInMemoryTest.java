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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.MessageDoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

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
    
    @Before
    public void setUp()
    {
        messageId = message.messageId;
        
        instance = new MessageRepositoryInMemory();
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
    }

    @Test
    public void testGetByApplication() throws Exception
    {
    }

    @Test
    public void testGetByTitle() throws Exception
    {
    }

    @Test
    public void testGetCountByApplication() throws Exception
    {
    }

}