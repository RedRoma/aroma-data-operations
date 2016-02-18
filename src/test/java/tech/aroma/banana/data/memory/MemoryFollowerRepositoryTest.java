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
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.User;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 *
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
    
    @GenerateList(Application.class)
    private List<Application> appsFollowed;
    
    @GeneratePojo
    private User user;
    
    @GenerateString(UUID)
    private String userId;
    
    @GenerateList(User.class)
    private List<User> followers;
    
    private MemoryFollowerRepository instance;
    
    @Before
    public void setUp()
    {
        application.applicationId = applicationId;
        application.unsetOrganizationId();
        
        user.userId = userId;
        
        appsFollowed.forEach((Application app) -> app.setApplicationId(one(uuids)));
        appsFollowed.forEach(app -> app.unsetOrganizationId());
        
        followers.forEach((User user) -> user.setUserId(one(uuids)));
        
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
        for(Application app : this.appsFollowed)
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
        for(User follower : this.followers)
        {
            instance.saveFollowing(follower, application);
        }
        
        List<User> result = instance.getApplicationFollowers(applicationId);
        assertThat(result, notNullValue());
        assertThat(Sets.containTheSameElements(result, followers), is(true));
    }

}