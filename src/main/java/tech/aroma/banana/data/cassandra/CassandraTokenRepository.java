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


import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.List;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.data.TokenRepository;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;

import static tech.aroma.banana.data.assertions.AuthenticationAssertions.completeToken;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
final class CassandraTokenRepository implements TokenRepository
{
    private final static Logger LOG = LoggerFactory.getLogger(CassandraTokenRepository.class);
    
    private final Session cassandra;
    private final QueryBuilder queryBuilder;
    private final Function<Row, AuthenticationToken> authenticaionMapper;

    @Inject
    CassandraTokenRepository(Session cassandra,
                             QueryBuilder queryBuilder,
                             Function<Row, AuthenticationToken> authenticaionMapper)
    {
        checkThat(cassandra, queryBuilder, authenticaionMapper)
            .are(notNull());
        
        this.cassandra = cassandra;
        this.queryBuilder = queryBuilder;
        this.authenticaionMapper = authenticaionMapper;
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
        
        for(Row row : results)
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void checkTokenId(String tokenId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Statement createStatementToCheckIfExists(String tokenId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Row tryToGetOneRowFrom(Statement query)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Statement createQueryToGetToken(String tokenId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private ResultSet tryToGetResultSetFrom(Statement query)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private AuthenticationToken tryToConvertRowToToken(Row row)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Statement createStatementToInsert(AuthenticationToken token)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void tryToExecute(Statement insertStatement)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Statement createQueryToGetTokensOwnedBy(String ownerId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
