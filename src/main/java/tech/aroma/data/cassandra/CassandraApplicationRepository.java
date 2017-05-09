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

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static tech.aroma.data.assertions.RequestAssertions.*;
import static tech.aroma.data.cassandra.Tables.Applications.*;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthGreaterThanOrEqualTo;

/**
 *
 * @author SirWellington
 */
final class CassandraApplicationRepository implements ApplicationRepository
{
    
    private final static Logger LOG = LoggerFactory.getLogger(CassandraApplicationRepository.class);
    private final static Duration DEFAULT_RECENT_DURATION = Duration.ofDays(5);
    
    private final Session cassandra;
    private final Function<Row, Application> applicationMapper;
    
    @Inject
    CassandraApplicationRepository(Session cassandra, 
                                   Function<Row, Application> applicationMapper)
    {
        checkThat(cassandra, applicationMapper)
            .are(notNull());
        
        this.cassandra = cassandra;
        this.applicationMapper = applicationMapper;
    }
    
    @Override
    public void saveApplication(Application application) throws TException
    {
        checkThat(application)
            .throwing(InvalidArgumentException.class)
            .is(validApplication());
        
        Statement statement = createStatementToSave(application);
        
        try
        {
            cassandra.execute(statement);
            LOG.debug("Successfully saved Application in Cassandra: {}", application);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to store Application in Cassandra: {}", application, ex);
            throw new OperationFailedException("Could not save Application: " + ex.getMessage());
        }
    }
    
    @Override
    public void deleteApplication(String applicationId) throws TException
    {
        checkApplicationId(applicationId);

        //Must fetch the full Application first
        Application app = this.getById(applicationId);
        
        Statement statement = createDeleteStatementFor(app);
        
        try
        {
            cassandra.execute(statement);
            LOG.debug("Successfully deleted Application with ID {}", applicationId);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to delete application with ID [{}] from Cassandra", applicationId, ex);
            throw new OperationFailedException("Could not delete Application with ID: " + applicationId);
        }
    }
    
    @Override
    public Application getById(String applicationId) throws TException
    {
        checkApplicationId(applicationId);
        
        Statement query = createQueryForAppWithId(applicationId);
        
        ResultSet results;
        
        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query for application with ID {}", applicationId, ex);
            throw new OperationFailedException("Could not Query Application with ID: " + applicationId);
        }
        
        Row row = results.one();
        checkRowNotMissing(applicationId, row);
        
        Application app = createApplicationFromRow(row);
        
        return app;
    }
    
    @Override
    public boolean containsApplication(String applicationId) throws TException
    {
        checkApplicationId(applicationId);
        
        Statement query = createQueryToCheckIfAppIdExists(applicationId);
        
        ResultSet results;
        
        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to check Application existence for [{}]", applicationId, ex);
            throw new OperationFailedException("Could not check for application existence: " + applicationId);
        }
        
        Row row = results.one();
        checkRowNotMissing(applicationId, row);
        
        long count = row.getLong(0);
        return count > 0L;
    }
    
    @Override
    public List<Application> getApplicationsOwnedBy(String userId) throws TException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(validUserId());
        
        Statement query = createQueryForAppsOwnedBy(userId);
        
        ResultSet results;
        
        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query for Apps owned by {}", userId, ex);
            throw new OperationFailedException("Could not determine Apps owned by user: " + userId);
        }
        
        List<Application> apps = Lists.create();
        
        for (Row row : results)
        {
            if (row == null)
            {
                continue;
            }
            
            Application app = createApplicationFromRow(row);
            apps.add(app);
        }
        
        LOG.debug("Found {} apps owned by user {}", apps.size(), userId);
        
        return apps;
    }
    
    @Override
    public List<Application> getApplicationsByOrg(String orgId) throws TException
    {
        checkThat(orgId)
            .throwing(InvalidArgumentException.class)
            .is(validOrgId());
        
        Statement query = createQueryForAppsWithOrg(orgId);
        
        ResultSet results;
        
        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to find Apps by Org with ID [{}]", orgId, ex);
            throw new OperationFailedException("Could not find Org's Apps: " + orgId);
        }
        
        List<Application> apps = Lists.create();
        
        for (Row row : results)
        {
            Application app = createApplicationFromRow(row);
            apps.add(app);
        }
        
        LOG.debug("Found {} apps in Org {}", apps.size(), orgId);
        
        return apps;
        
    }
    
    @Override
    public List<Application> searchByName(String searchTerm) throws TException
    {
        checkSearchTerm(searchTerm);
        
        throw new OperationFailedException("Searching not supported yet");
    }
    
    @Override
    public List<Application> getRecentlyCreated() throws TException
    {
        List<Application> apps = Lists.create();
        
        Statement query = createQueryForRecentlyCreatedApps();
        
        ResultSet results = null;
        
        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query for recently created apps", ex);
            throw new OperationFailedException("Could not get recently created apps: " + ex.getMessage());
        }

        for (Row row : results)
        {
            Application app = createApplicationFromRow(row);
            apps.add(app);
        }
        
        LOG.debug("Found {} recently created apps", apps.size());
        
        return apps;
    }
    
    private Statement createStatementToSave(Application app)
    {
        BatchStatement batch = new BatchStatement();

        //UUIDs
        UUID appId = UUID.fromString(app.applicationId);
        UUID iconId = null;
        UUID orgId = null;
        
        //Enums
        String tier = null;
        String programmingLanguage = null;
        
        if (app.tier != null)
        {
            tier = app.tier.toString();
        }
        
        if (app.programmingLanguage != null)
        {
            programmingLanguage = app.programmingLanguage.toString();
        }
        
        if (app.isSetApplicationIconMediaId())
        {
            iconId = UUID.fromString(app.applicationIconMediaId);
        }
        
        if (app.isSetOrganizationId())
        {
            orgId = UUID.fromString(app.organizationId);
        }
        
        Set<UUID> owners = Sets.nullToEmpty(app.owners)
            .stream()
            .map(UUID::fromString)
            .collect(Collectors.toSet());
        
        Statement insertIntoMainTable = QueryBuilder
            .insertInto(TABLE_NAME)
            .value(APP_ID, appId)
            .value(APP_NAME, app.name)
            .value(APP_DESCRIPTION, app.applicationDescription)
            .value(ICON_MEDIA_ID, iconId)
            .value(ORG_ID, orgId)
            .value(OWNERS, owners)
            .value(PROGRAMMING_LANGUAGE, programmingLanguage)
            .value(TIME_PROVISIONED, app.timeOfProvisioning)
            .value(TIME_OF_TOKEN_EXPIRATION, app.timeOfTokenExpiration)
            .value(TIER, tier);
        
        batch.add(insertIntoMainTable);
        
        //Save into the "Recents Table"
        Long timeToLive = DEFAULT_RECENT_DURATION.getSeconds();
        
        Statement insertIntoRecentlyCreated = QueryBuilder
            .insertInto(TABLE_NAME_RECENTLY_CREATED)
            .value(APP_ID, appId)
            .value(APP_NAME, app.name)
            .value(APP_DESCRIPTION, app.applicationDescription)
            .value(ICON_MEDIA_ID, iconId)
            .value(ORG_ID, orgId)
            .value(OWNERS, owners)
            .value(PROGRAMMING_LANGUAGE, programmingLanguage)
            .value(TIME_PROVISIONED, app.timeOfProvisioning)
            .value(TIME_OF_TOKEN_EXPIRATION, app.timeOfTokenExpiration)
            .value(TIER, tier)
            .using(ttl(timeToLive.intValue()));
        
        batch.add(insertIntoRecentlyCreated);
        return batch;
    }
    
    private Statement createDeleteStatementFor(Application app)
    {
        BatchStatement batch = new BatchStatement();
        
        UUID appId = UUID.fromString(app.applicationId);
        
        Statement deleteFromMainTable = QueryBuilder
            .delete()
            .all()
            .from(TABLE_NAME)
            .where(eq(APP_ID, appId));
        
        batch.add(deleteFromMainTable);
        
        Statement deleteFromRecentsTable = QueryBuilder
            .delete()
            .all()
            .from(TABLE_NAME_RECENTLY_CREATED)
            .where(eq(APP_ID, appId));
        batch.add(deleteFromRecentsTable);
        
        return batch;
    }
    
    private Statement createQueryForAppWithId(String applicationId)
    {
        UUID appId = UUID.fromString(applicationId);
        
        return QueryBuilder
            .select()
            .all()
            .from(TABLE_NAME)
            .where(eq(APP_ID, appId))
            .limit(2);
    }
    
    private Application createApplicationFromRow(Row row) throws OperationFailedException
    {
      return applicationMapper.apply(row);
    }
    
    private void checkApplicationId(String applicationId) throws InvalidArgumentException
    {
        checkThat(applicationId)
            .throwing(InvalidArgumentException.class)
            .is(validApplicationId());
    }
    
    private Statement createQueryToCheckIfAppIdExists(String applicationId)
    {
        UUID appId = UUID.fromString(applicationId);
        
        return QueryBuilder
            .select()
            .countAll()
            .from(TABLE_NAME)
            .where(eq(APP_ID, appId));
    }
    
    private void checkRowNotMissing(String applicationId, Row row) throws ApplicationDoesNotExistException
    {
        checkThat(row)
            .throwing(ApplicationDoesNotExistException.class)
            .usingMessage("No App with ID: " + applicationId)
            .is(notNull());
    }
    
    private Statement createQueryForAppsOwnedBy(String userId)
    {
        UUID ownerId = UUID.fromString(userId);
        
        return QueryBuilder
            .select()
            .all()
            .from(TABLE_NAME)
            .where(contains(OWNERS, ownerId));
    }
    
    private Statement createQueryForAppsWithOrg(String orgId)
    {
        UUID uuid = UUID.fromString(orgId);
        
        return QueryBuilder
            .select()
            .all()
            .from(TABLE_NAME)
            .where(eq(ORG_ID, uuid));
    }
    
    private void checkSearchTerm(String searchTerm) throws InvalidArgumentException
    {
        checkThat(searchTerm)
            .throwing(InvalidArgumentException.class)
            .is(stringWithLengthGreaterThanOrEqualTo(2))
            .is(nonEmptyString());
    }
    
    private Statement createQueryForRecentlyCreatedApps()
    {
        return QueryBuilder
            .select()
            .all()
            .from(TABLE_NAME_RECENTLY_CREATED)
            .limit(200)
            .allowFiltering();
    }
    
}
