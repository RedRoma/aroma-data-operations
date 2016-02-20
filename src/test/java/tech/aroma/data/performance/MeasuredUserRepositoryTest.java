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

package tech.aroma.data.performance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MeasuredUserRepositoryTest 
{

    @Mock
    private UserRepository delegate;
    
    private MeasuredUserRepository instance;
    
    @GeneratePojo
    private User user;
    
    @GenerateString
    private String userId;
    
    @GenerateString
    private String email;
    
    @GenerateString
    private String githubProfile;
    
    @Before
    public void setUp()
    {
        instance = new MeasuredUserRepository(delegate);
        verifyZeroInteractions(delegate);
    }
    
    @DontRepeat
    @Test
    public void testConstuctor() throws Exception
    {
        assertThrows(() -> new MeasuredUserRepository(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSaveUser() throws Exception
    {
        instance.saveUser(user);
        verify(delegate).saveUser(user);
    }
    
    @Test
    public void testSaveUserWhenThrows() throws Exception
    {
        doThrow(new OperationFailedException())
            .when(delegate)
            .saveUser(user);
        
        assertThrows(() -> instance.saveUser(user))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testGetUser() throws Exception
    {
        when(delegate.getUser(userId))
            .thenReturn(user);
        
        User result = instance.getUser(userId);
        assertThat(result, is(user));
        verify(delegate).getUser(userId);
    }
    
    @DontRepeat
    @Test
    public void testGetUserWhenThrows() throws Exception
    {
        when(delegate.getUser(userId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.getUser(userId))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testDeleteUser() throws Exception
    {
        instance.deleteUser(userId);
        verify(delegate).deleteUser(userId);
    }
    
    @DontRepeat
    @Test
    public void testDeleteUserWhenThrows() throws Exception
    {
        doThrow(new UserDoesNotExistException())
            .when(delegate)
            .deleteUser(userId);
        
        assertThrows(() -> instance.deleteUser(userId))
            .isInstanceOf(UserDoesNotExistException.class);
        
        verify(delegate).deleteUser(userId);
    }

    @Test
    public void testContainsUser() throws Exception
    {
        boolean expected = one(booleans());
        
        when(delegate.containsUser(userId))
            .thenReturn(expected);
        
        boolean result = instance.containsUser(userId);
        assertThat(result, is(expected));
        verify(delegate).containsUser(userId);
    }
    
    @DontRepeat
    @Test
    public void testContainsUserWhenThrows() throws Exception
    {
        when(delegate.containsUser(userId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.containsUser(userId))
            .isInstanceOf(OperationFailedException.class);
        
        verify(delegate).containsUser(userId);
    }

    @Test
    public void testGetUserByEmail() throws Exception
    {
        when(delegate.getUserByEmail(email))
            .thenReturn(user);
        
        User result = instance.getUserByEmail(email);
        assertThat(result, is(user));
        verify(delegate).getUserByEmail(email);
    }
    
    @DontRepeat
    @Test
    public void testGetUserByEmailWhenThrows() throws Exception
    {
        when(delegate.getUserByEmail(email))
            .thenThrow(new UserDoesNotExistException());
        
        assertThrows(() -> instance.getUserByEmail(email))
            .isInstanceOf(UserDoesNotExistException.class);
        
        verify(delegate).getUserByEmail(email);
    }

    @Test
    public void testFindByGithubProfile() throws Exception
    {
        when(delegate.findByGithubProfile(githubProfile))
            .thenReturn(user);
        
        User result = instance.findByGithubProfile(githubProfile);
        assertThat(result, is(user));
        
        verify(delegate).findByGithubProfile(githubProfile);
    }
    
    @DontRepeat
    @Test
    public void testFindByGithubProfileWhenThrows() throws Exception
    {
        when(delegate.findByGithubProfile(githubProfile))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.findByGithubProfile(githubProfile))
            .isInstanceOf(OperationFailedException.class);
        
        verify(delegate).findByGithubProfile(githubProfile);
    }

}
