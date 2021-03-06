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

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.cassandra.Tables.*;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static tech.aroma.data.assertions.RequestAssertions.*;
import static tech.aroma.data.cassandra.Tables.Follow.*;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class CassandraFollowerRepository implements FollowerRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(CassandraFollowerRepository.class);

    private final Session cassandra;
    private final Function<Row, User> userMapper;
    private final Function<Row, Application> applicationMapper;

    @Inject
    CassandraFollowerRepository(Session cassandra,
                                Function<Row, User> userMapper,
                                Function<Row, Application> applicationMapper)
    {
        checkThat(cassandra, userMapper, applicationMapper)
            .are(notNull());

        this.cassandra = cassandra;
        this.applicationMapper = applicationMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void saveFollowing(User user, Application application) throws TException
    {
        checkThat(user)
            .throwing(InvalidArgumentException.class)
            .is(validUser());

        checkThat(application)
            .throwing(InvalidArgumentException.class)
            .is(validApplication());

        Statement insertStatement = createStatementToSaveFollowing(user, application);

        try
        {
            cassandra.execute(insertStatement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to save following: User: [{}] App: [{}]", user.userId, application.applicationId, ex);
            throw new OperationFailedException("Could not save following in Cassandra: " + ex.getMessage());
        }
    }

    @Override
    public void deleteFollowing(String userId, String applicationId) throws TException
    {
        checkUserId(userId);
        checkAppId(applicationId);

        Statement deleteStatement = createDeleteStatementFor(userId, applicationId);

        try
        {
            cassandra.execute(deleteStatement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to delete the following between User: [{}] App: [{}]", userId, applicationId, ex);
            throw new OperationFailedException("Could not delete following: " + ex.getMessage());
        }

    }

    @Override
    public boolean followingExists(String userId, String applicationId) throws TException
    {
        checkUserId(userId);
        checkAppId(applicationId);

        Statement query = createStatementToCheckIfFollowingExists(userId, applicationId);

        ResultSet results;
        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query for following between User: [{}] App: [{}]", userId, applicationId, ex);
            throw new OperationFailedException("Could not query for following: " + ex.getMessage());
        }

        Row row = results.one();
        checkRowExists(row);

        long count = row.getLong(0);
        return count > 0;
    }

    @Override
    public List<Application> getApplicationsFollowedBy(String userId) throws TException
    {
        checkUserId(userId);

        Statement query = createQueryForAppsFollowedBy(userId);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query Cassandra for apps followed by User: [{}]", userId, ex);
            throw new OperationFailedException("Could not find apps followed by user: " + ex.getMessage());
        }

        List<Application> apps = Lists.create();

        for (Row row : results)
        {
            Application app = createAppFromRow(row);
            apps.add(app);
        }

        LOG.debug("Found {} apps followed by {}", apps.size(), userId);
        return apps;
    }

    @Override
    public List<User> getApplicationFollowers(String applicationId) throws TException
    {
        checkAppId(applicationId);

        Statement query = createQueryForFollowersOfApp(applicationId);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query for App's followed: App: [{}]", applicationId, ex);
            throw new OperationFailedException("Could not query for App's Followers: " + ex.getMessage());
        }

        List<User> followers = Lists.create();

        for (Row row : results)
        {
            User follower = createUserFromRow(row);
            followers.add(follower);
        }

        LOG.debug("Found {} Users followed App [{}]", followers.size(), applicationId);
        return followers;
    }

    private Statement createStatementToSaveFollowing(User user, Application app)
    {
        UUID userId = UUID.fromString(user.userId);
        UUID appId = UUID.fromString(app.applicationId);

        BatchStatement batch = new BatchStatement();

        Statement insertIntoAppFollowersTable = QueryBuilder
            .insertInto(Follow.TABLE_NAME_APP_FOLLOWERS)
            .value(APP_ID, appId)
            .value(USER_ID, userId)
            .value(APP_NAME, app.name)
            .value(USER_FIRST_NAME, user.firstName)
            .value(TIME_OF_FOLLOW, Instant.now().toEpochMilli());

        batch.add(insertIntoAppFollowersTable);

        Statement insertIntoUserFollowingsTable = QueryBuilder
            .insertInto(Follow.TABLE_NAME_USER_FOLLOWING)
            .value(APP_ID, appId)
            .value(USER_ID, userId)
            .value(APP_NAME, app.name)
            .value(USER_FIRST_NAME, user.firstName)
            .value(TIME_OF_FOLLOW, Instant.now().toEpochMilli());

        batch.add(insertIntoUserFollowingsTable);

        return batch;
    }

    private Statement createDeleteStatementFor(String userId, String applicationId)
    {
        UUID appUuid = UUID.fromString(applicationId);
        UUID userUuid = UUID.fromString(userId);

        BatchStatement batch = new BatchStatement();

        Statement deleteFromAppFollowersTable = QueryBuilder
            .delete()
            .all()
            .from(Follow.TABLE_NAME_APP_FOLLOWERS)
            .where(eq(APP_ID, appUuid))
            .and(eq(USER_ID, userUuid));

        batch.add(deleteFromAppFollowersTable);

        Statement deleteFromUserFollowingsTable = QueryBuilder
            .delete()
            .all()
            .from(Follow.TABLE_NAME_USER_FOLLOWING)
            .where(eq(APP_ID, appUuid))
            .and(eq(USER_ID, userUuid));

        batch.add(deleteFromUserFollowingsTable);

        return batch;
    }

    private Statement createStatementToCheckIfFollowingExists(String userId, String applicationId)
    {
        UUID appUuid = UUID.fromString(applicationId);
        UUID userUuid = UUID.fromString(userId);

        return QueryBuilder
            .select()
            .countAll()
            .from(Follow.TABLE_NAME_APP_FOLLOWERS)
            .where(eq(USER_ID, userUuid))
            .and(eq(APP_ID, appUuid));
    }

    private void checkRowExists(Row row) throws OperationFailedException
    {
        checkThat(row)
            .throwing(OperationFailedException.class)
            .usingMessage("Query Failed")
            .is(notNull());
    }

    private Statement createQueryForAppsFollowedBy(String userId)
    {
        UUID userUuid = UUID.fromString(userId);

        return QueryBuilder
            .select()
            .column(Follow.APP_NAME).as(Applications.APP_NAME)
            .column(Follow.APP_ID).as(Applications.APP_ID)
            .column(Follow.USER_FIRST_NAME).as(Users.FIRST_NAME)
            .column(Follow.TIME_OF_FOLLOW)
            .column(Follow.USER_ID)
            .from(Follow.TABLE_NAME_USER_FOLLOWING)
            .where(eq(USER_ID, userUuid));
    }

    private Statement createQueryForFollowersOfApp(String applicationId)
    {
        UUID appUuid = UUID.fromString(applicationId);

        return QueryBuilder
            .select()
            .column(Follow.APP_NAME).as(Applications.APP_NAME)
            .column(Follow.APP_ID).as(Applications.APP_ID)
            .column(Follow.USER_FIRST_NAME).as(Users.FIRST_NAME)
            .column(Follow.TIME_OF_FOLLOW)
            .column(Follow.USER_ID)
            .from(Follow.TABLE_NAME_APP_FOLLOWERS)
            .where(eq(APP_ID, appUuid));
    }

    private Application createAppFromRow(Row row)
    {
        return applicationMapper.apply(row);
    }

    private User createUserFromRow(Row row)
    {
        return userMapper.apply(row);
    }

    private void checkUserId(String userId) throws InvalidArgumentException
    {
        checkThat(userId)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(validUserId());
    }

    private void checkAppId(String applicationId) throws InvalidArgumentException
    {
        checkThat(applicationId)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(validApplicationId());

    }

}
