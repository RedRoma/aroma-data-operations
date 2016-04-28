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

package tech.aroma.data.cassandra;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.utils.UUIDs;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.TimeUnit;
import tech.aroma.thrift.exceptions.MessageDoesNotExistException;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.annotations.testing.TimeSensitive;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static sir.wellington.alchemy.collections.sets.Sets.containTheSameElements;
import static tech.aroma.thrift.generators.MessageGenerators.messages;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraMessageRepositoryIT 
{
    
    private static final LengthOfTime MESSAGE_LIFETIME = new LengthOfTime(TimeUnit.MINUTES, 2);

    private static Session session;
    private static QueryBuilder queryBuilder;

    @BeforeClass
    public static void begin()
    {
        session = TestCassandraProviders.getTestSession();
        queryBuilder = TestCassandraProviders.getQueryBuilder();
    }

    
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
    public void setUp() throws Exception
    {
        instance = new CassandraMessageRepository(session, queryBuilder, messageMapper);


        setupData();
    }

    private void setupData() throws Exception
    {
        msgId = UUIDs.timeBased().toString();

        message = one(messages());
        message.messageId = msgId;
        message.applicationId = appId;
        message.unsetIsTruncated();

        AlchemyGenerator<String> timeUids = () -> UUIDs.timeBased().toString();

        messages = messages.stream()
            .map(m -> m.setApplicationId(appId))
            .map(m -> m.setMessageId(one(timeUids)))
            .map(m ->
            {
                m.unsetIsTruncated();
                return m;
            })
            .collect(toList());


    }
    
    @After
    public void cleanUp() throws TException
    {
        try
        {
            instance.deleteAllMessages(appId);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to delete all messages for App: " + appId + "|" + ex.getMessage());
        }
    }

    private void saveMessages(List<Message> messages) throws TException
    {
        for (Message msg : messages)
        {
            instance.saveMessage(msg, MESSAGE_LIFETIME);
        }
    }

    private void deleteMessages(List<Message> messages) throws TException
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

    @TimeSensitive
    @DontRepeat
    @Test
    public void testGetCountByApplication() throws Exception
    {
        saveMessages(messages);
        
        Thread.sleep(5);
        long count = instance.getCountByApplication(appId);
        assertThat(count, greaterThanOrEqualTo((long) messages.size()));
    }

    private void assertMessagesMostlyMatch(Message result, Message expected)
    {
        assertThat(result, notNullValue());
        assertThat(result.applicationId, is(expected.applicationId));
        assertThat(result.applicationName, is(expected.applicationName));
        assertThat(result.body, is(expected.body));
        assertThat(result.deviceName, is(expected.deviceName));
        assertThat(result.hostname, is(expected.hostname));
        assertThat(result.macAddress, is(expected.macAddress));
        assertThat(result.messageId, is(expected.messageId));
        assertThat(result.title, is(expected.title));
        assertThat(result.timeMessageReceived, is(expected.timeMessageReceived));
        assertThat(result.timeOfCreation, is(expected.timeOfCreation));
        assertThat(result.urgency, is(expected.urgency));
    }

    @Test
    public void testDeleteAllMessages() throws Exception
    {
        saveMessages(messages);
        
        long count = instance.getCountByApplication(appId);
        assertThat(count, greaterThan(0L));
        
        instance.deleteAllMessages(appId);
        
        Thread.sleep(10);
        count = instance.getCountByApplication(appId);
        assertThat(count, is(0L));
        
        List<Message> messages = instance.getByApplication(appId);
        assertThat(messages, is(empty()));
    }

}
