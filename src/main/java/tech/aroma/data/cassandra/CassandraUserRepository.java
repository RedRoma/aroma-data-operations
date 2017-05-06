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

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.UserRepository;
import tech.aroma.data.cassandra.Tables.Users;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.Role;
import tech.aroma.thrift.TimeUnit;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.functions.TimeFunctions;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.ttl;
import static java.util.stream.Collectors.toSet;
import static tech.aroma.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.data.assertions.RequestAssertions.validUser;
import static tech.aroma.data.cassandra.Tables.Users.TABLE_NAME_BY_GITHUB_PROFILE;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 * Stores user information in Cassandra.
 *
 * @author SirWellington
 */
final class CassandraUserRepository implements UserRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(CassandraUserRepository.class);

    private final Session cassandra;
    private final Function<Row, User> userMapper;

    @Inject
    CassandraUserRepository(Session cassandra,
                            Function<Row, User> userMapper)
    {
        checkThat(cassandra, userMapper)
            .are(notNull());

        this.cassandra = cassandra;
        this.userMapper = userMapper;
    }

    @Override
    public void saveUser(User user) throws TException
    {
        checkThat(user)
            .throwing(InvalidArgumentException.class)
            .is(validUser());

        Statement statement = createStatementToSaveUser(user);

        LOG.debug("Executing statement in Cassandra to save user {}", user);
        tryToExecute(statement);
    }

    @Override
    public User getUser(String userId) throws TException
    {
        checkUserId(userId);

        LOG.debug("Executing query to get user with ID {}", userId);
        Statement query = createQueryToGetUser(userId);

        ResultSet results = tryToExecute(query);

        Row row = results.one();
        checkThat(row)
            .throwing(UserDoesNotExistException.class)
            .usingMessage("Could not find user with ID: " + userId)
            .is(notNull());

        User user = convertRowToUser(row);
        return user;
    }

    @Override
    public void deleteUser(String userId) throws TException
    {
        checkUserId(userId);

        //Must first get related data to delete all secondary tables
        User user = this.getUser(userId);

        Statement deleteUserStatement = createQueryToDeleteUser(user);

        LOG.debug("Executing Statement to delete user with ID {}", userId);
        tryToExecute(deleteUserStatement);
    }

    @Override
    public boolean containsUser(String userId) throws TException
    {
        checkUserId(userId);

        Statement selectStatement = createQueryToCheckExistenceFor(userId);

        LOG.info("Issuing query to check if user with ID [{}] exists", userId);
        ResultSet results = tryToExecute(selectStatement);

        long result = results.one().getLong(0);
        return result > 0L;

    }

    @Override
    public User getUserByEmail(String emailAddress) throws TException
    {
        checkThat(emailAddress)
            .throwing(InvalidArgumentException.class)
            .usingMessage("email cannot be empty")
            .is(nonEmptyString());

        Statement query = createQueryToGetUserByEmail(emailAddress);

        LOG.debug("Issuing CQL Query to find user with email {}", emailAddress);
        ResultSet results = tryToExecute(query);

        Row row = results.one();

        checkThat(row)
            .throwing(UserDoesNotExistException.class)
            .usingMessage("Could not find user with email: " + emailAddress)
            .is(notNull());

        User user = convertRowToUser(row);

        return user;
    }

    @Override
    public User findByGithubProfile(String githubProfile) throws TException
    {
        checkThat(githubProfile)
            .throwing(InvalidArgumentException.class)
            .usingMessage("github profile cannot be empty")
            .is(nonEmptyString());

        Statement query = createQueryToGetUsersByGithubProfile(githubProfile);

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to query Cassandra Table {} for profile {}", TABLE_NAME_BY_GITHUB_PROFILE, githubProfile, ex);
            throw new OperationFailedException("Could not query for profile: " + ex.getMessage());
        }

        Row row = results.one();

        checkThat(row)
            .throwing(UserDoesNotExistException.class)
            .usingMessage("Could not find user with Github Profile: " + githubProfile)
            .is(notNull());

        User user = convertRowToUser(row);

        return user;
    }

    @Override
    public List<User> getRecentlyCreatedUsers() throws TException
    {
        List<User> users = Lists.create();
        
        Statement query = createQueryToGetRecentlyCreatedUsers();
        
        ResultSet results = tryToExecute(query);
        
        for(Row row : results)
        {
            User user = this.convertRowToUser(row);
            users.add(user);
        }
        
        return users;
    }

    private Statement createStatementToSaveUser(User user)
    {
        BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);

        Statement insertIntoUsersTable = createInsertIntoUserTable(user);
        batchStatement.add(insertIntoUsersTable);

        Statement insertIntoUsersByEmailTable = createInsertIntoUsersByEmailTable(user);
        batchStatement.add(insertIntoUsersByEmailTable);

        if (!isNullOrEmpty(user.githubProfile))
        {
            Statement insertIntoUsersByGithubTable = createInsertIntoUsersByGithubTable(user);
            batchStatement.add(insertIntoUsersByGithubTable);
        }
        
        Statement insertIntoRecentUsersTable = createInsertIntoRecentUsersTable(user);
        batchStatement.add(insertIntoRecentUsersTable);

        return batchStatement;
    }

    private Statement createInsertIntoUserTable(User user)
    {
        UUID userUuid = UUID.fromString(user.userId);

        Set<String> emails = Sets.createFrom(user.email);
        Set<String> roles = Sets.nullToEmpty(user.roles)
            .stream()
            .map(Role::toString)
            .collect(toSet());
        
        return QueryBuilder
            .insertInto(Users.TABLE_NAME)
            .value(Users.USER_ID, userUuid)
            .value(Users.FIRST_NAME, user.firstName)
            .value(Users.MIDDLE_NAME, user.middleName)
            .value(Users.LAST_NAME, user.lastName)
            .value(Users.EMAILS, emails)
            .value(Users.ROLES, roles)
            .value(Users.GITHUB_PROFILE, user.githubProfile)
            .value(Users.PROFILE_IMAGE_ID, user.profileImageLink)
            .value(Users.TIME_ACCOUNT_CREATED, Instant.now().toEpochMilli());

    }
    
    private Statement createInsertIntoRecentUsersTable(User user)
    {
        UUID userUuid = UUID.fromString(user.userId);

        Set<String> emails = Sets.createFrom(user.email);
        Set<String> roles = Sets.nullToEmpty(user.roles)
            .stream()
            .map(Role::toString)
            .collect(toSet());
        
        LengthOfTime timeUserIsRecent = new LengthOfTime()
            .setUnit(TimeUnit.DAYS)
            .setValue(5);
        
        int recentDuration = (int) TimeFunctions.toSeconds(timeUserIsRecent);

        return QueryBuilder
            .insertInto(Users.TABLE_NAME_RECENT)
            .value(Users.USER_ID, userUuid)
            .value(Users.FIRST_NAME, user.firstName)
            .value(Users.MIDDLE_NAME, user.middleName)
            .value(Users.LAST_NAME, user.lastName)
            .value(Users.EMAILS, emails)
            .value(Users.ROLES, roles)
            .value(Users.GITHUB_PROFILE, user.githubProfile)
            .value(Users.PROFILE_IMAGE_ID, user.profileImageLink)
            .value(Users.TIME_ACCOUNT_CREATED, Instant.now().toEpochMilli())
            .using(ttl(recentDuration));


    }

    private Statement createInsertIntoUsersByEmailTable(User user)
    {
        UUID userUuid = UUID.fromString(user.userId);

        return QueryBuilder
            .insertInto(Users.TABLE_NAME_BY_EMAIL)
            .value(Users.USER_ID, userUuid)
            .value(Users.EMAIL, user.email)
            .value(Users.FIRST_NAME, user.firstName)
            .value(Users.MIDDLE_NAME, user.middleName)
            .value(Users.LAST_NAME, user.lastName)
            .value(Users.GITHUB_PROFILE, user.githubProfile)
            .value(Users.PROFILE_IMAGE_ID, user.profileImageLink)
            .value(Users.TIME_ACCOUNT_CREATED, Instant.now().toEpochMilli());

    }

    private Statement createInsertIntoUsersByGithubTable(User user)
    {
        UUID userUuid = UUID.fromString(user.userId);

        return QueryBuilder
            .insertInto(Users.TABLE_NAME_BY_GITHUB_PROFILE)
            .value(Users.GITHUB_PROFILE, user.githubProfile)
            .value(Users.USER_ID, userUuid)
            .value(Users.FIRST_NAME, user.firstName)
            .value(Users.MIDDLE_NAME, user.middleName)
            .value(Users.LAST_NAME, user.lastName)
            .value(Users.EMAIL, user.email)
            .value(Users.TIME_ACCOUNT_CREATED, Instant.now().toEpochMilli());

    }

    private Statement createQueryToGetUser(String userId)
    {
        UUID userUuid = UUID.fromString(userId);

        return QueryBuilder
            .select()
            .all()
            .from(Users.TABLE_NAME)
            .where(eq(Users.USER_ID, userUuid))
            .limit(2);
    }

    private Statement createQueryToGetUserByEmail(String email)
    {
        return QueryBuilder
            .select()
            .all()
            .from(Users.TABLE_NAME_BY_EMAIL)
            .where(eq(Users.EMAIL, email));
    }

    private Statement createQueryToGetUsersByGithubProfile(String githubProfile)
    {
        return QueryBuilder
            .select()
            .all()
            .from(Users.TABLE_NAME_BY_GITHUB_PROFILE)
            .where(eq(Users.GITHUB_PROFILE, githubProfile));
    }
    
    private Statement createQueryToGetRecentlyCreatedUsers()
    {
        return QueryBuilder
            .select()
            .all()
            .from(Tables.Users.TABLE_NAME_RECENT)
            .limit(100);
    }

    private User convertRowToUser(Row row)
    {
        return userMapper.apply(row);
    }

    private Statement createQueryToDeleteUser(User user)
    {
        UUID userUuuid = UUID.fromString(user.userId);

        BatchStatement batch = new BatchStatement();

        Statement deleteFromUsersTable = QueryBuilder
            .delete()
            .all()
            .from(Users.TABLE_NAME)
            .where(eq(Users.USER_ID, userUuuid));

        batch.add(deleteFromUsersTable);

        Statement deleteFromRecentUsersTable = QueryBuilder
            .delete()
            .all()
            .from(Users.TABLE_NAME_RECENT)
            .where(eq(Users.USER_ID, userUuuid));
        
        batch.add(deleteFromRecentUsersTable);
        
        Statement deleteFromUserEmailsTable = QueryBuilder
            .delete()
            .all()
            .from(Users.TABLE_NAME_BY_EMAIL)
            .where(eq(Users.EMAIL, user.email));

        batch.add(deleteFromUserEmailsTable);
        
        if (!isNullOrEmpty(user.githubProfile))
        {
            Statement deleteFromGithubTable = QueryBuilder
                .delete()
                .all()
                .from(Users.TABLE_NAME_BY_GITHUB_PROFILE)
                .where(eq(Users.GITHUB_PROFILE, user.githubProfile));

            batch.add(deleteFromGithubTable);
        }
        return batch;

    }

    private Statement createQueryToCheckExistenceFor(String userId)
    {
        UUID userUuid = UUID.fromString(userId);

        return QueryBuilder
            .select()
            .countAll()
            .from(Users.TABLE_NAME)
            .where(eq(Users.USER_ID, userUuid));
    }

    private void checkUserId(String userId) throws InvalidArgumentException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("missing userId")
            .is(nonEmptyString())
            .usingMessage("expecting UUID for userId")
            .is(validUUID());
    }

    private ResultSet tryToExecute(Statement statement) throws OperationFailedException
    {
        try
        {
            return cassandra.execute(statement);
        }
        catch (Exception ex)
        {
            LOG.error("Cassandra Operation failed", ex);
            throw new OperationFailedException("Data Operation Failed: " + ex.getMessage());
        }
    }

}
