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
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.Organization;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.OrganizationDoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.positiveLongs;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
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
        assertThat(statementMade, instanceOf(Insert.class));
    }

    @DontRepeat
    @Test
    public void testSaveOrganizationWhenFails() throws Exception
    {
        setupForFailure();

        assertThrows(() -> instance.saveOrganization(org))
            .isInstanceOf(TException.class);
    }

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

        verify(cassandra).execute(statementCaptor.capture());

        Statement statement = statementCaptor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, instanceOf(Select.Where.class));
    }

    @DontRepeat
    @Test
    public void testGetOrganizationWhenNotExists() throws Exception
    {
        when(results.one()).thenReturn(null);

        assertThrows(() -> instance.getOrganization(orgId))
            .isInstanceOf(OrganizationDoesNotExistException.class);
    }

    @DontRepeat
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
        instance.deleteOrganization(orgId);

        verify(cassandra, atLeastOnce()).execute(statementCaptor.capture());
        Statement statementMade = statementCaptor.getValue();
        assertThat(statementMade, notNullValue());
        assertThat(statementMade, instanceOf(Delete.Where.class));
    }

    @Test
    public void testDeleteOrganizationWhenNotExists() throws Exception
    {
        when(results.one()).thenReturn(null);

        instance.deleteOrganization(orgId);
    }

    @Test
    public void testDeleteOrganizationWhenFails() throws Exception
    {
        setupForFailure();

        assertThrows(() -> instance.deleteOrganization(orgId))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testDeleteOrganizationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteOrganization(""))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.deleteOrganization(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testContainsOrganization() throws Exception
    {
        when(row.getLong(0)).thenReturn(0L);

        boolean result = instance.containsOrganization(orgId);
        assertThat(result, is(false));

        long count = one(positiveLongs());
        when(row.getLong(0)).thenReturn(count);

        result = instance.containsOrganization(orgId);

        assertThat(result, is(true));
    }

    @DontRepeat
    @Test
    public void testOrganizationExistsWhenFails() throws Exception
    {
        setupForFailure();

        assertThrows(() -> instance.containsOrganization(orgId))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testOrganizationExistsWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.containsOrganization(""))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.containsOrganization(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetOrganizationMembers() throws Exception
    {
        Map<String, Row> rows = Maps.create();

        for (User member : members)
        {
            Row mockRow = mock(Row.class);

            when(userMapper.apply(mockRow))
                .thenReturn(member);

            rows.put(member.userId, mockRow);
        }

        when(results.iterator())
            .thenReturn(rows.values().iterator());

        List<User> response = instance.getOrganizationMembers(orgId);

        assertThat(Sets.toSet(response), is(Sets.toSet(members)));
    }

    @Test
    public void testGetOrganizationMembersWhenOrgNotExists() throws Exception
    {
        when(results.iterator())
            .thenReturn(Lists.<Row>emptyList().iterator());

        List<User> response = instance.getOrganizationMembers(orgId);
        assertThat(response, notNullValue());
        assertThat(response, is(empty()));
    }

    @DontRepeat
    @Test
    public void testGetOrganizationMembersWhenFails() throws Exception
    {
        setupForFailure();

        assertThrows(() -> instance.getOrganizationMembers(orgId))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testGetOrganizationMembersWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getOrganizationMembers(""))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getOrganizationMembers(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testSearchByName() throws Exception
    {
        assertThrows(() -> instance.searchByName(org.organizationName))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testSaveMemberInOrganization() throws Exception
    {
        instance.saveMemberInOrganization(orgId, user);

        verify(cassandra).execute(statementCaptor.capture());

        Statement statement = statementCaptor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, instanceOf(Insert.class));
    }

    @DontRepeat
    @Test
    public void testSaveMemberInOrganizationWhenFails() throws Exception
    {
        setupForFailure();

        assertThrows(() -> instance.saveMemberInOrganization(orgId, user))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testGetOrganizationOwners() throws Exception
    {
        List<User> response = instance.getOrganizationOwners(orgId);

        Set<User> expected = org.owners
            .stream()
            .map(id -> new User().setUserId(id))
            .collect(toSet());

        assertThat(Sets.toSet(response), is(expected));
    }

    @Test
    public void testGetOrganizationOwnersWhenOrgNotExists() throws Exception
    {
        when(results.one())
            .thenReturn(null);

        assertThrows(() -> instance.getOrganizationOwners(orgId))
            .isInstanceOf(OrganizationDoesNotExistException.class);
    }

    @DontRepeat
    @Test
    public void testGetOrganizationOwnersWhenFails() throws Exception
    {
        setupForFailure();

        assertThrows(() -> instance.getOrganizationOwners(orgId))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testGetOrganizationOwnersWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getOrganizationOwners(""))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getOrganizationOwners(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteMember() throws Exception
    {
        instance.deleteMember(orgId, userId);

        verify(cassandra).execute(statementCaptor.capture());

        Statement statement = statementCaptor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, instanceOf(Delete.Where.class));
    }

    @DontRepeat
    @Test
    public void testDeleteMemberWhenFails() throws Exception
    {
        setupForFailure();

        assertThrows(() -> instance.deleteMember(orgId, userId))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testDeleteMemberWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteMember("", userId))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.deleteMember(orgId, ""))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.deleteMember(badId, userId))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.deleteMember(orgId, badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteAllMembers() throws Exception
    {
        instance.deleteAllMembers(orgId);

        verify(cassandra).execute(statementCaptor.capture());
        
        Statement statement = statementCaptor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, instanceOf(Delete.Where.class));
    }

    @DontRepeat
    @Test
    public void testDeleteAllMembersWhenFails() throws Exception
    {
        setupForFailure();
        assertThrows(() -> instance.deleteAllMembers(orgId))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testDeleteAllMembersWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteAllMembers(""))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.deleteAllMembers(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    private void setupForFailure()
    {
        List<Class<? extends RuntimeException>> possibleExceptions = Lists.createFrom(IllegalArgumentException.class,
                                                                                      RuntimeException.class,
                                                                                      NoHostAvailableException.class);

        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenThrow(Lists.oneOf(possibleExceptions));
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

    @Test
    public void testIsMemberInOrganization() throws Exception
    {
        when(row.getLong(0)).thenReturn(0L);
        
        boolean result = instance.isMemberInOrganization(orgId, userId);
        assertThat(result, is(false));
        
        long count = one(positiveLongs());
        when(row.getLong(0)).thenReturn(count);
        
        result = instance.isMemberInOrganization(orgId, userId);
        assertThat(result, is(true));
    }
}
