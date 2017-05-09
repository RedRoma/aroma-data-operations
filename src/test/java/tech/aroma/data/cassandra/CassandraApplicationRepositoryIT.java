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

package tech.aroma.data.cassandra;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.thrift.TException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static sir.wellington.alchemy.collections.sets.Sets.toSet;
import static tech.aroma.thrift.generators.ApplicationGenerators.applications;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraApplicationRepositoryIT
{

    private static final Logger LOG = LoggerFactory.getLogger(CassandraApplicationRepositoryIT.class);
    
    private static Session session;

    @BeforeClass
    public static void begin()
    {
        session = TestCassandraProviders.getTestSession();
    }

    private Application app;

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

    private final Function<Row, Application> appMapper = Mappers.appMapper();


    @Before
    public void setUp()
    {
        app = one(applications());
        appId = app.applicationId;
        
        app.organizationId = orgId;
        owners = Lists.copy(app.owners);
        
        app.setOwners(toSet((owners)));
        instance = new CassandraApplicationRepository(session, appMapper);
        
        
        apps = listOf(applications(), 20).stream()
            .map(app -> app.setOrganizationId(orgId))
            .map(app -> app.setOwners(Sets.createFrom(ownerId)))
            .collect(toList());
        
        apps.forEach(app -> appIdMap.put(app.applicationId, app));
    }
    
    @After
    public void cleanUp() throws TException
    {
        deleteApp(app);
        deleteApps(apps);

        appIdMap.clear();
        apps.clear();
    }
    
    private void saveApplication(List<Application> apps) throws TException
    {
        for (Application app : apps)
        {
            instance.saveApplication(app);
        }
    }
    
    private void deleteApp(Application app)
    {
        try
        {
            instance.deleteApplication(app.applicationId);
        }
        catch (TException ex)
        {
            LOG.warn("Failed to delete App: {}", app, ex);
        }
    }    

    private void deleteApps(List<Application> apps) throws TException
    {
        apps.parallelStream().forEach(this::deleteApp);
    }
    
    @Test
    public void testSaveApplication() throws Exception
    {
        instance.saveApplication(app);
        
        Application result = instance.getById(appId);
        
        assertResultMostlyMatches(result, app);
    }
    
    @DontRepeat
    @Test
    public void testSaveApplicationTwice() throws Exception
    {
        instance.saveApplication(app);
        
        Thread.sleep(5);
        
        instance.saveApplication(app);
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
    public void testGetApplicationsOwnedByWheNone() throws Exception
    {
        List<Application> result = instance.getApplicationsOwnedBy(ownerId);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }

    @Test
    public void testGetApplicationsByOrg() throws Exception
    {
        saveApplication(apps);
        
         List<Application> results = instance.getApplicationsByOrg(orgId);
        
        for(Application result : results)
        {
            String appId = result.applicationId;
            assertThat(appId, isIn(appIdMap.keySet()));
            
            Application match = appIdMap.get(appId);
            assertResultMostlyMatches(result, match);
        }
        
        deleteApps(apps);
    }

    @DontRepeat
    @Test
    public void testSearchByName() throws Exception
    {
        assertThrows(() -> instance.searchByName(app.name))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testGetRecentlyCreated() throws Exception
    {
        List<Application> result = instance.getRecentlyCreated();
        assertThat(result, notNullValue());
    }

    private void assertResultMostlyMatches(Application result, Application expected)
    {
        assertThat(result.applicationId, is(expected.applicationId));
        assertThat(result.name, is(expected.name));
        assertThat(result.organizationId, is(expected.organizationId));
        assertThat(result.applicationDescription, is(expected.applicationDescription));
        assertThat(result.applicationIconMediaId, is(expected.applicationIconMediaId));
        
        assertThat(result.owners, is(expected.owners));
        assertThat(result.timeOfProvisioning, is(expected.timeOfProvisioning));
        assertThat(result.tier, is(expected.tier));
    }

}
