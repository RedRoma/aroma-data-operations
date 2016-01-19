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
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.List;
import java.util.Map;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.maps.Maps;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.exceptions.ApplicationDoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;
import static sir.wellington.alchemy.collections.sets.Sets.toSet;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraApplicationRepositoryIT
{

    @GeneratePojo
    private Application app;

    @GenerateList(Application.class)
    private List<Application> apps;
    
    private final Map<String,Application> appIdMap = Maps.createSynchronized();

    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(UUID)
    private String orgId;
    
    @GenerateString(UUID)
    private String ownerId;
    
    private List<String> owners;
    
    private CassandraApplicationRepository instance;

    private static Session session;
    private static QueryBuilder queryBuilder;
    private static Cluster cluster;

    @BeforeClass
    public static void begin()
    {
        cluster = TestSessions.createTestCluster();
        queryBuilder = TestSessions.createQueryBuilder(cluster);
        session = TestSessions.createTestSession(cluster);
    }

    @AfterClass
    public static void end()
    {
        session.close();
        cluster.close();
    }

    @Before
    public void setUp()
    {
        app.applicationId = appId;
        app.organizationId = orgId;
        owners = listOf(uuids, 5);
        
        app.setOwners(toSet((owners)));
        instance = new CassandraApplicationRepository(session, queryBuilder);
        
        
        apps = apps.stream()
            .map(app -> app.setOrganizationId(orgId))
            .map(app -> app.setOwners(Sets.createFrom(ownerId)))
            .map(app -> app.setApplicationId(one(uuids)))
            .collect(toList());
        
        apps.forEach(app -> appIdMap.put(app.applicationId, app));
    }
    
    @After
    public void cleanUp() throws TException
    {
        appIdMap.clear();
        apps.clear();
        
        try
        {
            instance.deleteApplication(appId);
        }
        catch(Exception ex)
        {
        }
    }
    
    private void saveApplication(List<Application> apps) throws TException
    {
        for (Application app : apps)
        {
            instance.saveApplication(app);
        }
    }
    
    private void deleteApps(List<Application> apps) throws TException
    {
        for (Application app : apps)
        {
            instance.deleteApplication(app.applicationId);
        }
    }
    
    @Test
    public void testSaveApplication() throws Exception
    {
        instance.saveApplication(app);
        
        Application result = instance.getById(appId);
        
        assertResultMostlyMatches(result, app);
    }

    @Test
    public void testDeleteApplication() throws Exception
    {
        instance.saveApplication(app);
        
        instance.deleteApplication(appId);
        
        assertThat(instance.containsApplication(appId), is(false));
    }

    @Test
    public void testDeleteApplicationWhenNotExists() throws Exception
    {
        assertThrows(() -> instance.deleteApplication(appId))
            .isInstanceOf(ApplicationDoesNotExistException.class);
    }

    @Test
    public void testGetById() throws Exception
    {
        instance.saveApplication(app);
        
        assertThat(instance.containsApplication(appId), is(true));
        Application result = instance.getById(appId);
        
        assertResultMostlyMatches(result, app);
    }

    @Test
    public void testGetByIdWhenNotExists() throws Exception
    {
        assertThrows(() -> instance.getById(appId))
            .isInstanceOf(ApplicationDoesNotExistException.class);
    }

    @Test
    public void testContainsApplicationWhenExists() throws Exception
    {
        instance.saveApplication(app);
        
        boolean result = instance.containsApplication(appId);
        assertThat(result, is(true));
    }

    @Test
    public void testContainsApplicationWhenNotExists() throws Exception
    {
        boolean result = instance.containsApplication(appId);
        assertThat(result, is(false));
    }

    @Test
    public void testGetApplicationsOwnedBy() throws Exception
    {
        saveApplication(apps);
        
        List<Application> results = instance.getApplicationsOwnedBy(ownerId);
        
        for(Application result : results)
        {
            String appId = result.applicationId;
            assertThat(appId, isIn(appIdMap.keySet()));
            
            Application match = appIdMap.get(appId);
            assertResultMostlyMatches(result, match);
        }
        
        deleteApps(apps);
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

    private void assertResultMostlyMatches(Application result, Application expected)
    {
        assertThat(result.applicationId, is(expected.applicationId));
        assertThat(result.name, is(expected.name));
        assertThat(result.organizationId, is(expected.organizationId));
        assertThat(result.applicationDescription, is(expected.applicationDescription));
        
        assertThat(result.owners, is(expected.owners));
        assertThat(result.timeOfProvisioning, is(expected.timeOfProvisioning));
        assertThat(result.tier, is(expected.tier));
    }

}
