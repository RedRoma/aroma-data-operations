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

package tech.aroma.banana.data.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.utils.UUIDs;
import java.util.List;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.exceptions.MessageDoesNotExistException;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraMessageRepositoryIT 
{

    @GeneratePojo
    private Message message;
    
    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(UUID)
    private String msgId;
    
    @GenerateList(Message.class)
    private List<Message> messages;
    
    private static Cluster cluster;
    private static Session session;
    private static QueryBuilder queryBuilder;
    
    private CassandraMessageRepository instance;
    
    @BeforeClass
    public static void begin()
    {
        cluster = TestSessions.createTestCluster();
        session = TestSessions.createTestSession(cluster);
        queryBuilder = TestSessions.createQueryBuilder(cluster);
    }
    
    @AfterClass
    public static void end()
    {
        session.close();
        cluster.close();
    }

    @Before
    public void setUp()
    {
        instance = new CassandraMessageRepository(session, queryBuilder);
        
        AlchemyGenerator<String> timeUids = () -> UUIDs.timeBased().toString();
        
        messages = messages.stream()
            .map(m -> m.setApplicationId(appId))
            .map(m -> m.setMessageId(one(timeUids)))
            .collect(toList());
        
        msgId = UUIDs.timeBased().toString();
        message.messageId = msgId;
        message.applicationId = appId;
    }
    
    @After
    public void cleanUp() throws TException
    {
        try
        {
            instance.deleteMessage(appId, msgId);
        }
        catch(Exception ex)
        {
            System.out.println("Failed to delete message: " + ex);
        }
    }

    @Test
    public void testSaveMessage() throws Exception
    {
        instance.saveMessage(message);
        
        assertThat(instance.containsMessage(appId, msgId), is(true));
    }

    @Test
    public void testGetMessage() throws Exception
    {
        instance.saveMessage(message);
        
        Message result = instance.getMessage(appId, msgId);
        
        assertMessagesMostlyMatch(result, message);
    }

    @Test
    public void testDeleteMessage() throws Exception
    {
        instance.saveMessage(message);
        
        instance.deleteMessage(appId, msgId);
        
        assertThat(instance.containsMessage(appId, msgId), is(false));
    }
    
    @Test
    public void testDeleteWhenNotPresent() throws Exception
    {
        assertThrows(() -> instance.deleteMessage(appId, msgId))
            .isInstanceOf(MessageDoesNotExistException.class);
    }

    @Test
    public void testContainsMessage() throws Exception
    {
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

    private void assertMessagesMostlyMatch(Message result, Message message)
    {
        assertThat(result, notNullValue());
        assertThat(result.applicationId, is(message.applicationId));
        assertThat(result.applicationName, is(message.applicationName));
        assertThat(result.body, is(message.body));
        assertThat(result.hostname, is(message.hostname));
        assertThat(result.macAddress, is(message.macAddress));
        assertThat(result.messageId, is(message.messageId));
        assertThat(result.title, is(message.title));
        assertThat(result.timeMessageReceived, is(message.timeMessageReceived));
        assertThat(result.timeOfCreation, is(message.timeOfCreation));
        assertThat(result.urgency, is(message.urgency));
    }

}