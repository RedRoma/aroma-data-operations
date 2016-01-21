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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.List;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.banana.thrift.Organization;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.OrganizationDoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraOrganizationRepositoryIT
{
    
    private static Cluster cluster;
    private static Session session;
    private static QueryBuilder queryBuilder;
    
    @BeforeClass
    public static void begin()
    {
        cluster = TestSessions.createTestCluster();
        session = TestSessions.createTestSession(cluster);
        queryBuilder = TestSessions.createQueryBuilder(cluster);
    }
    
    @AfterClass
    public static void end()
    {
        session.close();
        cluster.close();
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
    
    @Before
    public void setUp()
    {
        user.userId = userId;
        org.organizationId = orgId;
        
        org.owners = listOf(uuids, 5);
        
        instance = new CassandraOrganizationRepository(session, queryBuilder, organizationMapper, userMapper);
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
    }
    
    @Test
    public void testSaveOrganization() throws Exception
    {
        instance.saveOrganization(org);
        
        assertThat(instance.containsOrganization(orgId), is(true));
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
    }
    
    @Test
    public void testGetOrganizationMembers() throws Exception
    {
    }
    
    @Test
    public void testSearchByName() throws Exception
    {
    }
    
    @Test
    public void testSaveMemberInOrganization() throws Exception
    {
    }
    
    @Test
    public void testGetOrganizationOwners() throws Exception
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
    
}
