 /*
  * Copyright 2016 RedRoma, Inc.
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
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;

import static sir.wellington.alchemy.collections.lists.Lists.isEmpty;
import static tech.aroma.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.data.assertions.RequestAssertions.validApplication;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.keyInMap;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
final class MemoryApplicationRepository implements ApplicationRepository
{
    
    private final static Logger LOG = LoggerFactory.getLogger(MemoryApplicationRepository.class);
    
    private final Map<String, Application> mainTable = Maps.create();
    private final Map<String, Set<Application>> applicationsByOrg = Maps.create();
    private final ExpiringMap<String, Application> recents = ExpiringMap.builder()
        .expiration(10, TimeUnit.MINUTES)
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .build();
    
    MemoryApplicationRepository()
    {
        
    }
    
    
    @Override
    public void saveApplication(Application application) throws TException
    {
        
        checkThat(application)
            .throwing(InvalidArgumentException.class)
            .is(validApplication());
        
        String applicationId = application.applicationId;
        
        mainTable.put(applicationId, application);
        recents.put(applicationId, application);
        
        if (!isNullOrEmpty(application.organizationId))
        {
            Set<Application> applications = applicationsByOrg.getOrDefault(applicationId, Sets.create());
            applications.add(application);
            applicationsByOrg.put(applicationId, applications);
        }
    }
    
    @Override
    public boolean containsApplication(String applicationId) throws TException
    {
        return mainTable.containsKey(applicationId);
    }
    
    @Override
    public void deleteApplication(String applicationId) throws TException
    {
        checkIdExists(applicationId);
        
        mainTable.remove(applicationId);
        applicationsByOrg.remove(applicationId);
        recents.remove(applicationId);
    }
    
    
    @Override
    public Application getById(String applicationId) throws TException
    {
        checkIdExists(applicationId);
        
        return mainTable.get(applicationId);
    }
    
    @Override
    public List<Application> getApplicationsOwnedBy(String userId) throws TException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        return mainTable.values().stream()
            .filter(app -> app.isSetOwners())
            .filter(this.containsOwner(userId))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Application> getApplicationsByOrg(String orgId) throws TException
    {
        checkThat(orgId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        Set<Application> applications = applicationsByOrg.getOrDefault(orgId, Sets.emptySet());
        return Lists.toList(applications);
    }
    
    @Override
    public List<Application> searchByName(String searchTerm) throws TException
    {
        return mainTable.values().stream()
            .filter(containsInName(searchTerm))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Application> getRecentlyCreated() throws TException
    {
        return Lists.toList(recents.values());
    }
    
    private void checkIdExists(String applicationId) throws TException
    {
        checkThat(applicationId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString())
            .throwing(ApplicationDoesNotExistException.class)
            .usingMessage("application does not exist")
            .is(keyInMap(mainTable));
    }
    
    private Predicate<Application> containsOwner(String userId)
    {
        return app ->
        {
            if (isEmpty(app.owners))
            {
                return false;
            }
            
            return app.owners.stream()
                .filter(user -> !isNullOrEmpty(user))
                .filter(user -> user.equals(userId))
                .count() > 0;
        };
    }
    
    private Predicate<? super Application> containsInName(String searchTerm)
    {
        return app ->
        {
            if (isNullOrEmpty(app.name))
            {
                return false;
            }
            
            return app.name.contains(searchTerm);
        };
    }
    
}
