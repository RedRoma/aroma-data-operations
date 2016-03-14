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


import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.validUser;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

import tech.aroma.data.ActivityRepository;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

/**
 *
 * @author SirWellington
 */
@Internal
final class MemoryEventRepository implements ActivityRepository
{
    private final static Logger LOG = LoggerFactory.getLogger(MemoryEventRepository.class);

    private final Map<User, List<Event>> events = Maps.createSynchronized();
    
    @Override
    public void saveEvent(Event event, User forUser) throws TException
    {
        checkEvent(event);
        checkUser(forUser);
        User user = forUser;
        
        List<Event> eventsForUser = events.getOrDefault(user, Lists.create());
        eventsForUser.add(event);
        events.put(user, eventsForUser);
    }

    @Override
    public boolean containsEvent(@Required String eventId, @Required User user) throws TException
    {
        checkEventId(eventId);
        checkUser(user);
        
        return this.events.getOrDefault(user, Lists.emptyList())
            .stream()
            .anyMatch(e -> Objects.equal(e.eventId, eventId));
    }

    
    @Override
    public Event getEvent(String eventId, User user) throws TException
    {
        checkUser(user);
        checkEventId(eventId);
        
        return events.getOrDefault(user, Lists.emptyList())
            .stream()
            .filter(e -> Objects.equal(e.eventId, eventId))
            .findFirst()
            .orElseThrow(() -> new DoesNotExistException("Event does not exist"));
    }

    @Override
    public List<Event> getAllEventsFor(User user) throws TException
    {
        checkUser(user);
        
        return events.getOrDefault(user, Lists.emptyList());
    }

    @Override
    public void deleteEvent(String eventId, User user) throws TException
    {
        checkEventId(eventId);
        checkUser(user);
        
        List<Event> eventsForUser = events.getOrDefault(user, Lists.emptyList())
            .stream()
            .filter(e -> !Objects.equal(e.eventId, eventId))
            .collect(toList());
        
        events.put(user, eventsForUser);
    }

    @Override
    public void deleteAllEventsFor(User user) throws TException
    {
        checkUser(user);
        
        events.remove(user);
    }

    private void checkEvent(Event event) throws InvalidArgumentException
    {
        checkThat(event)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Event cannot be null")
            .is(notNull());
        
        checkThat(event.eventId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("eventId must be a valid UUID")
            .is(validUUID());
    }

    private void checkUser(User user) throws InvalidArgumentException
    {
        checkThat(user)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Invalid User")
            .is(validUser());
    }

    private void checkEventId(String eventId) throws InvalidArgumentException
    {
        checkThat(eventId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("EventID missing")
            .is(nonEmptyString())
            .usingMessage("Event ID must be a valid UUID")
            .is(validUUID());
    }

}
