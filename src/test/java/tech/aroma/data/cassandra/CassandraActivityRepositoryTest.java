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
import java.util.Map;
import java.util.function.Function;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.EventGenerators.events;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
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
public class CassandraActivityRepositoryTest 
{
    
    @Mock
    private Session session;
    
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
    
    @Captor
    private ArgumentCaptor<Statement> captor;

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new CassandraActivityRepository(session, eventMapper);
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
        
        when(session.execute(any(Statement.class)))
            .thenReturn(results);
        
        when(eventMapper.apply(row)).thenReturn(event);
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new CassandraActivityRepository(null, eventMapper));
        assertThrows(() -> new CassandraActivityRepository(session, null));
    }

    @Test
    public void testSaveEvent() throws Exception
    {
        instance.saveEvent(event, user);
        
        verify(session).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Insert.Options.class)));
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
    public void testContainsEventWhenContains() throws Exception
    {
        long count = one(positiveLongs());
        when(row.getLong(0)).thenReturn(count);
        
        boolean result = instance.containsEvent(eventId, user);
        assertThat(result, is(true));
    }

    @Test
    public void testContainsEventWhenNotContains() throws Exception
    {
        when(row.getLong(0)).thenReturn(0L);
        
        boolean result = instance.containsEvent(eventId, user);
        assertThat(result, is(false));
    }

    @Test
    public void testGetEvent() throws Exception
    {
        Event result = instance.getEvent(eventId, user);
        
        assertThat(result, is(event));
        
        verify(session).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Select.Where.class)));
    }
    
    @Test
    public void testGetEventWhenNotExists() throws Exception
    {
        when(eventMapper.apply(row))
            .thenReturn(null);
        
        assertThrows(() -> instance.getEvent(eventId, user))
            .isInstanceOf(DoesNotExistException.class);
    }
    
    @Test
    public void testGetEventWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getEvent("", user))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getEvent(badId, user))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getEvent(eventId, null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getEvent(eventId, new User()))
            .isInstanceOf(InvalidArgumentException.class);
        
        User userWithBadId = new User(user).setUserId(badId);
        assertThrows(() -> instance.getEvent(eventId, userWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
        
    }

    @Test
    public void testGetAllEventsFor() throws Exception
    {
        List<Event> events = listOf(events());
        List<Row> rows = Lists.create();
        
        Map<Row, Event> rowMap = Maps.create();
        
        for (Event event : events)
        {
            Row row = mock(Row.class);
            when(eventMapper.apply(row)).thenReturn(event);
            rows.add(row);
            
            rowMap.put(row, event);
        }
        
        when(results.iterator()).thenReturn(rows.iterator());
        when(results.all()).thenReturn(rows);
        
        List<Event> response = instance.getAllEventsFor(user);
        assertThat(response, is(events));
    }

    @Test
    public void testGetAllEventsForWhenNone() throws Exception
    {
        List<Row> rows = Lists.emptyList();
        when(results.iterator()).thenReturn(rows.iterator());
        
        List<Event> results = instance.getAllEventsFor(user);
        assertThat(results, notNullValue());
        assertThat(results, is(empty()));
    }

    @Test
    public void testGetAllEventsForWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getAllEventsFor(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getAllEventsFor(new User()))
            .isInstanceOf(InvalidArgumentException.class);
        
        User userWithBadId = new User(user).setUserId(badId);
        assertThrows(() -> instance.getAllEventsFor(userWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
        
    }

    @Test
    public void testDeleteEvent() throws Exception
    {
        instance.deleteEvent(eventId, user);
        
        verify(session).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Delete.Where.class)));
    }

    @Test
    public void testDeleteEventWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteEvent("", user))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteEvent(eventId, null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteEvent(eventId, new User()))
            .isInstanceOf(InvalidArgumentException.class);
        
        User userWithBadId = new User(user).setUserId(badId);
        assertThrows(() -> instance.deleteEvent(eventId, userWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
        
    }

    @Test
    public void testDeleteAllEventsFor() throws Exception
    {
        instance.deleteAllEventsFor(user);
        
        verify(session).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Delete.Where.class)));
    }

    @Test
    public void testDeleteAllEventsForWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteAllEventsFor(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteAllEventsFor(new User()))
            .isInstanceOf(InvalidArgumentException.class);
        
        User userWithBadId = new User(user).setUserId(badId);
        assertThrows(() -> instance.deleteAllEventsFor(userWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
        
    }

}