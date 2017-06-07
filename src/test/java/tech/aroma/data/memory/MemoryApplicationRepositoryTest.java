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

import java.util.List;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.generators.ApplicationGenerators;
import tech.aroma.thrift.generators.UserGenerators;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static sir.wellington.alchemy.collections.sets.Sets.containTheSameElements;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 * @author SirWellington
 */
@Repeat
@RunWith(AlchemyTestRunner.class)
public class MemoryApplicationRepositoryTest
{

    private Application app;
    private String appId;
    private String orgId;

    private List<Application> applications;

    private MemoryApplicationRepository instance;

    @Before
    public void setUp()
    {
        app = one(ApplicationGenerators.applications());
        appId = app.applicationId;
        orgId = app.organizationId;

        instance = new MemoryApplicationRepository();

        applications = listOf(ApplicationGenerators.applications(), 10)
                            .stream()
                            .map(app -> app.setOrganizationId(orgId))
                            .collect(toList());
    }

    private void saveApplications(List<Application> applications) throws TException
    {
        for (Application app : applications)
        {
            instance.saveApplication(app);
        }
    }

    @Test
    public void testSaveApplication() throws Exception
    {
        instance.saveApplication(app);

        Application result = instance.getById(appId);
        assertThat(result, is(app));
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
        instance.saveApplication(app);

        instance.deleteApplication(appId);

        assertThat(instance.containsApplication(appId), is(false));
    }

    @DontRepeat
    @Test
    public void testDeleteApplicationWhenIDDoesNotExists() throws Exception
    {
        assertThrows(() -> instance.deleteApplication(appId))
                .isInstanceOf(ApplicationDoesNotExistException.class);
    }

    @Test
    public void testGetById() throws Exception
    {
        instance.saveApplication(app);

        Application result = instance.getById(appId);
        assertThat(result, is(app));

        String randomId = one(uuids);
        assertThrows(() -> instance.getById(randomId))
                .isInstanceOf(TException.class);
    }

    @Test
    public void testGetApplicationsOwnedBy() throws Exception
    {
        User user = one(UserGenerators.users());

        applications.forEach(app -> app.owners.add(user.userId));

        saveApplications(applications);

        List<Application> result = instance.getApplicationsOwnedBy(user.userId);
        assertThat(containTheSameElements(applications, result), is(true));
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
        assertThat(containTheSameElements(result, applications), is(false));
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
        String name = one(alphabeticStrings(length));
        String term = name.substring(length / 2);

        app.setName(name);
        instance.saveApplication(app);

        List<Application> result = instance.searchByName(term);
        assertThat(result, contains(app));
    }

    @Test
    public void testGetRecentlyCreated() throws Exception
    {
        saveApplications(applications);

        List<Application> result = instance.getRecentlyCreated();
        assertThat(result, not(empty()));
        assertThat(containTheSameElements(result, applications), is(true));
    }

    @Test
    public void testContains() throws Exception
    {
        instance.saveApplication(app);

        assertThat(instance.containsApplication(appId), is(true));

        String randomId = one(uuids);
        assertThat(instance.containsApplication(randomId), is(false));
    }

}
