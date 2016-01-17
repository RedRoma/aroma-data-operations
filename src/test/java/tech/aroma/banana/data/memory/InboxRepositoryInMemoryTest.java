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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.User;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;


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
    
    @GenerateList(Message.class)
    private List<Message> messages;
    
    private InboxRepositoryInMemory instance;

    @Before
    public void setUp()
    {
        user = new User()
            .setUserId(userId)
            .setName(nameOfUser);
        
        instance = new InboxRepositoryInMemory();
    }

    @Test
    public void testSaveMessageForUser() throws Exception
    {
        instance.saveMessageForUser(message, user);
        
        List<Message> messages = instance.getMessagesForUser(userId);
        assertThat(messages, contains(message));
    }

    @Test
    public void testGetMessagesForUser() throws Exception
    {
    }

    @Test
    public void testDeleteMessageForUser() throws Exception
    {
    }

    @Test
    public void testDeleteAllMessagesForUser() throws Exception
    {
    }

    @Test
    public void testCountInboxForUser() throws Exception
    {
    }

}