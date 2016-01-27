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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.thrift.Organization;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.OrganizationDoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
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
    }

    @Test
    public void testGetOrganizationOwners() throws Exception
    {
    }

    @Test
    public void testSaveMemberInOrganization() throws Exception
    {
    }

    @Test
    public void testIsMemberInOrganization() throws Exception
    {
    }

    @Test
    public void testGetOrganizationMembers() throws Exception
    {
    }

    @Test
    public void testDeleteMember() throws Exception
    {
    }

    @Test
    public void testDeleteAllMembers() throws Exception
    {
    }

    private void setupData()
    {
        org.organizationId = orgId;
        org.owners = listOf(uuids);
        
        user.userId = userId;
    }

}