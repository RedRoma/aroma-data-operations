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
import java.util.Map;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;

import static tech.aroma.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.data.assertions.RequestAssertions.validUser;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.keyInMap;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 *
 * @author SirWellington
 */
final class MemoryUserRepository implements UserRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(MemoryUserRepository.class);

    private final Map<String, User> users = Maps.createSynchronized();
    private final Map<String, String> usersByEmail = Maps.createSynchronized();
    private final Map<String, String> usersByGithubProfile = Maps.createSynchronized();

    @Override
    public void saveUser(User user) throws TException
    {
        checkThat(user)
            .throwing(InvalidArgumentException.class)
            .is(validUser());

        String userId = user.userId;
        String email = user.email;
        String githubProfile = user.githubProfile;
        
        users.put(userId, user);
        
        if (!isNullOrEmpty(email))
        {
            usersByEmail.put(email, userId);
        }
        
        if (!isNullOrEmpty(githubProfile))
        {
            usersByGithubProfile.put(githubProfile, userId);
        }
    }

    @Override
    public User getUser(String userId) throws TException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString())
            .throwing(UserDoesNotExistException.class)
            .usingMessage(userId)
            .is(keyInMap(users));
        
        return users.get(userId);
    }

    @Override
    public void deleteUser(String userId) throws TException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString())
            .throwing(UserDoesNotExistException.class)
            .is(keyInMap(users));
        
        User removedUser = users.remove(userId);
        
        String email = removedUser.email;
        if (!isNullOrEmpty(email))
        {
            this.usersByEmail.remove(email);
        }
        
        String githubProfile = removedUser.githubProfile;
        if(!isNullOrEmpty(githubProfile))
        {
            this.usersByGithubProfile.remove(githubProfile);
        }
    }

    @Override
    public boolean containsUser(String userId) throws TException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        return users.containsKey(userId);
    }

    @Override
    public User getUserByEmail(String emailAddress) throws TException
    {
        checkThat(emailAddress)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString())
            .throwing(UserDoesNotExistException.class)
            .usingMessage("Could not find email: " + emailAddress)
            .is(keyInMap(usersByEmail));
        
        String userId = usersByEmail.get(emailAddress);
        return users.get(userId);
    }

    @Override
    public User findByGithubProfile(String githubProfile) throws TException
    {
        checkThat(githubProfile)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString())
            .throwing(UserDoesNotExistException.class)
            .is(keyInMap(usersByGithubProfile));
        
        String userId = usersByGithubProfile.get(githubProfile);
        return users.get(userId);
    }

    @Override
    public List<User> getRecentlyCreatedUsers() throws TException
    {
        return Lists.copy(users.values());
    }

}
