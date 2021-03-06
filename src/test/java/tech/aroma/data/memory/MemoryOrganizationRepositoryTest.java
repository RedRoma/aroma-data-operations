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
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.Organization;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OrganizationDoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class MemoryOrganizationRepositoryTest 
{
    
    @GeneratePojo
    private Organization org;
    
    @GenerateString(UUID)
    private String orgId;
    
    
    @GeneratePojo
    private User user;
    
    @GenerateString(UUID)
    private String userId;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    private MemoryOrganizationRepository instance;

    @Before
    public void setUp()
    {
        instance = new MemoryOrganizationRepository();
        
        setupData();
    }

    @Test
    public void testSaveOrganization() throws Exception
    {
        instance.saveOrganization(org);
        
        assertThat(instance.containsOrganization(orgId), is(true));
    }
    
    @DontRepeat
    @Test
    public void testSaveOrgWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveOrganization(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.saveOrganization(new Organization()))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetOrganization() throws Exception
    {
        instance.saveOrganization(org);
        
        Organization result = instance.getOrganization(orgId);
        assertThat(result, is(org));
    }
    
    @Test
    public void testGetOrganizationWhenNotExists() throws Exception
    {
        assertThrows(() -> instance.getOrganization(orgId))
            .isInstanceOf(OrganizationDoesNotExistException.class);
    }

    @Test
    public void testDeleteOrganization() throws Exception
    {
        instance.saveOrganization(org);
        instance.deleteOrganization(orgId);
        assertThat(instance.containsOrganization(orgId), is(false));
    }

    @DontRepeat
    @Test
    public void testDeleteOrganizationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteOrganization(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testContainsOrganization() throws Exception
    {
        assertThat(instance.containsOrganization(orgId), is(false));
        
        instance.saveOrganization(org);
        
        assertThat(instance.containsOrganization(orgId), is(true));
    }

    @DontRepeat
    @Test
    public void testContainsOrganizationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.containsOrganization(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.containsOrganization(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testSearchByName() throws Exception
    {
        List<Organization> orgs = listOf(pojos(Organization.class))
            .stream()
            .map(org -> org.setOrganizationId(one(uuids)))
            .map(org -> org.setOwners(Lists.emptyList()))
            .collect(toList());
        
        Set<String> orgNames = orgs.stream()
            .map(Organization::getOrganizationName)
            .collect(toSet());
        
        String oneName = Sets.oneOf(orgNames);
        
        for(Organization organization : orgs)
        {
            instance.saveOrganization(organization);
        }
        
        List<Organization> result = instance.searchByName(oneName);
        assertThat(result, not(empty()));
        
        for(Organization organization : result)
        {
            assertThat(organization, isIn(orgs));
        }
    }

    @Test
    public void testGetOrganizationOwners() throws Exception
    {
        instance.saveOrganization(org);
        
        List<User> owners = instance.getOrganizationOwners(orgId);
        
        for(User owner : owners)
        {
            assertThat(owner.userId, isIn(org.owners));
        }
    }

    @Test
    public void testSaveMemberInOrganization() throws Exception
    {
        instance.saveOrganization(org);
        instance.saveMemberInOrganization(orgId, user);
        
        boolean memberInOrganization = instance.isMemberInOrganization(orgId, userId);
        assertThat(memberInOrganization, is(true));
    }

    @Test
    public void testIsMemberInOrganization() throws Exception
    {
        boolean memberInOrganization = instance.isMemberInOrganization(orgId, userId);
        assertThat(memberInOrganization, is(false));
        
        instance.saveOrganization(org);
        instance.saveMemberInOrganization(orgId, user);
        
        memberInOrganization = instance.isMemberInOrganization(orgId, userId);
        assertThat(memberInOrganization, is(true));
    }

    @Test
    public void testGetOrganizationMembers() throws Exception
    {
        instance.saveOrganization(org);
        
        List<User> members = instance.getOrganizationMembers(orgId);
        assertThat(members, is(empty()));
        
        instance.saveMemberInOrganization(orgId, user);
        
        members = instance.getOrganizationMembers(orgId);
        assertThat(members, not(empty()));
        assertThat(members, contains(user));
    }

    @Test
    public void testDeleteMember() throws Exception
    {
        instance.saveOrganization(org);
        instance.saveMemberInOrganization(orgId, user);
        
        instance.deleteMember(orgId, userId);
        assertThat(instance.isMemberInOrganization(orgId, userId), is(false));
    }

    @Test
    public void testDeleteAllMembers() throws Exception
    {
        instance.saveOrganization(org);
        instance.saveMemberInOrganization(orgId, user);
        
        instance.deleteAllMembers(orgId);
        
        List<User> members = instance.getOrganizationMembers(orgId);
        assertThat(members, is(empty()));
    }

    private void setupData()
    {
        org.organizationId = orgId;
        org.owners = listOf(uuids);
        
        user.userId = userId;
    }

}
