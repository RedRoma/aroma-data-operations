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
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.data.cassandra.Tables.UsersTable;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.exceptions.UserDoesNotExistException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static tech.aroma.banana.data.assertions.RequestAssertions.validUser;
import static tech.aroma.banana.data.cassandra.Tables.UsersTable.TABLE_NAME_BY_GITHUB_PROFILE;
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
        Insert insertIntoUsersByEmailTable = createInsertIntoUsersByEmailTable(user);
        Insert insertIntoUsersByGithubTable = createInsertIntoUsersByGithubTable(user);

        batchStatement.add(insertIntoUsersTable)
            .add(insertIntoUsersByEmailTable)
            .add(insertIntoUsersByGithubTable);

        LOG.debug("Executing Batch Statement: {}", batchStatement);
        try
        {
            cassandra.execute(batchStatement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to save user in Cassandra: {}", user, ex);
            throw new OperationFailedException("Could not save user");
        }

    }

    @Override
    public User getUser(String userId) throws TException
    {
        checkUserId(userId);

        Select query = createQueryToGetUser(userId);
        ResultSet results = cassandra.execute(query);

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
        try
        {
            cassandra.execute(deleteUserStatement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to delete User with ID {}", userId);
            throw new OperationFailedException("Could not delete user: " + userId);
        }
    }

    @Override
    public boolean containsUser(String userId) throws TException
    {
        checkUserId(userId);

        Statement selectStatement = createQueryToCheckExistenceFor(userId);

        ResultSet results;

        try
        {
            results = cassandra.execute(selectStatement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to run Select Statement on Cassandra: {}", selectStatement, ex);
            throw new OperationFailedException("Could not check for the existence of user: " + userId);
        }

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

        ResultSet results;

        try
        {
            results = cassandra.execute(query);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to execute query for user by email {}", emailAddress, ex);
            throw new OperationFailedException("Could not find user with email: " + emailAddress);
        }

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

    private Insert createInsertIntoUserTable(User user)
    {
        return queryBuilder.insertInto(UsersTable.TABLE_NAME)
            .value(UsersTable.USER_ID, user.userId)
            .value(UsersTable.FIRST_NAME, user.firstName)
            .value(UsersTable.MIDDLE_NAME, user.middleName)
            .value(UsersTable.LAST_NAME, user.lastName)
            .value(UsersTable.EMAILS, user.email)
            .value(UsersTable.ROLES, user.roles)
            .value(UsersTable.GITHUB_PROFILE, user.profileImageLink);

    }

    private Insert createInsertIntoUsersByEmailTable(User user)
    {
        return queryBuilder.insertInto(UsersTable.TABLE_NAME_BY_EMAIL)
            .value(UsersTable.USER_ID, user.userId)
            .value(UsersTable.EMAIL, user.email)
            .value(UsersTable.FIRST_NAME, user.firstName)
            .value(UsersTable.MIDDLE_NAME, user.middleName)
            .value(UsersTable.LAST_NAME, user.lastName)
            .value(UsersTable.GITHUB_PROFILE, user.githubProfile)
            .value(UsersTable.GITHUB_PROFILE, user.profileImageLink);
    }

    private Insert createInsertIntoUsersByGithubTable(User user)
    {
        return queryBuilder.insertInto(UsersTable.TABLE_NAME_BY_GITHUB_PROFILE)
            .value(UsersTable.GITHUB_PROFILE, user.githubProfile)
            .value(UsersTable.USER_ID, user.userId)
            .value(UsersTable.FIRST_NAME, user.firstName)
            .value(UsersTable.MIDDLE_NAME, user.middleName)
            .value(UsersTable.LAST_NAME, user.lastName)
            .value(UsersTable.EMAILS, user.email);
    }

    private Select createQueryToGetUser(String userId)
    {
        UUID userUuid = UUID.fromString(userId);

        return queryBuilder
            .select()
            .all()
            .from(UsersTable.TABLE_NAME)
            .where(eq(UsersTable.USER_ID, userUuid))
            .limit(2);
    }

    private Statement createQueryToGetUserByEmail(String email)
    {
        return queryBuilder
            .select()
            .all()
            .from(UsersTable.TABLE_NAME_BY_EMAIL)
            .where(eq(UsersTable.EMAIL, email));
    }

    private Statement createQueryToGetUsersByGithubProfile(String githubProfile)
    {
        return queryBuilder
            .select()
            .all()
            .from(UsersTable.TABLE_NAME_BY_GITHUB_PROFILE)
            .where(eq(UsersTable.GITHUB_PROFILE, githubProfile));
    }

    private User createUserFromRow(Row row)
    {
        return userMapper.apply(row);
    }

    private Statement createQueryToDeleteUser(User user)
    {
        BatchStatement batch = new BatchStatement();

        Statement deleteFromUsersTable = queryBuilder
            .delete()
            .all()
            .from(UsersTable.TABLE_NAME)
            .where(eq(UsersTable.USER_ID, user.userId));

        batch.add(deleteFromUsersTable);

        Statement deleteFromUserEmailsTable = queryBuilder
            .delete()
            .all()
            .from(UsersTable.TABLE_NAME_BY_EMAIL)
            .where(eq(UsersTable.EMAIL, user.email));

        batch.add(deleteFromUserEmailsTable);

        Statement deleteFromGithubTable = queryBuilder
            .delete()
            .all()
            .from(UsersTable.TABLE_NAME_BY_GITHUB_PROFILE)
            .where(eq(UsersTable.GITHUB_PROFILE, user.githubProfile));

        batch.add(deleteFromGithubTable);

        return batch;

    }

    private Statement createQueryToCheckExistenceFor(String userId)
    {
        UUID userUuid = UUID.fromString(userId);

        return queryBuilder
            .select()
            .countAll()
            .from(UsersTable.TABLE_NAME)
            .where(eq(UsersTable.USER_ID, userUuid));
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

}
