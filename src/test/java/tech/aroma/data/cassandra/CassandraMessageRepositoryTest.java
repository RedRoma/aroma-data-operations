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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class CassandraMessageRepositoryTest 
{

   @Mock
    private Session cassandra;

    @Mock
    private Function<Row, Message> messageMapper;


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
    
    private CassandraMessageRepository instance;

    
    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new CassandraMessageRepository(cassandra, messageMapper);
        verifyZeroInteractions(cassandra, messageMapper);
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
        assertThrows(() -> new CassandraMessageRepository(null, messageMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraMessageRepository(cassandra, null))
            .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    public void testSaveMessage() throws Exception
    {
        instance.saveMessage(message, lifetime);
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, instanceOf(Insert.Options.class));
    }
    
    @Test
    public void testSaveMessageWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveMessage(null, lifetime))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveMessage(message, null))
            .isInstanceOf(InvalidArgumentException.class);
        
        Message emptyMessage = new Message();
        assertThrows(() -> instance.saveMessage(emptyMessage, lifetime))
            .isInstanceOf(InvalidArgumentException.class);
        
        Message messageWithBadId = new Message(message).setMessageId(badId);
        assertThrows(() -> instance.saveMessage(messageWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetMessage() throws Exception
    {
        Message result = instance.getMessage(appId, messageId);
        assertThat(result, is(message));
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, instanceOf(Select.class));
    }
    
    @DontRepeat
    @Test
    public void testGetMessageWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getMessage("", messageId))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getMessage(appId, ""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getMessage(badId, messageId))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getMessage(appId, badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteMessage() throws Exception
    {
        instance.deleteMessage(appId, messageId);

        verify(cassandra).execute(captor.capture());

        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, instanceOf(Delete.Where.class));
    }

    @DontRepeat
    @Test
    public void testDeleteMessageWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteMessage("", messageId))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteMessage(appId, ""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteMessage(badId, messageId))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteMessage(appId, badId))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteMessage(null, null))
            .isInstanceOf(InvalidArgumentException.class);
        
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

    @Test
    public void testDeleteAllMessages() throws Exception
    {
    }

}