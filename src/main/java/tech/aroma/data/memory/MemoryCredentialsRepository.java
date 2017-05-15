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

 
package tech.aroma.data.memory;


import java.util.Map;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.data.CredentialRepository;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.keyInMap;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 *
 * @author SirWellington
 */
@Internal
final class MemoryCredentialsRepository implements CredentialRepository
{
    private final static Logger LOG = LoggerFactory.getLogger(MemoryCredentialsRepository.class);

    private final Map<String, String> passwords = Maps.createSynchronized();
    
    @Override
    public void saveEncryptedPassword(String userId, String encryptedPassword) throws TException
    {
        checkUserId(userId);
        checkPassword(encryptedPassword);
        
        passwords.put(userId, encryptedPassword);
    }

    @Override
    public boolean containsEncryptedPassword(String userId) throws TException
    {
        checkUserId(userId);
        
        return passwords.containsKey(userId);
    }

    @Override
    public String getEncryptedPassword(String userId) throws TException
    {
        checkUserId(userId);
        
        checkThat(userId)
            .throwing(DoesNotExistException.class)
            .is(keyInMap(passwords));
        
        return passwords.get(userId);
    }

    @Override
    public void deleteEncryptedPassword(String userId) throws TException
    {
        checkUserId(userId);
        
        passwords.remove(userId);
    }

    private void checkPassword(String encryptedPassword) throws InvalidArgumentException
    {
        checkThat(encryptedPassword)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
    }

    private void checkUserId(String userId) throws InvalidArgumentException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(validUserId());
    }

}
