/*
 * Copyright 2017 RedRoma, Inc.
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

import java.util.List;
import java.util.function.Function;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.*;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.positiveLongs;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class CassandraInboxRepositoryTest
{

    @Mock
    private Session cassandra;

    @Mock
    private Function<Row, Message> messageMapper;

    private CassandraInboxRepository instance;

    @GeneratePojo
    private Message message;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String messageId;

    @GeneratePojo
    private User user;

    @GenerateString(UUID)
    private String userId;

    @GenerateString(ALPHABETIC)
    private String badId;

    @Mock
    private ResultSet results;

    @Mock
    private Row row;

    @Captor
    private ArgumentCaptor<Statement> captor;

    private LengthOfTime lifetime = new LengthOfTime(TimeUnit.DAYS, 1);

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new CassandraInboxRepository(cassandra, messageMapper);
    }

    private void setupData() throws Exception
    {
        message.messageId = messageId;
        message.applicationId = appId;
        user.userId = userId;

    }

    private void setupMocks() throws Exception
    {
        when(cassandra.execute(any(Statement.class))).thenReturn(results);
        when(results.one()).thenReturn(row);
        
        List<Row> rows = Lists.createFrom(row);
        when(results.iterator()).thenReturn(rows.iterator());
        
        when(messageMapper.apply(row)).thenReturn(message);

    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new CassandraInboxRepository(null, messageMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraInboxRepository(cassandra, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSaveMessageForUser() throws Exception
    {
        instance.saveMessageForUser(user, message, lifetime);

        verify(cassandra).execute(captor.capture());

        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Insert.Options.class)));
    }

    @DontRepeat
    @Test
    public void testSaveMessageForUserWithBadArgs() throws Exception
    {
        User userWithBadId = new User(user).setUserId(badId);
        assertThrows(() -> instance.saveMessageForUser(userWithBadId, message, lifetime))
            .isInstanceOf(InvalidArgumentException.class);

        Message messageWithBadId = new Message(message).setMessageId(badId);
        assertThrows(() -> instance.saveMessageForUser(user, messageWithBadId, lifetime))
            .isInstanceOf(InvalidArgumentException.class);

        Message messageWithBadAppId = new Message(message).setApplicationId(badId);
        assertThrows(() -> instance.saveMessageForUser(user, messageWithBadAppId, lifetime))
            .isInstanceOf(InvalidArgumentException.class);

    }

    @Test
    public void testGetMessagesForUser() throws Exception
    {
        List<Message> result = instance.getMessagesForUser(userId);
        assertThat(result, notNullValue());
        assertThat(result, not(empty()));
        assertThat(result, contains(message));

        verify(cassandra).execute(captor.capture());

        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Select.class)));

    }
    
    @DontRepeat
    @Test
    public void testGetMessagesForUserWhenEmpty() throws Exception
    {
        List<Row> empty = Lists.emptyList();
        when(results.iterator()).thenReturn(empty.iterator());
        List<Message> result = instance.getMessagesForUser(userId);
        
        assertThat(result, is(empty()));
    }
    
    @DontRepeat
    @Test
    public void testGetMessagesForUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getMessagesForUser(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testContainsMessageInInbox() throws Exception
    {
        long count = one(positiveLongs());
        when(row.getLong(0)).thenReturn(count);
        
        boolean result = instance.containsMessageInInbox(userId, message);
        assertThat(result, is(true));

        count = 0L;
        when(row.getLong(0)).thenReturn(count);
        result = instance.containsMessageInInbox(userId, message);
        assertThat(result, is(false));
        
    }

    @DontRepeat
    @Test
    public void testContainsMessageInInboxWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.containsMessageInInbox(badId, message))
            .isInstanceOf(InvalidArgumentException.class);
        
        Message messageWithBadId = new Message(message).setMessageId(badId);
        assertThrows(() -> instance.containsMessageInInbox(userId, messageWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
        
        Message messageWithBadAppId = new Message(message).setApplicationId(badId);
        assertThrows(() -> instance.containsMessageInInbox(userId, messageWithBadAppId))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testDeleteMessageForUser() throws Exception
    {
        instance.deleteMessageForUser(userId, messageId);
        
        verify(cassandra).execute(captor.capture());
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Delete.Where.class)));
    }
    
    @DontRepeat
    @Test
    public void testDeleteMessageForUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteMessageForUser(badId, messageId))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteMessageForUser(userId, badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteAllMessagesForUser() throws Exception
    {
        instance.deleteAllMessagesForUser(userId);
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Delete.Where.class)));
    }
    
    @DontRepeat
    @Test
    public void testDeleteAllMessagesForUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteAllMessagesForUser(""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteAllMessagesForUser(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testCountInboxForUser() throws Exception
    {
        long count = one(positiveLongs());
        when(row.getLong(0)).thenReturn(count);
        
        long result = instance.countInboxForUser(userId);
        assertThat(result, is(count));
    }
    
    @DontRepeat
    @Test
    public void testCountInboxForUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.countInboxForUser(""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.countInboxForUser(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

}
