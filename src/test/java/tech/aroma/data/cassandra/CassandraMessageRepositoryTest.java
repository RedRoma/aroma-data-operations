/*
 * Copyright 2016 RedRoma.
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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import java.util.List;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.TimeUnit;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
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
    }

    @Test
    public void testDeleteMessage() throws Exception
    {
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