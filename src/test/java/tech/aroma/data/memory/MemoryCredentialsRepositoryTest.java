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

package tech.aroma.data.memory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MemoryCredentialsRepositoryTest 
{

    @GenerateString(UUID)
    private String userId;

    @GenerateString(ALPHABETIC)
    private String badId;
    
    @GenerateString(HEXADECIMAL)
    private String password;
    
    private MemoryCredentialsRepository instance;
    
    @Before
    public void setUp() throws Exception
    {
        
        instance = new MemoryCredentialsRepository();
    }

    @Test
    public void testSaveEncryptedPassword() throws Exception
    {
        instance.saveEncryptedPassword(userId, password);
        
        assertThat(instance.containsEncryptedPassword(userId), is(true));
    }
    
    @DontRepeat
    @Test
    public void testSaveEncryptedPasswordWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveEncryptedPassword(badId, password))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveEncryptedPassword(userId, ""))
            .isInstanceOf(InvalidArgumentException.class);
        
    }

    @Test
    public void testContainsEncryptedPassword() throws Exception
    {
        boolean result = instance.containsEncryptedPassword(userId);
        assertThat(result, is(false));
        
        instance.saveEncryptedPassword(userId, password);
        
        result = instance.containsEncryptedPassword(userId);
        assertThat(result, is(true));
    }

    @Test
    public void testGetEncryptedPassword() throws Exception
    {
        instance.saveEncryptedPassword(userId, password);
        
        String result = instance.getEncryptedPassword(userId);
        assertThat(result, is(password));
    }

    
    @Test
    public void testGetEncryptedPasswordWhenNotExists() throws Exception
    {
        assertThrows(() -> instance.getEncryptedPassword(userId))
            .isInstanceOf(DoesNotExistException.class);
    }
    
    @DontRepeat
    @Test
    public void testGetEncryptedPasswordWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getEncryptedPassword(""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getEncryptedPassword(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteEncryptedPassword() throws Exception
    {
        instance.deleteEncryptedPassword(userId);
        
        instance.saveEncryptedPassword(userId, password);
        
        instance.deleteEncryptedPassword(userId);
        
        assertThat(instance.containsEncryptedPassword(userId), is(false));
    }
    
    @Test
    public void testDeleteEncryptedPasswordWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteEncryptedPassword(""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteEncryptedPassword(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

}
