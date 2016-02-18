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

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.utils.UUIDs;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.TimeUnit;
import tech.aroma.thrift.User;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraInboxRepositoryIT
{

    private static final LengthOfTime MESSAGE_LIFETIME = new LengthOfTime(TimeUnit.MINUTES, 5);

    private static Session session;
    private static QueryBuilder queryBuilder;

    @BeforeClass
    public static void begin()
    {
        session = TestCassandraProviders.getTestSession();
        queryBuilder = TestCassandraProviders.getQueryBuilder();
    }

    
    @GeneratePojo
    private Message message;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String msgId;

    @GeneratePojo
    private User user;
    
    @GenerateString(UUID)
    private String userId;

    @GenerateList(Message.class)
    private List<Message> messages;
    
    private Map<String, Message> messageMapping;

    private final Function<Row, Message> messageMapper = Mappers.messageMapper();

    private CassandraInboxRepository instance;

    @Before
    public void setUp()
    {
        instance = new CassandraInboxRepository(session, queryBuilder, messageMapper);

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

        msgId = UUIDs.timeBased().toString();
        message.messageId = msgId;
        message.applicationId = appId;
        message.unsetIsTruncated();
        
        user.userId = userId;
        
        messageMapping = Maps.create();
        messages.forEach(m -> messageMapping.put(m.messageId, m));
    }

    @After
    public void cleanUp() throws TException
    {
        try
        {
            instance.deleteMessageForUser(userId, msgId);
        }
        catch (Exception ex)
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
        
        messageMapping.clear();
    }

    private void saveMessagesInInbox(List<Message> messages) throws TException
    {
        for (Message msg : messages)
        {
            instance.saveMessageForUser(user, msg, MESSAGE_LIFETIME);
        }
    }

    private void deleteMessages(List<Message> messages) throws TException
    {
        for (Message msg : messages)
        {
            instance.deleteMessageForUser(userId, msg.messageId);
        }
    }

    @Test
    public void testSaveMessageForUser() throws Exception
    {
        instance.saveMessageForUser(user, message, MESSAGE_LIFETIME);
        assertThat(instance.containsMessageInInbox(userId, message), is(true));
    }

    @Test
    public void testGetMessagesForUser() throws Exception
    {
        saveMessagesInInbox(messages);
        
        List<Message> result = instance.getMessagesForUser(userId);
        assertThat(result.size(), is(messages.size()));
        
        for (Message m : result)
        {
            assertThat(messageMapping.containsKey(m.messageId), is(true));
            Message expected = messageMapping.get(m.messageId);
            assertMostlySame(m, expected);
        }
    }

    @Test
    public void testContainsMessageInInbox() throws Exception
    {
        boolean result = instance.containsMessageInInbox(userId, message);
        assertThat(result, is(false));
        
        instance.saveMessageForUser(user, message);
        
        result = instance.containsMessageInInbox(userId, message);
        assertThat(result, is(true));
    }

    @Test
    public void testDeleteMessageForUser() throws Exception
    {
        instance.saveMessageForUser(user, message);

        instance.deleteMessageForUser(userId, msgId);
        
        assertThat(instance.containsMessageInInbox(userId, message), is(false));
    }

    @Test
    public void testDeleteAllMessagesForUser() throws Exception
    {
        saveMessagesInInbox(messages);
        
        instance.deleteAllMessagesForUser(userId);
        
        assertThat(instance.countInboxForUser(userId), is(0L));
    }

    @Test
    public void testCountInboxForUser() throws Exception
    {
        long count = instance.countInboxForUser(userId);
        assertThat(count, is(0L));
        
        saveMessagesInInbox(messages);
        count = instance.countInboxForUser(userId);
        assertThat(count, greaterThanOrEqualTo((long) messages.size()));
    }

    private void assertMostlySame(Message result, Message expected)
    {
        assertThat(result, notNullValue());
        assertThat(result.applicationId, is(expected.applicationId));
        assertThat(result.applicationName, is(expected.applicationName));
        assertThat(result.hostname, is(expected.hostname));
        assertThat(result.macAddress, is(expected.macAddress));
        assertThat(result.messageId, is(expected.messageId));
        assertThat(result.title, is(expected.title));
        assertThat(result.timeMessageReceived, is(expected.timeMessageReceived));
        assertThat(result.timeOfCreation, is(expected.timeOfCreation));
        assertThat(result.urgency, is(expected.urgency));
        assertThat(result.body, is(expected.body));
    }

}
