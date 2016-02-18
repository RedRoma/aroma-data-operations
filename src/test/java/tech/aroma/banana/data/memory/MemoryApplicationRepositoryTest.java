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
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MemoryApplicationRepositoryTest
{
    
    @GeneratePojo
    private Application application;
    
    @GenerateList(Application.class)
    private List<Application> applications;
    
    @GenerateString(UUID)
    private String applicationId;
    
    private MemoryApplicationRepository instance;
    
    @GenerateString(UUID)
    private String orgId;
    
    @Before
    public void setUp()
    {
        application.applicationId = applicationId;
        application.organizationId = orgId;
        
        instance = new MemoryApplicationRepository();
        
        applications = applications.stream()
            .map(app -> app.setApplicationId(one(uuids)))
            .map(app -> app.setOrganizationId(orgId))
            .collect(toList());
    }
    
    private void saveApplications(List<Application> applications) throws TException
    {
        for(Application app : applications)
        {
            instance.saveApplication(app);
        }
    }
    
    @Test
    public void testSaveApplication() throws Exception
    {
        instance.saveApplication(application);
        
        Application result = instance.getById(applicationId);
        assertThat(result, is(application));
    }
    
    @DontRepeat
    @Test
    public void testSaveApplicationWithBadArguments() throws Exception
    {
        assertThrows(() -> instance.saveApplication(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        Application emptyApplication = new Application();
        
        assertThrows(() -> instance.saveApplication(emptyApplication))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testDeleteApplication() throws Exception
    {
        instance.saveApplication(application);
        
        instance.deleteApplication(applicationId);
        
        assertThat(instance.containsApplication(applicationId), is(false));
    }
    
    @DontRepeat
    @Test
    public void testDeleteApplicationWhenIDDoesNotExists() throws Exception
    {
        assertThrows(() -> instance.deleteApplication(applicationId))
            .isInstanceOf(ApplicationDoesNotExistException.class);
    }
    
    @Test
    public void testGetById() throws Exception
    {
        instance.saveApplication(application);
        
        Application result = instance.getById(applicationId);
        assertThat(result, is(application));
        
        String randomId = one(uuids);
        assertThrows(() -> instance.getById(randomId))
            .isInstanceOf(TException.class);
    }
    
    @Test
    public void testGetApplicationsOwnedBy() throws Exception
    {
        User user = one(pojos(User.class));
        
        applications.forEach(app -> app.owners.add(user.userId));
        
        saveApplications(applications);        
        
        List<Application> result = instance.getApplicationsOwnedBy(user.userId);
        assertThat(Sets.containTheSameElements(applications, result), is(true));
    }
    
    @DontRepeat
    @Test
    public void testGetApplicationOwnedByWhenNoneOwned() throws Exception
    {
        User user = one(pojos(User.class));
        
        List<Application> result = instance.getApplicationsOwnedBy(user.userId);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }
    
    @Test
    public void testGetApplicationsByOrg() throws Exception
    {
        applications.forEach(app -> app.setOrganizationId(orgId));
        
        saveApplications(applications);        
        
        List<Application> result = instance.getApplicationsByOrg(orgId);
        assertThat(Sets.containTheSameElements(result, applications), is(false));
    }
    
    @DontRepeat
    @Test
    public void testGetApplicationsByOrgWhenNone() throws Exception
    {
        List<Application> result = instance.getApplicationsByOrg(orgId);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }
    
    @Test
    public void testSearchByName() throws Exception
    {
        int length = one(integers(100, 200));
        String name = one(alphabeticString(length));
        String term = name.substring(length / 2);
        
        application.setName(name);
        instance.saveApplication(application);
        
        List<Application> result = instance.searchByName(term);
        assertThat(result, contains(application));
    }
    
    @Test
    public void testGetRecentlyCreated() throws Exception
    {
        saveApplications(applications);
        
        List<Application> result = instance.getRecentlyCreated();
        assertThat(result, not(empty()));
        assertThat(Sets.containTheSameElements(result, applications), is(true));
    }
    
    @Test
    public void testContains() throws Exception
    {
        instance.saveApplication(application);
        
        assertThat(instance.containsApplication(applicationId), is(true));
        
        String randomId = one(uuids);
        assertThat(instance.containsApplication(randomId), is(false));
    }
    
}
