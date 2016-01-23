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
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.data.TokenRepository;
import tech.aroma.banana.data.cassandra.Tables.Tokens;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static tech.aroma.banana.data.assertions.AuthenticationAssertions.completeToken;
import static tech.aroma.banana.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.banana.data.cassandra.Tables.Tokens.ORG_ID;
import static tech.aroma.banana.data.cassandra.Tables.Tokens.OWNER_ID;
import static tech.aroma.banana.data.cassandra.Tables.Tokens.OWNER_NAME;
import static tech.aroma.banana.data.cassandra.Tables.Tokens.TIME_OF_CREATION;
import static tech.aroma.banana.data.cassandra.Tables.Tokens.TIME_OF_EXPIRATION;
import static tech.aroma.banana.data.cassandra.Tables.Tokens.TOKEN_ID;
import static tech.aroma.banana.data.cassandra.Tables.Tokens.TOKEN_TYPE;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
 * @author SirWellington
 */
final class CassandraTokenRepository implements TokenRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(CassandraTokenRepository.class);

    private final Session cassandra;
    private final QueryBuilder queryBuilder;
    private final Function<Row, AuthenticationToken> tokenMapper;

    @Inject
    CassandraTokenRepository(Session cassandra,
                             QueryBuilder queryBuilder,
                             Function<Row, AuthenticationToken> tokenMapper)
    {
        checkThat(cassandra, queryBuilder, tokenMapper)
            .are(notNull());

        this.cassandra = cassandra;
        this.queryBuilder = queryBuilder;
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
            .is(nonEmptyString());

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

        return queryBuilder
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
            .usingMessage("token does not exist")
            .is(notNull());

        return row;
    }

    private Statement createQueryToGetToken(String tokenId)
    {
        UUID tokenUuid = UUID.fromString(tokenId);

        return queryBuilder
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

    private Statement createStatementToInsert(AuthenticationToken token)
    {
        UUID tokenId = UUID.fromString(token.tokenId);
        UUID ownerId = UUID.fromString(token.ownerId);
        UUID orgId = null;

        if (!isNullOrEmpty(token.organizationId))
        {
            orgId = UUID.fromString(token.organizationId);
        }

        BatchStatement batch = new BatchStatement();

        Statement insertIntoMainTable = queryBuilder
            .insertInto(Tokens.TABLE_NAME)
            .value(TOKEN_ID, tokenId)
            .value(OWNER_ID, ownerId)
            .value(ORG_ID, orgId)
            .value(OWNER_NAME, token.ownerName)
            .value(TIME_OF_EXPIRATION, token.timeOfExpiration)
            .value(TIME_OF_CREATION, token.timeOfCreation)
            .value(TOKEN_TYPE, token.tokenType);

        batch.add(insertIntoMainTable);

        Statement insertIntoOwnersTable = queryBuilder
            .insertInto(Tokens.TABLE_NAME_BY_OWNER)
            .value(OWNER_ID, ownerId)
            .value(TOKEN_ID, tokenId)
            .value(ORG_ID, orgId)
            .value(OWNER_NAME, token.ownerName)
            .value(TIME_OF_EXPIRATION, token.timeOfExpiration)
            .value(TIME_OF_CREATION, token.timeOfCreation)
            .value(TOKEN_TYPE, token.tokenType);

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

        return queryBuilder
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

        Statement deleteFromMainTable = queryBuilder
            .delete()
            .all()
            .from(Tokens.TABLE_NAME)
            .where(eq(TOKEN_ID, tokenUuid));
        
        batch.add(deleteFromMainTable);

        Statement deleteFromOwnersTable = queryBuilder
            .delete()
            .all()
            .from(Tokens.TABLE_NAME_BY_OWNER)
            .where(eq(OWNER_ID, ownerUuid));

        batch.add(deleteFromOwnersTable);
        
        return batch;
    }

}
