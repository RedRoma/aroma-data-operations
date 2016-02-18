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
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import java.util.List;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.thrift.LengthOfTime;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.TimeUnit;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
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

    @Mock(answer = RETURNS_MOCKS)
    private Cluster cluster;

    @Mock
    private Session cassandra;

    private QueryBuilder queryBuilder;

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

        instance = new CassandraInboxRepository(cassandra, queryBuilder, messageMapper);
    }

    private void setupData() throws Exception
    {
        message.messageId = messageId;
        message.applicationId = appId;
        user.userId = userId;

    }

    private void setupMocks() throws Exception
    {
        queryBuilder = new QueryBuilder(cluster);

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
        assertThrows(() -> new CassandraInboxRepository(null, queryBuilder, messageMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraInboxRepository(cassandra, null, messageMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraInboxRepository(cassandra, queryBuilder, null))
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
