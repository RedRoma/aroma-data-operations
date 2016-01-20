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
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.thrift.LengthOfTime;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.TimeUnit;
import tech.aroma.banana.thrift.User;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
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

    @GeneratePojo
    private User user;
    
    @GenerateString(UUID)
    private String userId;

    @GenerateList(Message.class)
    private List<Message> messages;

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
    }

    private void saveMessages(List<Message> messages) throws TException
    {
        for (Message msg : messages)
        {
            instance.saveMessageForUser(msg, user);
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
        instance.saveMessageForUser(message, user);
        assertThat(instance.containsMessageInInbox(userId, message), is(true));
    }

    @Test
    public void testGetMessagesForUser() throws Exception
    {
    }

    @Test
    public void testContainsMessageInInbox() throws Exception
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