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

 
package tech.aroma.banana.data.assertions;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.User;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
 * @author SirWellington
 */
@NonInstantiable
@Internal
public final class RequestAssertions 
{
    private final static Logger LOG = LoggerFactory.getLogger(RequestAssertions.class);

    RequestAssertions() throws IllegalAccessException
    {
        throw new IllegalAccessException("cannot instantiate");
    }
    
    public static AlchemyAssertion<Application> validApplication()
    {
        return app ->
        {
            checkThat(app)
                .is(notNull());
            
            checkThat(app.applicationId, app.name)
                .are(nonEmptyString());
            
            checkThat(app.applicationId)
                .is(validAppId());
        };
    }
    
    public static AlchemyAssertion<Message> validMessage()
    {
        return message ->
        {
            checkThat(message).is(notNull());
            
            checkThat(message.messageId)
                .is(validMessageId());
            
            checkThat(message.title)
                .usingMessage("message missing Title")
                .is(nonEmptyString());
        };
    }
    
    public static AlchemyAssertion<User> validUser()
    {
        return user ->
        {
            checkThat(user)
                .is(notNull());
            
            checkThat(user.userId)
                .is(validUserId());
            
            checkThat(user.name)
                .is(nonEmptyString());
        };
    }
    
    public static AlchemyAssertion<String> validAppId()
    {
        return appId ->
        {
            checkThat(appId)
                .usingMessage("missing appId")
                .is(nonEmptyString())
                .usingMessage("appId must be a UUID")
                .is(validUUID());
        };
    }
    
    public static AlchemyAssertion<String> validMessageId()
    {
        return msgId ->
        {
            checkThat(msgId)
                .usingMessage("missing messageID")
                .is(nonEmptyString())
                .usingMessage("messageID must be a UUID type")
                .is(validUUID());
        };
    }
    
    public static AlchemyAssertion<String> validUserId()
    {
        return userId ->
        {
            checkThat(userId)
                .usingMessage("missing userId")
                .is(nonEmptyString())
                .usingMessage("userId must be a UUID")
                .is(validUUID());
        };
    }

    public static boolean isNullOrEmpty(@Optional String string)
    {
        return string == null || string.isEmpty();
    }
    
}
