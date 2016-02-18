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

import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.banana.data.FollowerRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;

import static tech.aroma.banana.data.assertions.RequestAssertions.validApplication;
import static tech.aroma.banana.data.assertions.RequestAssertions.validUser;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

/**
 *
 * @author SirWellington
 */
final class MemoryFollowerRepository implements FollowerRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(MemoryFollowerRepository.class);

    private final Map<String, List<Application>> userFollowings = Maps.createSynchronized();
    private final Map<String, List<User>> applicationFollowers = Maps.createSynchronized();

    @Override
    public void saveFollowing(User user, Application application) throws TException
    {
        checkThat(user)
            .throwing(InvalidArgumentException.class)
            .is(validUser());

        checkThat(application)
            .throwing(InvalidArgumentException.class)
            .is(validApplication());

        String userId = user.userId;
        String appId = application.applicationId;

        List<Application> applications = userFollowings.getOrDefault(userId, Lists.create());
        applications.add(application);
        userFollowings.put(userId, applications);

        List<User> followers = applicationFollowers.getOrDefault(appId, Lists.create());
        followers.add(user);
        applicationFollowers.put(appId, followers);
    }

    @Override
    public void deleteFollowing(String userId, String applicationId) throws TException
    {
        checkThat(userId, applicationId)
            .throwing(InvalidArgumentException.class)
            .are(nonEmptyString());

        List<Application> apps = userFollowings.getOrDefault(userId, Lists.emptyList());
        
        apps = apps.stream()
            .filter(app -> !Objects.equal(applicationId, app.applicationId))
            .collect(Collectors.toList());
        
        userFollowings.put(userId, apps);
        
        List<User> followers = applicationFollowers.getOrDefault(applicationId, Lists.emptyList());
        followers = followers.stream()
            .filter(user -> !Objects.equal(userId, user.userId))
            .collect(Collectors.toList());
        
        applicationFollowers.put(applicationId, followers);
    }

    @Override
    public boolean followingExists(String userId, String applicationId) throws TException
    {
        checkThat(userId, applicationId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("missing arguments")
            .are(nonEmptyString());
        
        return userFollowsApp(userId, applicationId) && 
               appHasFollower(applicationId, userId);
    }

    @Override
    public List<Application> getApplicationsFollowedBy(String userId) throws TException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("missing userId")
            .is(nonEmptyString());
        
        return userFollowings.getOrDefault(userId, Lists.emptyList());
    }

    @Override
    public List<User> getApplicationFollowers(String applicationId) throws TException
    {
        return applicationFollowers.getOrDefault(applicationId, Lists.emptyList());
    }

    private boolean userFollowsApp(String userId, String applicationId)
    {
        List<Application> apps = userFollowings.get(userId);
        
        if(Lists.isEmpty(apps))
        {
            return false;
        }
        
        return apps.stream()
            .anyMatch(app -> Objects.equal(applicationId, app.applicationId));
    }

    private boolean appHasFollower(String applicationId, String userId)
    {
        List<User> followers = applicationFollowers.get(applicationId);
        
        if(Lists.isEmpty(followers))
        {
            return false;
        }
        
        return followers.stream()
            .anyMatch(user -> Objects.equal(userId, user.userId));
    }

}
