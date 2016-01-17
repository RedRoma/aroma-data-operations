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


package tech.aroma.banana.data;

import java.util.List;
import org.apache.thrift.TException;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.User;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.annotations.arguments.Required;


/**
 * The Inbox repository is reponsible for storage and retrieval of messags that are stored
 * for an Applications followers in their own provide "Inbox".
 * 
 * @author SirWellington
 */
public interface InboxRepository 
{
    void saveMessageForUser(@Required Message message, @Required User user) throws TException;

    List<Message> getMessagesForUser(@Required String userId, @Optional String applicationId) throws TException;

    void deleteMessageForUser(@Required String userId, @Required String messageId) throws TException;
    
    void deleteAllMessagesForUser(@Required String userId) throws TException;
    
}
