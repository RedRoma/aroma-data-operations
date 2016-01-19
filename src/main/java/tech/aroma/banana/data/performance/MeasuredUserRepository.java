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

 
package tech.aroma.banana.data.performance;


import decorice.DecoratedBy;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.data.performance.Operation.VoidOperation;
import tech.aroma.banana.thrift.User;
import tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern.Role.DECORATOR;

/**
 *
 * @author SirWellington
 */
@DecoratorPattern(role = DECORATOR)
final class MeasuredUserRepository implements UserRepository
{
    private final static Logger LOG = LoggerFactory.getLogger(MeasuredUserRepository.class);

    private final UserRepository delegate;

    @Inject
    MeasuredUserRepository(@DecoratedBy(MeasuredUserRepository.class) UserRepository delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void saveUser(User user) throws TException
    {
        VoidOperation operation = () -> delegate.saveUser(user);
        
        Operations.logLatency(operation, "saveUser");
    }

    @Override
    public User getUser(String userId) throws TException
    {
        Operation<User> operation = () -> delegate.getUser(userId);
        
        return Operations.logLatency(operation, "getUser");
    }

    @Override
    public void deleteUser(String userId) throws TException
    {
        VoidOperation operation = () -> delegate.deleteUser(userId);
        
        Operations.logLatency(operation, "deleteUser");
    }

    @Override
    public boolean containsUser(String userId) throws TException
    {
        Operation<Boolean> operation = () -> delegate.containsUser(userId);
        return Operations.logLatency(operation, "containsUser");
    }

    @Override
    public User getUserByEmail(String emailAddress) throws TException
    {
        Operation<User> operation = () -> delegate.getUserByEmail(emailAddress);
        return Operations.logLatency(operation, "getUserByEmail");
    }

    @Override
    public User findByGithubProfile(String githubProfile) throws TException
    {
        Operation<User> operation = () -> delegate.findByGithubProfile(githubProfile);
        return Operations.logLatency(operation, "findByGithubProfile");
    }
    
    
    
}
