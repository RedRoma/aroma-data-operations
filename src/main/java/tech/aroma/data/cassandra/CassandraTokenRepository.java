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
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.TokenRepository;
import tech.aroma.data.cassandra.Tables.Tokens;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.exceptions.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static tech.aroma.data.assertions.AuthenticationAssertions.completeToken;
import static tech.aroma.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.data.cassandra.Tables.Tokens.*;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 *
 * @author SirWellington
 */
final class CassandraTokenRepository implements TokenRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(CassandraTokenRepository.class);

    private final Session cassandra;
    private final Function<Row, AuthenticationToken> tokenMapper;

    @Inject
    CassandraTokenRepository(Session cassandra,
                             Function<Row, AuthenticationToken> tokenMapper)
    {
        checkThat(cassandra, tokenMapper)
            .are(notNull());

        this.cassandra = cassandra;
        this.tokenMapper = tokenMapper;
    }

    @Override
    public boolean containsToken(String tokenId) throws TException
    {
        checkTokenId(tokenId);

        Statement query = createStatementToCheckIfExists(tokenId);

        Row row = tryToGetOneRowFrom(query);

        long count = row.getLong(0);
        return count > 0;
    }

    @Override
    public AuthenticationToken getToken(String tokenId) throws TException, InvalidTokenException
    {
        checkTokenId(tokenId);

        Statement query = createQueryToGetToken(tokenId);

        Row row = tryToGetOneRowFrom(query);

        AuthenticationToken token = tryToConvertRowToToken(row);

        return token;
    }

    @Override
    public void saveToken(AuthenticationToken token) throws TException
    {
        checkThat(token)
            .throwing(InvalidArgumentException.class)
            .is(completeToken());

        Statement insertStatement = createStatementToInsert(token);

        tryToExecute(insertStatement);
        LOG.debug("Token saved in Cassandra");
    }

    @Override
    public List<AuthenticationToken> getTokensBelongingTo(String ownerId) throws TException
    {
        checkThat(ownerId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("ownerId missing")
            .is(nonEmptyString())
            .usingMessage("ownerId must be a UUID type")
            .is(validUUID());

        Statement query = createQueryToGetTokensOwnedBy(ownerId);

        ResultSet results = tryToGetResultSetFrom(query);

        List<AuthenticationToken> tokens = Lists.create();

        for (Row row : results)
        {
            AuthenticationToken token = tryToConvertRowToToken(row);
            tokens.add(token);
        }

        LOG.debug("Found {} tokens owned by {}", tokens.size(), ownerId);
        return tokens;
    }

    @Override
    public void deleteToken(String tokenId) throws TException
    {
        checkTokenId(tokenId);

        Statement deleteStatement = createStatementToDeleteToken(tokenId);

        tryToExecute(deleteStatement);

        LOG.debug("Successfully deleted Token {}", tokenId);
    }

    private void checkTokenId(String tokenId) throws InvalidArgumentException
    {
        checkThat(tokenId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("missing tokenId")
            .is(nonEmptyString())
            .usingMessage("tokenId must be a UUID")
            .is(validUUID());
    }

    private Statement createStatementToCheckIfExists(String tokenId)
    {
        UUID tokenUuid = UUID.fromString(tokenId);

        return QueryBuilder
            .select()
            .countAll()
            .from(Tokens.TABLE_NAME)
            .where(eq(TOKEN_ID, tokenUuid));
    }

    private Row tryToGetOneRowFrom(Statement query) throws InvalidTokenException, OperationFailedException
    {
        ResultSet results = tryToGetResultSetFrom(query);

        Row row = results.one();

        checkThat(row)
            .throwing(InvalidTokenException.class)
            .usingMessage("Token does not exist")
            .is(notNull());

        return row;
    }

    private Statement createQueryToGetToken(String tokenId)
    {
        UUID tokenUuid = UUID.fromString(tokenId);

        return QueryBuilder
            .select()
            .all()
            .from(Tokens.TABLE_NAME)
            .where(eq(TOKEN_ID, tokenUuid));
    }

    private ResultSet tryToGetResultSetFrom(Statement statment) throws OperationFailedException
    {
        ResultSet results;
        try
        {
            results = cassandra.execute(statment);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to execute Cassandra statement: {}", statment, ex);
            throw new OperationFailedException("Could not query for Token:" + ex.getMessage());
        }
        
        checkThat(results)
            .usingMessage("unexpected null result from cassandra")
            .throwing(OperationFailedException.class)
            .is(notNull());
        
        return results;
    }

    private AuthenticationToken tryToConvertRowToToken(Row row) throws OperationFailedException, InvalidTokenException
    {
        AuthenticationToken token;

        try
        {
            token = tokenMapper.apply(row);
        }
        catch (Exception ex)
        {
            LOG.error("Could not map Row {} to Token", row, ex);
            throw new OperationFailedException("Failed to query for Token: " + ex.getMessage());
        }

        checkThat(token)
            .throwing(InvalidTokenException.class)
            .is(completeToken());

        return token;
    }

    private Statement createStatementToInsert(AuthenticationToken token) throws InvalidArgumentException
    {
        //UUIDs
        UUID tokenId = UUID.fromString(token.tokenId);
        UUID ownerId = UUID.fromString(token.ownerId);
        UUID orgId = null;
        
        //Enums
        String tokenType = null;
        if (token.tokenType != null)
        {
            tokenType = token.tokenType.toString();
        }
        
        String tokenStatus = null;
        if (token.status != null)
        {
            tokenStatus = token.status.toString();
        }
        
        if (!isNullOrEmpty(token.organizationId))
        {
            checkThat(token.organizationId)
                .usingMessage("token organizationId must be a UUID type")
                .throwing(InvalidArgumentException.class)
                .is(validUUID());
            
            orgId = UUID.fromString(token.organizationId);
        }

        BatchStatement batch = new BatchStatement();

        Statement insertIntoMainTable = QueryBuilder
            .insertInto(Tokens.TABLE_NAME)
            .value(TOKEN_ID, tokenId)
            .value(OWNER_ID, ownerId)
            .value(ORG_ID, orgId)
            .value(OWNER_NAME, token.ownerName)
            .value(TIME_OF_EXPIRATION, token.timeOfExpiration)
            .value(TIME_OF_CREATION, token.timeOfCreation)
            .value(TOKEN_TYPE, tokenType)
            .value(TOKEN_STATUS, tokenStatus);

        batch.add(insertIntoMainTable);

        Statement insertIntoOwnersTable = QueryBuilder
            .insertInto(Tokens.TABLE_NAME_BY_OWNER)
            .value(OWNER_ID, ownerId)
            .value(TOKEN_ID, tokenId)
            .value(ORG_ID, orgId)
            .value(OWNER_NAME, token.ownerName)
            .value(TIME_OF_EXPIRATION, token.timeOfExpiration)
            .value(TIME_OF_CREATION, token.timeOfCreation)
            .value(TOKEN_TYPE, tokenType)
            .value(TOKEN_STATUS, tokenStatus);

        batch.add(insertIntoOwnersTable);

        return batch;
    }

    private void tryToExecute(Statement statement) throws OperationFailedException
    {
        try
        {
            cassandra.execute(statement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to execute CQL Statement: {}", statement, ex);
            throw new OperationFailedException("Could not perform operation: " + ex.getMessage());
        }
    }

    private Statement createQueryToGetTokensOwnedBy(String ownerId)
    {
        UUID ownerUuid = UUID.fromString(ownerId);

        return QueryBuilder
            .select()
            .all()
            .from(Tokens.TABLE_NAME_BY_OWNER)
            .where(eq(OWNER_ID, ownerUuid))
            .limit(1000);
    }

    private Statement createStatementToDeleteToken(String tokenId) throws TException
    {
        UUID tokenUuid = UUID.fromString(tokenId);

        //Need to get Token first
        AuthenticationToken token = this.getToken(tokenId);

        UUID ownerUuid = UUID.fromString(token.ownerId);

        BatchStatement batch = new BatchStatement();

        Statement deleteFromMainTable = QueryBuilder
            .delete()
            .all()
            .from(Tokens.TABLE_NAME)
            .where(eq(TOKEN_ID, tokenUuid));
        
        batch.add(deleteFromMainTable);

        Statement deleteFromOwnersTable = QueryBuilder
            .delete()
            .all()
            .from(Tokens.TABLE_NAME_BY_OWNER)
            .where(eq(OWNER_ID, ownerUuid));

        batch.add(deleteFromOwnersTable);
        
        return batch;
    }

}
