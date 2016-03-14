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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.EventGenerators.events;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class CassandraActivityRepositoryTest 
{
    
    @Mock(answer = RETURNS_MOCKS)
    private Cluster cluster;
    
    @Mock
    private Session session;
    
    private QueryBuilder queryBuilder;
    
    @Mock
    private Function<Row, Event> eventMapper;
    
    @Mock
    private ResultSet results;
    
    @Mock
    private Row row;
    
    private Event event;
    
    @GeneratePojo
    private User user;
    
    private String eventId;
    
    @GenerateString(UUID)
    private String userId;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    private CassandraActivityRepository instance;

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new CassandraActivityRepository(session, queryBuilder, eventMapper);
    }


    private void setupData() throws Exception
    {
        event = one(events());
        eventId = event.eventId;
        
        user.userId = userId;
    }

    private void setupMocks() throws Exception
    {
        when(results.one()).thenReturn(row);
        
        queryBuilder = new QueryBuilder(cluster);
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new CassandraActivityRepository(null, queryBuilder, eventMapper));
        assertThrows(() -> new CassandraActivityRepository(session, null, eventMapper));
        assertThrows(() -> new CassandraActivityRepository(session, queryBuilder, null));
    }

    @Test
    public void testSaveEvent() throws Exception
    {
        instance.saveEvent(event, user);
        
    }
    
    @Test
    public void testSaveEventWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveEvent(null, user))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveEvent(event, null))
            .isInstanceOf(InvalidArgumentException.class);
        
        User userMissingId = new User();
        assertThrows(() -> instance.saveEvent(event, userMissingId))
            .isInstanceOf(InvalidArgumentException.class);
        
        Event eventMissingId = new Event(event);
        eventMissingId.unsetEventId();
        
        assertThrows(() -> instance.saveEvent(eventMissingId, user))
            .isInstanceOf(InvalidArgumentException.class);
        
        User userWithBadId = new User(user).setUserId(badId);
        assertThrows(() -> instance.saveEvent(event, userWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
        
        Event eventWithBadId = new Event(event).setEventId(badId);
        assertThrows(() -> instance.saveEvent(eventWithBadId, user))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testContainsEvent() throws Exception
    {
    }

    @Test
    public void testGetEvent() throws Exception
    {
    }

    @Test
    public void testGetAllEventsFor() throws Exception
    {
    }

    @Test
    public void testDeleteEvent() throws Exception
    {
    }

    @Test
    public void testDeleteAllEventsFor() throws Exception
    {
    }

}