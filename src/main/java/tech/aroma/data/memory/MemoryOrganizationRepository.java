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


import java.util.*;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.maps.Maps;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.OrganizationRepository;
import tech.aroma.thrift.Organization;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OrganizationDoesNotExistException;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static tech.aroma.data.assertions.RequestAssertions.*;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.keyInMap;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 *
 * @author SirWellington
 */
@Internal
final class MemoryOrganizationRepository implements OrganizationRepository
{
    private final static Logger LOG = LoggerFactory.getLogger(MemoryOrganizationRepository.class);
    
    private final Map<String, Organization> organizations = Maps.createSynchronized();
    private final Map<String, Set<User>> members = Maps.createSynchronized();

    @Override
    public void saveOrganization(Organization organization) throws TException
    {
        checkThat(organization)
            .throwing(InvalidArgumentException.class)
            .is(validOrganization());
        
        String orgId = organization.organizationId;
        organizations.put(orgId, organization);
    }

    @Override
    public Organization getOrganization(String organizationId) throws TException
    {
        checkThat(organizationId)
            .throwing(InvalidArgumentException.class)
            .is(validOrgId())
            .throwing(OrganizationDoesNotExistException.class)
            .is(keyInMap(organizations));
        
        return organizations.get(organizationId);
    }

    @Override
    public void deleteOrganization(String organizationId) throws TException
    {
        checkOrgId(organizationId);
        
        this.organizations.remove(organizationId);
    }

    @Override
    public boolean containsOrganization(String organizationId) throws TException
    {
        checkOrgId(organizationId);

        return organizations.containsKey(organizationId);
    }

    @Override
    public List<Organization> searchByName(String searchTerm) throws TException
    {
        checkThat(searchTerm)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString());
        
        return organizations.values()
            .stream()
            .filter(org -> org.organizationName.contains(searchTerm))
            .collect(toList());
        
    }

    @Override
    public List<User> getOrganizationOwners(String organizationId) throws TException
    {
        checkOrgId(organizationId);
        
        Organization org = getOrganization(organizationId);
        
        return Sets.copyOf(org.owners)
            .stream()
            .map(id -> new User().setUserId(id))
            .collect(toList());
    }

    @Override
    public void saveMemberInOrganization(String organizationId, User user) throws TException
    {
        checkThat(organizationId)
            .throwing(InvalidArgumentException.class)
            .is(validOrgId())
            .throwing(OrganizationDoesNotExistException.class)
            .is(keyInMap(organizations));
        
        checkThat(user)
            .throwing(InvalidArgumentException.class)
            .is(validUser());
        
        Set<User> result = members.getOrDefault(organizationId, Sets.create());
        result.add(user);
        members.put(organizationId, result);
    }

    @Override
    public boolean isMemberInOrganization(String organizationId, String userId) throws TException
    {
        checkOrgId(organizationId);
        
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(validUserId());
        
        return members.getOrDefault(organizationId, Sets.emptySet())
            .stream()
            .map(User::getUserId)
            .anyMatch(id -> Objects.equals(id, userId));
    }

    @Override
    public List<User> getOrganizationMembers(String organizationId) throws TException
    {
        checkOrgId(organizationId);
        
        return members.getOrDefault(organizationId, Sets.emptySet())
            .stream()
            .collect(toList());
    }

    @Override
    public void deleteMember(String organizationId, String userId) throws TException
    {
        checkOrgId(organizationId);
        
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(validUserId());
        
        Set<User> users = members.getOrDefault(organizationId, Sets.create());
        users = users.parallelStream()
            .filter(user -> !Objects.equals(user.userId, userId))
            .collect(toSet());
        
        members.put(organizationId, users);
    }

    @Override
    public void deleteAllMembers(String organizationId) throws TException
    {
        checkOrgId(organizationId);
        
        members.remove(organizationId);
    }

    private void checkOrgId(String organizationId) throws InvalidArgumentException
    {
        checkThat(organizationId)
            .throwing(InvalidArgumentException.class)
            .is(validOrgId());
    }

}
