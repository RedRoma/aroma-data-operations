/*
 * Copyright 2016 RedRoma, Inc.
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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.ReactionRepository;
import tech.aroma.data.cassandra.Tables.Reactions;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.thrift.ThriftObjects;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.aroma.data.cassandra.Tables.Reactions.OWNER_ID;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class CassandraReactionRepository implements ReactionRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(CassandraReactionRepository.class);

    private final Session cassandra;
    private final Function<Row, List<Reaction>> reactionMapper;

    @Inject
    CassandraReactionRepository(Session cassandra, Function<Row, List<Reaction>> reactionMapper)
    {
        checkThat(cassandra, reactionMapper)
            .are(notNull());
        
        this.cassandra = cassandra;
        this.reactionMapper = reactionMapper;
    }

    @Override
    public void saveReactionsForUser(String userId, List<Reaction> reactions) throws TException
    {
        checkUserId(userId);

        Statement statement;
        if (Lists.isEmpty(reactions))
        {
            statement = createQueryToRemoveReactionsFor(userId);
        }
        else
        {
            statement = createStatementToSaveReactions(userId, reactions);
        }

        tryToExecute(statement, "saveReactionsForUser");
    }

    @Override
    public List<Reaction> getReactionsForUser(String userId) throws TException
    {
        checkUserId(userId);

        Statement query = createQueryToGetReactionsFor(userId);

        ResultSet results = tryToExecute(query, "getReactions");

        if (results.isExhausted())
        {
            return Lists.emptyList();
        }

        Row row = results.one();
        if (row == null)
        {
            return Lists.emptyList();
        }

        return reactionMapper.apply(row);
    }

    @Override
    public void saveReactionsForApplication(String appId, List<Reaction> reactions) throws TException
    {
        checkAppId(appId);

        if (Lists.isEmpty(reactions))
        {
            Statement statement = createQueryToRemoveReactionsFor(appId);
            tryToExecute(statement, "deleteReactionsForApplication");
            return;
        }
        
        Statement statement = createStatementToSaveReactions(appId, reactions);
        tryToExecute(statement, "saveReactionsForApplication");
    }

    @Override
    public List<Reaction> getReactionsForApplication(String appId) throws TException
    {
        checkAppId(appId);

        Statement query = createQueryToGetReactionsFor(appId);

        ResultSet results = tryToExecute(query, "getReactionsForApplication");

        Row row = results.one();

        if (row == null)
        {
            return Lists.emptyList();
        }

        return reactionMapper.apply(row);
    }

    private void checkUserId(String userId) throws InvalidArgumentException
    {
        checkThat(userId).throwing(InvalidArgumentException.class).is(validUserId());
    }

    private void checkAppId(String appId) throws InvalidArgumentException
    {
        checkThat(appId).throwing(InvalidArgumentException.class).is(validApplicationId());
    }

    private ResultSet tryToExecute(Statement statement, String operationName) throws OperationFailedException
    {
        try
        {
            return cassandra.execute(statement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to execute Cassandra Operation: {}", operationName, ex);
            throw new OperationFailedException("Could not execute database operation: " + ex.getMessage());
        }
    }

    private Statement createQueryToRemoveReactionsFor(String ownerId)
    {
        UUID ownerUuid = UUID.fromString(ownerId);

        return QueryBuilder
            .delete().all()
            .from(Reactions.TABLE_NAME)
            .where(eq(OWNER_ID, ownerUuid));
    }

    private Statement createStatementToSaveReactions(String ownerId, List<Reaction> reactions)
    {
        UUID ownerUuid = UUID.fromString(ownerId);

        List<String> serializedReactions = reactions.stream()
            .map(this::serialize)
            .filter(Objects::nonNull)
            .collect(toList());

        return QueryBuilder
            .insertInto(Reactions.TABLE_NAME)
            .value(Reactions.OWNER_ID, ownerUuid)
            .value(Reactions.SERIALIZED_REACTIONS, serializedReactions);
    }

    private Statement createQueryToGetReactionsFor(String ownerId)
    {
        UUID ownerUuid = UUID.fromString(ownerId);

        return QueryBuilder
            .select().all()
            .from(Reactions.TABLE_NAME)
            .where(eq(Reactions.OWNER_ID, ownerUuid));
    }

    private String serialize(Reaction reaction)
    {
        try
        {
            return ThriftObjects.toJson(reaction);
        }
        catch (TException ex)
        {
            LOG.warn("Failed to serialize Object {} ", reaction, ex);
            return null;
        }
    }
}
