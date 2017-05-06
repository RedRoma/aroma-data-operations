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

package tech.aroma.data.performance;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MeasuredApplicationRepositoryTest
{

    @Mock
    private ApplicationRepository delegate;
    
    @GenerateList(Application.class)
    private List<Application> applications;

    @GeneratePojo
    private Application application;

    @GenerateString
    private String appId;

    @GenerateString
    private String appName;
    
    @GenerateString
    private String userId;
    
    @GenerateString
    private String orgId;

    private MeasuredApplicationRepository instance;

    @Before
    public void setUp()
    {
        instance = new MeasuredApplicationRepository(delegate);
        verifyZeroInteractions(delegate);
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new MeasuredApplicationRepository(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSaveApplication() throws Exception
    {
        instance.saveApplication(application);
        verify(delegate).saveApplication(application);
    }

    @DontRepeat
    @Test
    public void testSaveApplicationWhenThrows() throws Exception
    {
        doThrow(new OperationFailedException())
            .when(delegate)
            .saveApplication(application);

        assertThrows(() -> instance.saveApplication(application))
            .isInstanceOf(OperationFailedException.class);

        verify(delegate).saveApplication(application);
    }

    @Test
    public void testDeleteApplication() throws Exception
    {
        instance.deleteApplication(appId);

        verify(delegate).deleteApplication(appId);
    }

    @DontRepeat
    @Test
    public void testDeleteApplicationWhenThrows() throws Exception
    {
        doThrow(new ApplicationDoesNotExistException())
            .when(delegate)
            .deleteApplication(appId);

        assertThrows(() -> instance.deleteApplication(appId))
            .isInstanceOf(ApplicationDoesNotExistException.class);

        verify(delegate).deleteApplication(appId);
    }

    @Test
    public void testGetById() throws Exception
    {
        when(delegate.getById(appId))
            .thenReturn(application);
        
        Application result = instance.getById(appId);
        assertThat(result, is(application));
        
        verify(delegate).getById(appId);
    }

    @Test
    public void testContainsApplication() throws Exception
    {
        boolean expected = one(booleans());
        
        when(delegate.containsApplication(appId))
            .thenReturn(expected);
        
        boolean result = instance.containsApplication(appId);
        assertThat(result, is(expected));
        
        verify(delegate).containsApplication(appId);
    }

    @DontRepeat
    @Test
    public void testContainsApplicationWhenThrows() throws Exception
    {
        when(delegate.containsApplication(appId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.containsApplication(appId))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @Test
    public void testGetApplicationsOwnedBy() throws Exception
    {
        when(delegate.getApplicationsOwnedBy(userId))
            .thenReturn(applications);
        
        List<Application> result = instance.getApplicationsOwnedBy(userId);
        assertThat(result, is(applications));
        
        verify(delegate).getApplicationsOwnedBy(userId);
    }
    
    @DontRepeat
    @Test
    public void testGetApplicationsOwnedByWhenThrows() throws Exception
    {
        when(delegate.getApplicationsOwnedBy(userId))
            .thenThrow(new UserDoesNotExistException());
        
        assertThrows(() -> instance.getApplicationsOwnedBy(userId))
            .isInstanceOf(UserDoesNotExistException.class);
        
        verify(delegate).getApplicationsOwnedBy(userId);
    }

    @Test
    public void testGetApplicationsByOrg() throws Exception
    {
        when(delegate.getApplicationsByOrg(orgId))
            .thenReturn(applications);
        
        List<Application> result = instance.getApplicationsByOrg(orgId);
        assertThat(result, is(applications));
        
        verify(delegate).getApplicationsByOrg(orgId);
    }

    @Test
    public void testSearchByName() throws Exception
    {
        when(delegate.searchByName(appName))
            .thenReturn(applications);
        
        List<Application> result = instance.searchByName(appName);
        assertThat(result, is(applications));
        
        verify(delegate).searchByName(appName);
    }
    
    
    @Test
    public void testSearchByNameWhenThrows() throws Exception
    {
        when(delegate.searchByName(appName))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.searchByName(appName))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testGetRecentlyCreated() throws Exception
    {
        when(delegate.getRecentlyCreated())
            .thenReturn(applications);
        
        List<Application> result = instance.getRecentlyCreated();
        assertThat(result, is(applications));
        
        verify(delegate).getRecentlyCreated();
    }
    
    @DontRepeat
    @Test
    public void testGetRecentlyCreatedWhenThrows() throws Exception
    {
        when(delegate.getRecentlyCreated())
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.getRecentlyCreated())
            .isInstanceOf(OperationFailedException.class);
    }

}
