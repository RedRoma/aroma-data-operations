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
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.List;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.aroma.banana.thrift.Organization;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
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
public class CassandraOrganizationRepositoryTest
{

    @Mock(answer = RETURNS_MOCKS)
    private Cluster cluster;

    @Mock
    private Session cassandra;

    private QueryBuilder queryBuilder;

    @Mock
    private Function<Row, Organization> organizationMapper;

    @Mock
    private Function<Row, User> userMapper;

    @Mock
    private Row row;

    @Mock
    private ResultSet results;

    @Captor
    private ArgumentCaptor<Statement> statementCaptor;

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
    
    @GenerateString(ALPHABETIC)
    private String badId;

    @Before
    public void setUp()
    {
        org.organizationId = orgId;
        user.userId = userId;

        members = members.stream()
            .map(u -> u.setUserId(one(uuids)))
            .collect(toList());

        org.owners = listOf(uuids, 3);

        queryBuilder = new QueryBuilder(cluster);
        instance = new CassandraOrganizationRepository(cassandra, queryBuilder, organizationMapper, userMapper);
        
        setupBasicStubbing();
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new CassandraOrganizationRepository(null, queryBuilder, organizationMapper, userMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraOrganizationRepository(cassandra, null, organizationMapper, userMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraOrganizationRepository(cassandra, queryBuilder, null, userMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraOrganizationRepository(cassandra, queryBuilder, organizationMapper, null))
            .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void testSaveOrganization() throws Exception
    {
        instance.saveOrganization(org);

        verify(cassandra).execute(statementCaptor.capture());
        Statement statementMade = statementCaptor.getValue();
        assertThat(statementMade, notNullValue());
    }

    @Test
    public void testSaveOrganizationWhenFails() throws Exception
    {
        setupForFailure();

        assertThrows(() -> instance.saveOrganization(org))
            .isInstanceOf(TException.class);
    }

    @DontRepeat
    @Test
    public void testSaveOrganizationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveOrganization(null))
            .isInstanceOf(InvalidArgumentException.class);

        Organization emptyOrg = new Organization();

        assertThrows(() -> instance.saveOrganization(emptyOrg))
            .isInstanceOf(InvalidArgumentException.class);

        Organization orgWithoutName = new Organization(org);
        orgWithoutName.unsetOrganizationName();

        assertThrows(() -> instance.saveOrganization(orgWithoutName))
            .isInstanceOf(InvalidArgumentException.class);

    }

    @Test
    public void testGetOrganization() throws Exception
    {
        Organization result = instance.getOrganization(orgId);
        
        assertThat(result, is(org));
    }

    @Test
    public void testGetOrganizationWhenFails() throws Exception
    {
        setupForFailure();
        
        assertThrows(() -> instance.getOrganization(orgId))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testGetOrganizationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getOrganization(""))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.getOrganization(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteOrganization() throws Exception
    {
    }

    @Test
    public void testOrganizationExists() throws Exception
    {
    }

    @Test
    public void testGetOrganizationMemebers() throws Exception
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

    private void setupForFailure()
    {
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenThrow(new IllegalArgumentException());
    }

    private void setupBasicStubbing()
    {
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenReturn(results);
        
        when(results.one())
            .thenReturn(row);
        
        when(organizationMapper.apply(row))
            .thenReturn(org);
        
        when(userMapper.apply(row))
            .thenReturn(user);
    }
}
