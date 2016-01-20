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
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.utils.UUIDs;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.banana.thrift.LengthOfTime;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.TimeUnit;
import tech.aroma.banana.thrift.exceptions.MessageDoesNotExistException;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static sir.wellington.alchemy.collections.sets.Sets.containTheSameElements;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
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
    
    private static final LengthOfTime MESSAGE_LIFETIME = new LengthOfTime(TimeUnit.MINUTES, 2);

    private static Cluster cluster;
    private static Session session;
    private static QueryBuilder queryBuilder;

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
    
    @GeneratePojo
    private Message message;
    
    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(UUID)
    private String msgId;
    
    @GenerateList(Message.class)
    private List<Message> messages;
    
    private final Function<Row, Message> messageMapper = Mappers.messageMapper();

    private CassandraMessageRepository instance;


    @Before
    public void setUp()
    {
        instance = new CassandraMessageRepository(session, queryBuilder, messageMapper);
        
        AlchemyGenerator<String> timeUids = () -> UUIDs.timeBased().toString();
        
        messages = messages.stream()
            .map(m -> m.setApplicationId(appId))
            .map(m -> m.setMessageId(one(timeUids)))
            .map(m -> { m.unsetIsTruncated(); return m;})
            .collect(toList());
        
        msgId = UUIDs.timeBased().toString();
        message.messageId = msgId;
        message.applicationId = appId;
        message.unsetIsTruncated();
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
        
        try
        {
            deleteMessages(messages);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to delete messages: " + ex);

        }
    }

    private void saveMessages(List<Message> messages) throws TException
    {
        for (Message msg : messages)
        {
            instance.saveMessage(msg, MESSAGE_LIFETIME);
        }
    }

    private void deleteMessages(List<Message> messaages) throws TException
    {
        for (Message msg : messages)
        {
            instance.deleteMessage(msg.applicationId, msg.messageId);
        }
    }

    @Test
    public void testSaveMessage() throws Exception
    {
        instance.saveMessage(message, MESSAGE_LIFETIME);
        
        assertThat(instance.containsMessage(appId, msgId), is(true));
    }

    @Test
    public void testGetMessage() throws Exception
    {
        instance.saveMessage(message, MESSAGE_LIFETIME);
        
        Message result = instance.getMessage(appId, msgId);
        
        assertMessagesMostlyMatch(result, message);
    }

    @Test
    public void testDeleteMessage() throws Exception
    {
        instance.saveMessage(message, MESSAGE_LIFETIME);
        
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
        boolean result = instance.containsMessage(appId, msgId);
        assertThat(result, is(false));
        
        instance.saveMessage(message, MESSAGE_LIFETIME);
        result = instance.containsMessage(appId, msgId);
        assertThat(result, is(true));
    }

    @Test
    public void testGetByHostname() throws Exception
    {
        instance.saveMessage(message, MESSAGE_LIFETIME);
        
        List<Message> result = instance.getByHostname(message.hostname);
        assertThat(result, notNullValue());
        assertThat(result, not(empty()));
        assertThat(result, contains(message));
    }

    @Test
    public void testGetByApplication() throws Exception
    {
        saveMessages(messages);
        
        List<Message> result = instance.getByApplication(appId);
        assertThat(result, notNullValue());
        assertThat(result, not(empty()));
        assertThat(result.size(), is(messages.size()));
        
        Set<Message> expected = Sets.copyOf(messages);
        Set<Message> actual = Sets.copyOf(result);
        assertThat(actual, is(expected));
        
        deleteMessages(messages);
    }

    @Test
    public void testGetByTitle() throws Exception
    {
        String title = one(alphabeticString());
        
        List<Message> expected = messages.stream()
            .map(m -> m.setTitle(title))
            .collect(toList());
        
        saveMessages(expected);
        
        List<Message> result = instance.getByTitle(appId, title);
        assertThat(result, notNullValue());
        assertThat(result, not(empty()));
        assertThat(containTheSameElements(result, expected), is(true));
    }

    @Ignore
    @Test
    public void testGetCountByApplication() throws Exception
    {
        saveMessages(messages);
        
        long count = instance.getCountByApplication(appId);
        assertThat(count, greaterThanOrEqualTo((long) messages.size()));
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