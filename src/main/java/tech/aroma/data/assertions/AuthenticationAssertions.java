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

package tech.aroma.data.assertions;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import tech.aroma.data.TokenRepository;
 import tech.aroma.thrift.authentication.AuthenticationToken;
 import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
 import tech.sirwellington.alchemy.annotations.arguments.Required;
 import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
 import tech.sirwellington.alchemy.arguments.FailedAssertionException;

 import static tech.sirwellington.alchemy.arguments.Arguments.*;
 import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
 import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@NonInstantiable
public final class AuthenticationAssertions
{
    
    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationAssertions.class);

    AuthenticationAssertions() throws IllegalAccessException
    {
        throw new IllegalAccessException("cannot instantiate");
    }
    
    public static AlchemyAssertion<String> tokenInRepository(@Required TokenRepository repository) throws IllegalArgumentException
    {
        checkThat(repository)
            .usingMessage("repository missing")
            .is(notNull());
        
        return token ->
        {
            boolean exists;
            try
            {
                exists = repository.containsToken(token);
            }
            catch (Exception ex)
            {
                throw new FailedAssertionException("Could not check in repository", ex);
            }
            
            if (!exists)
            {
                throw new FailedAssertionException("Token does not exist: " + token);
            }
        };
    }
    
    public static AlchemyAssertion<AuthenticationToken> completeToken()
    {
        return token ->
        {
            checkThat(token)
                .usingMessage("token is null")
                .is(notNull());
            
            checkThat(token.tokenId, token.ownerId)
                .usingMessage("tokenId and ownerId are required")
                .are(nonEmptyString());
            
            checkThat(token.tokenType)
                .usingMessage("Token Type is required in token")
                .is(notNull());
        };
    }
    
}
