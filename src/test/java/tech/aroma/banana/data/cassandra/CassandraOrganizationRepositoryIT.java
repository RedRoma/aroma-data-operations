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

package tech.aroma.banana.data.cassandra;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Objects;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.maps.Maps;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.Organization;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.OrganizationDoesNotExistException;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.annotations.testing.TimeSensitive;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraOrganizationRepositoryIT
{
    
    private static Session session;
    private static QueryBuilder queryBuilder;
    
    @BeforeClass
    public static void begin()
    {
        session = TestCassandraProviders.getTestSession();
        queryBuilder = TestCassandraProviders.getQueryBuilder();
    }
    
    
    private final Function<Row, Organization> organizationMapper = Mappers.orgMapper();
    
    private final Function<Row, User> userMapper = Mappers.userMapper();
    
    private CassandraOrganizationRepository instance;
    
    
    @GeneratePojo
    private Organization org;
    
    @GenerateString(UUID)
    private String orgId;
    
    @GenerateString(UUID)
    private String userId;
    
    @GeneratePojo
    private User user;
    
    @GenerateList(User.class)
    private List<User> members;
    
    //This flag saves some time on the Integration Test.
    private boolean deleteMembersAtEnd = false;
    
    private Map<String,User> mapOfMembers;
    
    @Before
    public void setUp()
    {
        user.userId = userId;
        org.organizationId = orgId;
        
        org.owners = listOf(uuids, 5);
        
        instance = new CassandraOrganizationRepository(session, queryBuilder, organizationMapper, userMapper);
        
        mapOfMembers = Maps.create();
        members = members
            .stream()
            .map(m -> m.setUserId(one(uuids)))
            .collect(toList());
        
        members.forEach(m -> mapOfMembers.put(m.userId, m));
        
        deleteMembersAtEnd = false;
    }
    
    @After
    public void cleanUp() throws Exception
    {
        try
        {
            instance.deleteOrganization(orgId);
        }
        catch (TException ex)
        {
            System.out.println("Failed to delete Organization: " + orgId);
        }
        
        if (deleteMembersAtEnd)
        {
            deleteMembers(members);
        }
    }
    
    @Test
    public void testSaveOrganization() throws Exception
    {
        instance.saveOrganization(org);
        
        assertThat(instance.containsOrganization(orgId), is(true));
    }
    
    @DontRepeat
    @TimeSensitive
    @Test
    public void testSaveOrganizationTwice() throws Exception
    {
        instance.saveOrganization(org);
        Thread.sleep(5);
        instance.saveOrganization(org);
        
        Organization result = instance.getOrganization(orgId);
        assertMostlyMatch(result, org);
    }
    
    @Test
    public void testGetOrganization() throws Exception
    {
        assertThrows(() -> instance.getOrganization(orgId))
            .isInstanceOf(OrganizationDoesNotExistException.class);
        
        instance.saveOrganization(org);
        
        Organization result = instance.getOrganization(orgId);
        assertMostlyMatch(result, org);
    }
    
    @Test
    public void testDeleteOrganization() throws Exception
    {
        //Should be ok
        instance.deleteOrganization(orgId);

        instance.saveOrganization(org);
        assertThat(instance.containsOrganization(orgId), is(true));

        instance.deleteOrganization(orgId);
        assertThat(instance.containsOrganization(orgId), is(false));
    }
    
    @Test
    public void testContainsOrganization() throws Exception
    {
        boolean result = instance.containsOrganization(orgId);
        assertThat(result, is(false));
        
        instance.saveOrganization(org);
        result = instance.containsOrganization(orgId);
        assertThat(result, is(true));
    }
    
    @Test
    public void testGetOrganizationMembers() throws Exception
    {
        saveMembers(members);
        deleteMembersAtEnd = true;
        
        List<User> response = instance.getOrganizationMembers(orgId);
        
        for(User member : response)
        {
            User expected = mapOfMembers.get(member.userId);
            assertThat(expected, notNullValue());
            assertUserMostlyMatch(member, expected);
        }
    }
    
    @DontRepeat
    @Test
    public void testSearchByName() throws Exception
    {
        assertThrows(() -> instance.searchByName(userId))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @Test
    public void testSaveMemberInOrganization() throws Exception
    {
        instance.saveMemberInOrganization(orgId, user);
        
        List<User> members = instance.getOrganizationMembers(orgId);
        
        User match = members
            .stream()
            .filter(u -> Objects.equal(u.userId, user.userId))
            .findFirst()
            .get();
        
        assertUserMostlyMatch(match, user);
    }
    
    @Test
    public void testIsMemberInOrganization() throws Exception
    {
        boolean result = instance.isMemberInOrganization(orgId, userId);
        assertThat(result, is(false));
        
        instance.saveMemberInOrganization(orgId, user);
        
        result = instance.isMemberInOrganization(orgId, userId);
        assertThat(result, is(true));
    }
    
    @Test
    public void testGetOrganizationOwners() throws Exception
    {
        instance.saveOrganization(org);
        
        List<User> owners = instance.getOrganizationOwners(orgId);
        
        Set<String> ownerIds = owners.stream()
            .map(User::getUserId)
            .collect(toSet());
        
        Set<String> expected = Sets.toSet(org.owners);
        
        assertThat(ownerIds, is(expected));
    }
    
    @Test
    public void testDeleteMember() throws Exception
    {
        instance.saveMemberInOrganization(orgId, user);
        assertThat(instance.isMemberInOrganization(orgId, userId), is(true));

        instance.deleteMember(orgId, userId);
        assertThat(instance.isMemberInOrganization(orgId, userId), is(false));
    }
    
    @Test
    public void testDeleteAllMembers() throws Exception
    {
        saveMembers(members);
        
        instance.deleteAllMembers(orgId);
        
        for(User member : members)
        {
            assertThat(instance.isMemberInOrganization(orgId, member.userId), is(false));
        }
    }
    
    private void saveMembers(List<User> members) throws TException
    {
        for (User member : members)
        {
            instance.saveMemberInOrganization(orgId, member);
        }
    }
    
    private void deleteMembers(List<User> members)
    {
        for (User member : members)
        {
            try
            {
                instance.deleteMember(orgId, member.userId);
            }
            catch (Exception ex)
            {
                System.out.println("Failed to delete member: " + member.userId);
            }
        }
    }
    
    private void assertMostlyMatch(Organization result, Organization expected)
    {
        assertThat(result, notNullValue());
        
        assertThat(result.organizationId, is(expected.organizationId));
        assertThat(result.organizationName, is(expected.organizationName));
        assertThat(result.organizationDescription, is(expected.organizationDescription));
        assertThat(result.organizationEmail, is(expected.organizationEmail));
        assertThat(result.stockMarketSymbol, is(expected.stockMarketSymbol));
        assertThat(result.website, is(expected.website));
        assertThat(result.githubProfile, is(expected.githubProfile));
        assertThat(result.logoLink, is(expected.logoLink));
        assertThat(result.industry, is(expected.industry));
        assertThat(result.tier, is(expected.tier));
        assertThat(Sets.toSet(result.owners), is(Sets.toSet(expected.owners)));
        assertThat(result.industry, is(expected.industry));
    }

    private void assertUserMostlyMatch(User result, User expected)
    {
        assertThat(result, notNullValue());
        
        assertThat(result.userId, is(expected.userId));
        assertThat(result.firstName, is(expected.firstName));
        assertThat(result.lastName, is(expected.lastName));
        assertThat(result.email, is(expected.email));
        assertThat(Sets.toSet(result.roles), is(Sets.toSet(expected.roles)));

    }

    
}
