
package tech.aroma.banana.data;

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

import java.util.List;
import org.apache.thrift.TException;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.thrift.LengthOfTime;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.TimeUnit;
import tech.sirwellington.alchemy.annotations.arguments.Required;


/*
 * It may be necessary to create a separate Pojo if the Service Operations
 * need more information than is provided by the Thrift Message Object.
 */

/**
 * Answers questions and performs actions relating to {@linkplain Message Messages}.
 * 
 * @author SirWellington
 */
public interface MessageRepository
{

    default void saveMessage(@Required Message message) throws TException
    {
        LengthOfTime defaultTime = new LengthOfTime()
        .setValue(1)
        .setUnit(TimeUnit.WEEKS);
        
        this.saveMessage(message, defaultTime);
    }
    
    void saveMessage(@Required Message message, @Required LengthOfTime lifetime) throws TException;

    Message getMessage(@Required String applicationId, @Required String messageId) throws TException;

    void deleteMessage(@Required String applicationId, @Required String messageId) throws TException;
    
    default void deleteAllMessages(@Required String applicationId) throws TException
    {
        List<TException> exceptions = Lists.create();
        
        getByApplication(applicationId)
            .stream()
            .forEach(message -> 
            {
                try
                {
                    deleteMessage(applicationId, message.messageId);
                }
                catch(TException ex)
                {
                    exceptions.add(ex);
                }
            });
        
        if(!Lists.isEmpty(exceptions))
        {
            throw Lists.oneOf(exceptions);
        }
    }

    boolean containsMessage(@Required String applicationId, @Required String messageId) throws TException;

    List<Message> getByHostname(@Required String hostname) throws TException;

    List<Message> getByApplication(@Required String applicationId) throws TException;

    List<Message> getByTitle(@Required String applicationId, @Required String title) throws TException;
    
    long getCountByApplication(@Required String applicationId) throws TException;
}
