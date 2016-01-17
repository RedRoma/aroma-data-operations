
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

import tech.aroma.banana.thrift.Message;
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
    String saveMessage(@Required Message message);
    
    Message getMessage(@Required String messageId);
    
    void deleteMessage(@Required String messageId);
}
