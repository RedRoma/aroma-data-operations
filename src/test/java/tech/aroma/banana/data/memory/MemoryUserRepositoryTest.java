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

package tech.aroma.banana.data.memory;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MemoryUserRepositoryTest 
{
    
    @GeneratePojo
    private User user;
    
    @GenerateString(UUID)
    private String userId;

    private MemoryUserRepository instance;
    
    @Before
    public void setUp()
    {
        user.userId = userId;
        
        instance = new MemoryUserRepository();
    }

    @Test
    public void testSaveUser() throws Exception
    {
        instance.saveUser(user);
        User result = instance.getUser(userId);
        assertThat(result, is(user));
    }

    @DontRepeat
    @Test
    public void testSaveUserWithBadArgs()
    {
        assertThrows(() -> instance.saveUser(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveUser(new User()))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    
    @Test
    public void testGetUser() throws Exception
    {
        instance.saveUser(user);
        
        User result = instance.getUser(userId);
        assertThat(result, is(user));
    }
    
    @Test
    public void testGetUserWhenNotExists() throws Exception
    {
        assertThrows(() -> instance.getUser(userId))
            .isInstanceOf(UserDoesNotExistException.class);
    }
    
    @DontRepeat
    @Test
    public void testGetUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getUser(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getUser(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteUser() throws Exception
    {
        instance.saveUser(user);
        assertThat(instance.containsUser(userId), is(true));
        
        instance.deleteUser(userId);
        assertThat(instance.containsUser(userId), is(false));
        
        assertThrows(() -> instance.getUserByEmail(user.email))
            .isInstanceOf(UserDoesNotExistException.class);
    }
    
    @Test
    public void testDeleteUserWhenNotExist() throws Exception
    {
        assertThrows(() -> instance.deleteUser(userId))
            .isInstanceOf(UserDoesNotExistException.class);
    }
    
    @Test
    public void testDeleteUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteUser(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteUser(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testContainsUser() throws Exception
    {
        assertThat(instance.containsUser(userId), is(false));
        
        instance.saveUser(user);
        
        assertThat(instance.containsUser(userId), is(true));
    }

    @Test
    public void testGetUserByEmail() throws Exception
    {
        instance.saveUser(user);
        
        String email = user.email;
        User result = instance.getUserByEmail(email);
        assertThat(result, is(user));
    }

    @Test
    public void testGetUserByEmailWhenNotExists() throws Exception
    {
        assertThrows(() -> instance.getUserByEmail(userId))
            .isInstanceOf(UserDoesNotExistException.class);
    }

    @DontRepeat
    @Test
    public void testGetUserByEmailWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getUserByEmail(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getUserByEmail(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testFindByGithubProfile() throws Exception
    {
        instance.saveUser(user);
        String profile = user.githubProfile;
        
        User result = instance.findByGithubProfile(profile);
        assertThat(result, is(user));
    }

    @Test
    public void testFindByGithubProfileWheNotExists() throws Exception
    {
        String profile = user.githubProfile;
        
        assertThrows(() -> instance.findByGithubProfile(profile))
            .isInstanceOf(UserDoesNotExistException.class);
    }

    @DontRepeat
    @Test
    public void testFindByGithubProfileWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.findByGithubProfile(""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.findByGithubProfile(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetRecentlyCreatedUsers() throws Exception
    {
        List<User> result = instance.getRecentlyCreatedUsers();
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
        
        instance.saveUser(user);
        
        result = instance.getRecentlyCreatedUsers();
        assertThat(result, contains(user));
    }

}
