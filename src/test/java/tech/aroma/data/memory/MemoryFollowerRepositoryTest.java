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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static tech.aroma.thrift.generators.ApplicationGenerators.applications;
import static tech.aroma.thrift.generators.UserGenerators.users;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MemoryFollowerRepositoryTest
{

    @GeneratePojo
    private Application application;

    @GenerateString(UUID)
    private String applicationId;

    private List<Application> appsFollowed;

    @GeneratePojo
    private User user;

    @GenerateString(UUID)
    private String userId;

    private List<User> followers;

    private MemoryFollowerRepository instance;

    @Before
    public void setUp()
    {
        application = one(applications());
        applicationId = application.applicationId;
        application.unsetOrganizationId();

        user.userId = userId;

        appsFollowed = listOf(applications(), 10);
        appsFollowed.forEach(Application::unsetOrganizationId);

        followers = listOf(users(), 20);

        instance = new MemoryFollowerRepository();
    }

    @Test
    public void testSaveFollowing() throws Exception
    {

        instance.saveFollowing(user, application);

        assertThat(instance.followingExists(userId, applicationId), is(true));

        List<Application> appsFollowed = instance.getApplicationsFollowedBy(userId);
        assertThat(appsFollowed, contains(application));

        List<User> applicationFollowers = instance.getApplicationFollowers(applicationId);
        assertThat(applicationFollowers, contains(user));

    }

    @Test
    public void testDeleteFollowing() throws Exception
    {
        instance.saveFollowing(user, application);
        assertThat(instance.followingExists(userId, applicationId), is(true));

        instance.deleteFollowing(userId, applicationId);
        assertThat(instance.followingExists(userId, applicationId), is(false));
    }

    @DontRepeat
    @Test
    public void testDeleteFollowingWhenNone() throws Exception
    {
        instance.deleteFollowing(userId, applicationId);
    }

    @Test
    public void testFollowingExists() throws Exception
    {
        boolean result = instance.followingExists(userId, applicationId);
        assertThat(result, is(false));

        instance.saveFollowing(user, application);
        result = instance.followingExists(userId, applicationId);
        assertThat(result, is(true));
    }

    @Test
    public void testGetApplicationsFollowedBy() throws Exception
    {
        for (Application app : this.appsFollowed)
        {
            instance.saveFollowing(user, app);
        }

        List<Application> result = instance.getApplicationsFollowedBy(userId);
        assertThat(result, notNullValue());
        assertThat(Sets.containTheSameElements(result, appsFollowed), is(true));
    }

    @Test
    public void testGetApplicationFollowers() throws Exception
    {
        for (User follower : this.followers)
        {
            instance.saveFollowing(follower, application);
        }

        List<User> result = instance.getApplicationFollowers(applicationId);
        assertThat(result, notNullValue());
        assertThat(Sets.containTheSameElements(result, followers), is(true));
    }

}
