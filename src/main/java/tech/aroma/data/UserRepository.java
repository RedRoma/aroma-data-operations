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


package tech.aroma.data;

import java.util.List;
import org.apache.thrift.TException;
import tech.aroma.thrift.User;
import tech.sirwellington.alchemy.annotations.arguments.Required;


/**
 * Answers questions about and performs actions to {@linkplain User Users}.
 * @author SirWellington
 */
public interface UserRepository 
{
    void saveUser(@Required User user) throws TException;
    
    User getUser(@Required String userId) throws TException;
    
    void deleteUser(@Required String userId) throws TException;
    
    boolean containsUser(@Required String userId) throws TException;
    
    User getUserByEmail(@Required String emailAddress) throws TException;
    
    User findByGithubProfile(@Required String githubProfile) throws TException;
    
    List<User> getRecentlyCreatedUsers() throws TException;
    
}
