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
import java.util.List;
import java.util.function.Function;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.aroma.thrift.generators.EventGenerators.events;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraActivityRepositoryIT
{

    private static Session session;
    private static QueryBuilder queryBuilder;
    private static Function<Row, Event> eventMapper;

    @BeforeClass
    public static void begin()
    {
        queryBuilder = TestCassandraProviders.getQueryBuilder();
        session = TestCassandraProviders.getTestSession();
        eventMapper = Mappers.eventMapper();
    }

    private String eventId;

    private Event event;
    private List<Event> events;

    @GenerateString(UUID)
    private String userId;

    @GeneratePojo
    private User user;

    private CassandraActivityRepository instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();

        instance = new CassandraActivityRepository(session, queryBuilder, eventMapper);
    }

    private void setupData() throws Exception
    {
        event = one(events());
        eventId = event.eventId;

        events = listOf(events(), 15);

        user.userId = userId;
    }

    @After
    public void cleanUp() throws Exception
    {
        deleteAllEvents();
    }

    private void deleteEvent(Event event)
    {
        try
        {
            instance.deleteEvent(event.eventId, user);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to delete Event: " + ex.getMessage());
        }
    }

    private void deleteAllEvents()
    {
        try
        {
            instance.deleteAllEventsFor(user);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to delete all events for user: " + ex.getMessage());
        }
    }

    @Test
    public void testSaveEvent() throws Exception
    {
        instance.saveEvent(event, user);

        assertThat(instance.containsEvent(eventId, user), is(true));
    }

    @Test
    public void testContainsEvent() throws Exception
    {
        assertThat(instance.containsEvent(eventId, user), is(false));

        instance.saveEvent(event, user);

        assertThat(instance.containsEvent(eventId, user), is(true));
    }

    @Test
    public void testGetEvent() throws Exception
    {
        instance.saveEvent(event, user);

        Event result = instance.getEvent(eventId, user);
        assertThat(result, is(event));
    }

    @Test
    public void testGetEventWhenNotExists() throws Exception
    {
        assertThrows(() -> instance.getEvent(eventId, user))
            .isInstanceOf(DoesNotExistException.class);
    }

    @Test
    public void testGetAllEventsFor() throws Exception
    {
        for (Event event : events)
        {
            instance.saveEvent(event, user);
        }

        List<Event> result = instance.getAllEventsFor(user);
        result.forEach(e -> assertThat(e, isIn(events)));
        events.forEach(e -> assertThat(e, isIn(result)));
    }

    @Test
    public void testGetAllEventsWhenNone() throws Exception
    {
        List<Event> results = instance.getAllEventsFor(user);
        assertThat(results, notNullValue());
        assertThat(results, is(empty()));
    }

    @Test
    public void testDeleteEvent() throws Exception
    {
        instance.saveEvent(event, user);
        assertThat(instance.containsEvent(eventId, user), is(true));

        instance.deleteEvent(eventId, user);
        assertThat(instance.containsEvent(eventId, user), is(false));
    }

    @Test
    public void testDeleteEventWhenNotExists() throws Exception
    {
        instance.deleteEvent(eventId, user);
    }

    @Test
    public void testDeleteAllEventsFor() throws Exception
    {
        for(Event event : events)
        {
            instance.saveEvent(event, user);
        }
        
        instance.deleteAllEventsFor(user);
        
        for (Event event : events)
        {
            assertThat(instance.containsEvent(event.eventId, user), is(false));
        }
    }

    @Test
    public void testDeleteAllEventsForWhenNoneExist() throws Exception
    {
        instance.deleteAllEventsFor(user);
    }

}
