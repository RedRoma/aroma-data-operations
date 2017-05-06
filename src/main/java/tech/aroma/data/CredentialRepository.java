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


package tech.aroma.data;

import org.apache.thrift.TException;
import tech.sirwellington.alchemy.annotations.arguments.Required;


/**
 * Responsible for storage of User Credentials, for use with Authenticating Login requests.
 * 
 * @author SirWellington
 */
public interface CredentialRepository 
{
    /**
     * Saves an encrypted password associated with the given userId.
     * 
     * Note that this also replaces any password that was currently existing.
     * @param userId
     * @param encryptedPassword The password is expected to already be encrypted.
     * 
     * @throws TException 
     */
    void saveEncryptedPassword(@Required String userId, @Required String encryptedPassword) throws TException;
    
    boolean containsEncryptedPassword(@Required String userId) throws TException;
    
    String getEncryptedPassword(@Required String userId) throws TException;
    
    void deleteEncryptedPassword(@Required String userId) throws TException;
}
