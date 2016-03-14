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

package tech.aroma.data.memory;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.aroma.thrift.generators.EventGenerators.events;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class MemoryActivityRepositoryTest 
{
    private Event event;
    
    private List<Event> events;
    
    @GeneratePojo
    private User user;
    
    @GenerateString(UUID)
    private String userId;
    
    private String eventId;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    private MemoryActivityRepository instance;
    

    @Before
    public void setUp() throws Exception
    {
        instance = new MemoryActivityRepository();
        
        setupData();
    }


    private void setupData() throws Exception
    {
        user.userId = userId;
        
        event = one(events());
        eventId = event.eventId;
        
        events = listOf(events());
    }

    @Test
    public void testSaveEvent() throws Exception
    {
        instance.saveEvent(event, user);
        
        assertThat(instance.containsEvent(eventId, user), is(true));
    }

    @Test
    public void testSaveEventWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveEvent(null, user))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveEvent(event, null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetEvent() throws Exception
    {
        instance.saveEvent(event, user);
        
        Event result = instance.getEvent(eventId, user);
        assertThat(result, is(event));
    }
    
    @Test
    public void testGetEventWhenNotExist() throws Exception
    {
        assertThrows(() -> instance.getEvent(eventId, user))
            .isInstanceOf(DoesNotExistException.class);
    }

    @Test
    public void testGetAllEventsFor() throws Exception
    {
        for (Event element : events)
        {
            instance.saveEvent(element, user);
        }
        
        List<Event> result = instance.getAllEventsFor(user);
        assertThat(result, is(events));
    }


    @Test
    public void testGetAllEventsForWhenNone() throws Exception
    {
        List<Event> result = instance.getAllEventsFor(user);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }
    
    @Test
    public void testGetAllEventsForWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getAllEventsFor(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getAllEventsFor(new User()))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteEvent() throws Exception
    {
        instance.saveEvent(event, user);
        
        instance.deleteEvent(eventId, user);
        
        assertThat(instance.containsEvent(eventId, user), is(false));
    }

    @Test
    public void testDeleteEventWhenNotExists() throws Exception
    {
        instance.deleteEvent(eventId, user);
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
    }

    @Test
    public void testDeleteAllEventsFor() throws Exception
    {
        for (Event e : events)
        {
            instance.saveEvent(e, user);
        }
        
        instance.deleteAllEventsFor(user);
        
        assertThat(instance.getAllEventsFor(user), is(empty()));
    }

    @Test
    public void testDeleteAllEventsForWhenNone() throws Exception
    {
        instance.deleteAllEventsFor(user);
    }

    @Test
    public void testDeleteAllEventsForWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteAllEventsFor(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteAllEventsFor(new User()))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteAllEventsFor(new User().setUserId(badId)))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testContainsEventWhenContains() throws Exception
    {
        instance.saveEvent(event, user);
        assertThat(instance.containsEvent(eventId, user), is(true));
    }
        
    public void testContainsEventWhenNotContains() throws Exception
    {
        assertThat(instance.containsEvent(eventId, user), is(false));
    }

}