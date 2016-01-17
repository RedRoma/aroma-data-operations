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
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;


/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class InboxRepositoryInMemoryTest 
{
    
    @GenerateString
    private String userId;
    @GenerateString
    private String nameOfUser;
    private User user;
    
    @GeneratePojo
    private Message message;
    private String messageId;
    
    @GenerateList(Message.class)
    private List<Message> messages;
    
    private InboxRepositoryInMemory instance;

    @Before
    public void setUp()
    {
        user = new User()
            .setUserId(userId)
            .setName(nameOfUser);
        messageId = message.messageId;
        
        instance = new InboxRepositoryInMemory();
    }

    private void saveMessages(List<Message> messages) throws TException
    {
        for(Message message : messages)
        {
            instance.saveMessageForUser(message, user);
        }
    }
    
    @Test
    public void testSaveMessageForUser() throws Exception
    {
        instance.saveMessageForUser(message, user);
        
        List<Message> result = instance.getMessagesForUser(userId);
        assertThat(result, contains(message));
    }
    
    @DontRepeat
    public void testSaveMessageForUserWithBadArguments() throws Exception
    {
        assertThrows(() -> instance.saveMessageForUser(message, null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveMessageForUser(null, user))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveMessageForUser(message, new User()))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveMessageForUser(new Message(), user))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetMessagesForUser() throws Exception
    {
        saveMessages(messages);
        List<Message> result = instance.getMessagesForUser(userId);
        assertThat(result, is(messages));
    }
    
    @DontRepeat
    @Test
    public void testGetMessagesForUserWhenEmpty() throws Exception
    {
        List<Message> result = instance.getMessagesForUser(userId);
        assertThat(result, is(empty()));
    }
    
    @DontRepeat
    @Test
    public void testGetMessagesForUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getMessagesForUser(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getMessagesForUser(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteMessageForUser() throws Exception
    {
        instance.saveMessageForUser(message, user);
        
        instance.deleteMessageForUser(userId, messageId);
        
        assertThat(instance.containsMessageInInbox(userId, message), is(false));
    }

    @DontRepeat
    @Test
    public void testDeleteMessageForUserWhenNoneExist() throws Exception
    {
        instance.deleteMessageForUser(userId, messageId);
    }

    @DontRepeat
    @Test
    public void testDeleteMessageForUserWithBadArgs() throws Exception
    {
        
        assertThrows(() -> instance.deleteMessageForUser("", messageId))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteMessageForUser(userId, ""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteMessageForUser(null, messageId))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteMessageForUser(userId, null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteAllMessagesForUser() throws Exception
    {
        saveMessages(messages);
        assertThat(instance.countInboxForUser(userId), is(messages.size()));
        
        instance.deleteAllMessagesForUser(userId);
        assertThat(instance.countInboxForUser(userId), is(0));
    }

    @DontRepeat
    @Test
    public void testDeleteAllMessagesForUserWhenNoneExist() throws Exception
    {
        instance.deleteAllMessagesForUser(userId);
    }

    @DontRepeat
    @Test
    public void testDeleteAllMessagesForUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteAllMessagesForUser(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteAllMessagesForUser(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testCountInboxForUser() throws Exception
    {
        saveMessages(messages);
        
        int result = instance.countInboxForUser(userId);
        assertThat(result, is(messages.size()));
    }

    @DontRepeat
    @Test
    public void testCountInboxForUserWhenEmpty() throws Exception
    {
        int result = instance.countInboxForUser(userId);
        assertThat(result, is(0));
    }

    @DontRepeat
    @Test
    public void testCountInboxForUserWithBadArgs() throws Exception
    {
        
        assertThrows(() -> instance.countInboxForUser(""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.countInboxForUser(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testContainsMessageInInbox() throws Exception
    {
        assertThat(instance.containsMessageInInbox(userId, message), is(false));
        
        instance.saveMessageForUser(message, user);
        
        assertThat(instance.containsMessageInInbox(userId, message), is(true));
        
    }

}