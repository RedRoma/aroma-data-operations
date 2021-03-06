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


import java.util.Date;
import java.util.UUID;
import javax.inject.Inject;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.CredentialRepository;
import tech.aroma.data.cassandra.Tables.Credentials;
import tech.aroma.thrift.exceptions.*;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 *
 * @author SirWellington
 */
@Internal
final class CassandraCredentialsRepository implements CredentialRepository
{
    private final static Logger LOG = LoggerFactory.getLogger(CassandraCredentialsRepository.class);
    
    private final Session cassandra;

    @Inject
    CassandraCredentialsRepository(Session cassandra)
    {
        checkThat(cassandra).is(notNull());
        
        this.cassandra = cassandra;
    }

    @Override
    public void saveEncryptedPassword(String userId, String encryptedPassword) throws TException
    {
        checkUserId(userId);
        checkPassword(encryptedPassword);
        
        Statement insertStatement = createInsertStatementFor(userId, encryptedPassword);
        
        tryToExecute(insertStatement, "Could not insert credentials for: " + userId);
        LOG.debug("Successfully stored credentials for {} in Cassandra", userId);
    }

    @Override
    public boolean containsEncryptedPassword(String userId) throws TException
    {
        checkUserId(userId);
        
        Statement query = createQueryToCheckIfExists(userId);
        
        ResultSet results = tryToExecute(query, "Could not query if credentials exist for: " + userId);
        
        checkResultExist(results);
        
        Row row = results.one();
        ensureRowNotEmpty(row);
        
        long count = row.getLong(0);
        return count > 0;
        
    }

    @Override
    public String getEncryptedPassword(String userId) throws TException
    {
        checkUserId(userId);
        
        Statement query = createQueryToGetEncryptedPasswordFor(userId);
        
        ResultSet results = tryToExecute(query, "Could not query for Credentials of " + userId);
        checkResultExist(results);
        
        Row row = results.one();
        ensureRowNotEmpty(row);
        
        String encryptedPassword = row.getString(Tables.Credentials.ENCRYPTED_PASSWORD);
        ensureNotEmpty(encryptedPassword);
        
        return encryptedPassword;
    }

    @Override
    public void deleteEncryptedPassword(String userId) throws TException
    {
        checkUserId(userId);
        
        Statement deleteStatement = createStatementToDeleteCredentialsFor(userId);

        tryToExecute(deleteStatement, "Could not delete credentials for: " + userId);
        LOG.debug("Successfully deleted credentials for user [{}]", userId);
    }

    private void checkUserId(String userId) throws InvalidArgumentException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(validUserId());
    }

    private void checkPassword(String encryptedPassword) throws InvalidArgumentException
    {
        checkThat(encryptedPassword)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString())
            .is(stringWithLengthGreaterThan(1));
    }
    
    private ResultSet tryToExecute(Statement statement, String errorMessage) throws OperationFailedException
    {
        try
        {
            return cassandra.execute(statement);
        }
        catch (Exception ex)
        {
            LOG.error(errorMessage, ex);
            throw new OperationFailedException(errorMessage + ": " + ex.getMessage());
        }
    }
    
    private Statement createInsertStatementFor(String userId, String encryptedPassword)
    {
        UUID userUuid = UUID.fromString(userId);
        
        return QueryBuilder
            .insertInto(Credentials.TABLE_NAME)
            .value(Credentials.USER_ID, userUuid)
            .value(Credentials.TIME_CREATED, new Date())
            .value(Credentials.ENCRYPTED_PASSWORD, encryptedPassword);
    }


    private Statement createQueryToCheckIfExists(String userId)
    {
        UUID userUuid = UUID.fromString(userId);
        
        return QueryBuilder
            .select()
            .countAll()
            .from(Credentials.TABLE_NAME)
            .where(eq(Credentials.USER_ID, userUuid));
    }

    private void checkResultExist(ResultSet results) throws OperationFailedException
    {
        checkThat(results)
            .throwing(OperationFailedException.class)
            .is(notNull());
    }

    private void ensureRowNotEmpty(Row row) throws DoesNotExistException
    {
        checkThat(row)
            .throwing(DoesNotExistException.class)
            .is(notNull());
    }

    private Statement createQueryToGetEncryptedPasswordFor(String userId)
    {
        UUID userUuid = UUID.fromString(userId);
        
        return QueryBuilder
            .select(Credentials.ENCRYPTED_PASSWORD)
            .from(Credentials.TABLE_NAME)
            .where(eq(Credentials.USER_ID, userUuid));
    }

    private void ensureNotEmpty(String encryptedPassword) throws DoesNotExistException
    {
        checkThat(encryptedPassword)
            .throwing(DoesNotExistException.class)
            .usingMessage("Password is missing or invalid")
            .is(nonEmptyString());
    }

    private Statement createStatementToDeleteCredentialsFor(String userId)
    {
        UUID userUuid = UUID.fromString(userId);
        
        return QueryBuilder
            .delete()
            .all()
            .from(Credentials.TABLE_NAME)
            .where(eq(Credentials.USER_ID, userUuid));
    }

}
