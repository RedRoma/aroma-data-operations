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


package tech.aroma.data;

import java.util.List;
import org.apache.thrift.TException;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.nonEmptyList;


/**
 * Responsible for storage of Aroma {@linkplain Event Events}.
 * 
 * 
 * @author SirWellington
 */
public interface EventRepository 
{
    void saveEvent(Event event, User forUser) throws TException;
    
    default void saveEvents(@Required Event event, List<User> users) throws TException
    {
        checkThat(users)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyList());
        
        List<TException> exceptions = Lists.create();
        
        users.parallelStream().forEach(user ->
        {
            try
            {
                this.saveEvent(event, user);
            }
            catch (TException ex)
            {
                exceptions.add(ex);
            }
        });
        
        if (!Lists.isEmpty(exceptions))
        {
            throw Lists.oneOf(exceptions);
        }
        
    }
    
    boolean containsEvent(@Required String eventId, @Required User user) throws TException;
    
    Event getEvent(@Required String eventId, @Required User user) throws TException;
    
    List<Event> getAllEventsFor(@Required User user) throws TException;
    
    void deleteEvent(@Required String eventId, @Required User user) throws TException;
    
    void deleteAllEventsFor(@Required User user) throws TException;
}
