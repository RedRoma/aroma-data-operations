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

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
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
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.data.cassandra.Tables.Users;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.exceptions.UserDoesNotExistException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static tech.aroma.banana.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.banana.data.assertions.RequestAssertions.validUser;
import static tech.aroma.banana.data.cassandra.Tables.Users.TABLE_NAME_BY_GITHUB_PROFILE;
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
    private final QueryBuilder queryBuilder;
    private final Function<Row, User> userMapper;

    @Inject
    CassandraUserRepository(Session cassandra,
                            QueryBuilder queryBuilder,
                            Function<Row, User> userMapper)
    {
        checkThat(cassandra, queryBuilder, userMapper)
            .are(notNull());

        this.cassandra = cassandra;
        this.queryBuilder = queryBuilder;
        this.userMapper = userMapper;
    }

    @Override
    public void saveUser(User user) throws TException
    {
        checkThat(user)
            .throwing(InvalidArgumentException.class)
            .is(validUser());

        BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);

        Insert insertIntoUsersTable = createInsertIntoUserTable(user);
        batchStatement.add(insertIntoUsersTable);

        Insert insertIntoUsersByEmailTable = createInsertIntoUsersByEmailTable(user);
        batchStatement.add(insertIntoUsersByEmailTable);

        if (!isNullOrEmpty(user.githubProfile))
        {
            Insert insertIntoUsersByGithubTable = createInsertIntoUsersByGithubTable(user);
            batchStatement.add(insertIntoUsersByGithubTable);
        }
        
        LOG.debug("Executing batch statement in Cassandra to save user {}", user);
        tryToExecute(batchStatement);
    }

    @Override
    public User getUser(String userId) throws TException
    {
        checkUserId(userId);

        LOG.debug("Executing query to get user with ID {}", userId);
        Select query = createQueryToGetUser(userId);
        
        ResultSet results = tryToExecute(query);

        Row row = results.one();
        checkThat(row)
            .throwing(UserDoesNotExistException.class)
            .usingMessage("Could not find user with ID: " + userId)
            .is(notNull());

        User user = createUserFromRow(row);
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

        User user = createUserFromRow(row);

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

        User user = createUserFromRow(row);

        return user;
    }
    
    @Override
    public List<User> getRecentlyCreatedUsers() throws TException
    {
        return Lists.emptyList();
    }

    private Insert createInsertIntoUserTable(User user)
    {
        UUID userUuid = UUID.fromString(user.userId);

        Set<String> emails = Sets.createFrom(user.email);

        return queryBuilder.insertInto(Users.TABLE_NAME)
            .value(Users.USER_ID, userUuid)
            .value(Users.FIRST_NAME, user.firstName)
            .value(Users.MIDDLE_NAME, user.middleName)
            .value(Users.LAST_NAME, user.lastName)
            .value(Users.EMAILS, emails)
            .value(Users.ROLES, user.roles)
            .value(Users.GITHUB_PROFILE, user.githubProfile)
            .value(Users.PROFILE_IMAGE_ID, user.profileImageLink);

    }

    private Insert createInsertIntoUsersByEmailTable(User user)
    {
        UUID userUuid = UUID.fromString(user.userId);

        return queryBuilder.insertInto(Users.TABLE_NAME_BY_EMAIL)
            .value(Users.USER_ID, userUuid)
            .value(Users.EMAIL, user.email)
            .value(Users.FIRST_NAME, user.firstName)
            .value(Users.MIDDLE_NAME, user.middleName)
            .value(Users.LAST_NAME, user.lastName)
            .value(Users.GITHUB_PROFILE, user.githubProfile)
            .value(Users.PROFILE_IMAGE_ID, user.profileImageLink);
    }

    private Insert createInsertIntoUsersByGithubTable(User user)
    {
        UUID userUuid = UUID.fromString(user.userId);

        return queryBuilder.insertInto(Users.TABLE_NAME_BY_GITHUB_PROFILE)
            .value(Users.GITHUB_PROFILE, user.githubProfile)
            .value(Users.USER_ID, userUuid)
            .value(Users.FIRST_NAME, user.firstName)
            .value(Users.MIDDLE_NAME, user.middleName)
            .value(Users.LAST_NAME, user.lastName)
            .value(Users.EMAIL, user.email);
    }

    private Select createQueryToGetUser(String userId)
    {
        UUID userUuid = UUID.fromString(userId);

        return queryBuilder
            .select()
            .all()
            .from(Users.TABLE_NAME)
            .where(eq(Users.USER_ID, userUuid))
            .limit(2);
    }

    private Statement createQueryToGetUserByEmail(String email)
    {
        return queryBuilder
            .select()
            .all()
            .from(Users.TABLE_NAME_BY_EMAIL)
            .where(eq(Users.EMAIL, email));
    }

    private Statement createQueryToGetUsersByGithubProfile(String githubProfile)
    {
        return queryBuilder
            .select()
            .all()
            .from(Users.TABLE_NAME_BY_GITHUB_PROFILE)
            .where(eq(Users.GITHUB_PROFILE, githubProfile));
    }

    private User createUserFromRow(Row row)
    {
        return userMapper.apply(row);
    }

    private Statement createQueryToDeleteUser(User user)
    {
        UUID userUuuid = UUID.fromString(user.userId);

        BatchStatement batch = new BatchStatement();

        Statement deleteFromUsersTable = queryBuilder
            .delete()
            .all()
            .from(Users.TABLE_NAME)
            .where(eq(Users.USER_ID, userUuuid));

        batch.add(deleteFromUsersTable);

        Statement deleteFromUserEmailsTable = queryBuilder
            .delete()
            .all()
            .from(Users.TABLE_NAME_BY_EMAIL)
            .where(eq(Users.EMAIL, user.email));

        batch.add(deleteFromUserEmailsTable);

        if (!isNullOrEmpty(user.githubProfile))
        {
            Statement deleteFromGithubTable = queryBuilder
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

        return queryBuilder
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
