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
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.UUID.fromString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sir.wellington.alchemy.collections.sets.Sets.toSet;
import static tech.aroma.banana.data.cassandra.Tables.ApplicationsTable.APP_DESCRIPTION;
import static tech.aroma.banana.data.cassandra.Tables.ApplicationsTable.APP_ID;
import static tech.aroma.banana.data.cassandra.Tables.ApplicationsTable.APP_NAME;
import static tech.aroma.banana.data.cassandra.Tables.ApplicationsTable.ORG_ID;
import static tech.aroma.banana.data.cassandra.Tables.ApplicationsTable.TIME_PROVISIONED;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.longs;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraApplicationRepositoryTest 
{

    @Mock(answer =RETURNS_MOCKS)
    private Cluster cluster;
    
    @Mock(answer = RETURNS_MOCKS)
    private Session session;
    
    @Captor
    private ArgumentCaptor<Statement> statementCaptor;
    
    private QueryBuilder queryBuilder;
    
    private CassandraApplicationRepository instance;
    
    @GeneratePojo
    private Application app;
    
    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(UUID)
    private String orgId;
    
    private Row mockRow;
    
    @Before
    public void setUp()
    {
        app.applicationId = appId;
        app.organizationId = orgId;
        mockRow = mockRowFor(app);
        
        List<String> owners = listOf(uuids, 5);
        app.setOwners(toSet(owners));
        
        queryBuilder = new QueryBuilder(cluster);
        
        instance = new CassandraApplicationRepository(session, queryBuilder);
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new CassandraApplicationRepository(session, null))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new CassandraApplicationRepository(null, queryBuilder))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSaveApplication() throws Exception
    {
        instance.saveApplication(app);
        verify(session).execute(statementCaptor.capture());
        
        Statement statementUsed = statementCaptor.getValue();
        assertThat(statementUsed, notNullValue());
    }
    
    @Ignore
    @Test
    public void testDeleteApplication() throws Exception
    {
        instance.saveApplication(app);
        
        when(session.execute(Mockito.any(Statement.class)));
        
        instance.deleteApplication(appId);
    }
    
    @DontRepeat
    @Test
    public void testDeleteApplicationWithBadArgs() throws Exception
    {
        String empty = "";
        assertThrows(() -> instance.deleteApplication(empty))
            .isInstanceOf(InvalidArgumentException.class);
        
        String badId = one(alphabeticString());
        assertThrows(() -> instance.deleteApplication(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetById() throws Exception
    {
        ResultSet results = mock(ResultSet.class);
        when(results.one()).thenReturn(mockRow);
        when(session.execute(Mockito.any(Statement.class)))
            .thenReturn(results);
        
        Application result = instance.getById(appId);
       
        verify(session).execute(statementCaptor.capture());
        
        Statement statementMade = statementCaptor.getValue();
        assertThat(statementMade, notNullValue());
        
        assertThat(result.applicationId, is(appId));
    }

    @Test
    public void testContainsApplication() throws Exception
    {
        ResultSet results = mock(ResultSet.class);
        when(session.execute(Mockito.any(Statement.class)))
            .thenReturn(results);
       
        Row fakeRow = mock(Row.class);
        when(results.one()).thenReturn(fakeRow);
        
        long count = one(longs(0, 2));
        when(fakeRow.getLong(0)).thenReturn(count);
        
        boolean result = instance.containsApplication(appId);
        assertThat(result, is(count > 0L));
    }

    @Test
    public void testGetApplicationsOwnedBy() throws Exception
    {
    }

    @Test
    public void testGetApplicationsByOrg() throws Exception
    {
    }

    @Test
    public void testSearchByName() throws Exception
    {
    }

    @Test
    public void testGetRecentlyCreated() throws Exception
    {
    }
    
    private Row mockRowFor(Application app)
    {
        Row row = mock(Row.class);
        
        when(row.getUUID(APP_ID))
            .thenReturn(fromString(app.applicationId));
        
        when(row.getUUID(ORG_ID))
            .thenReturn(fromString(app.organizationId));
        
        when(row.getString(APP_NAME))
            .thenReturn(app.name);
        
        when(row.getString(APP_DESCRIPTION))
            .thenReturn(app.applicationDescription);
        
        when(row.getTimestamp(TIME_PROVISIONED))
            .thenReturn(new Date(app.timeOfProvisioning));
        
        return row;
    }

}