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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.banana.thrift.Application;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.mockito.Answers.RETURNS_MOCKS;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraApplicationRepositoryTest 
{

    @Mock(answer =RETURNS_MOCKS)
    private Cluster cluster;
    
    @Mock(answer = RETURNS_MOCKS)
    private Session session;
    
    private QueryBuilder queryBuilder;
    
    private CassandraApplicationRepository instance;
    
    @GeneratePojo
    private Application app;
    
    @GenerateString(UUID)
    private String appId;
    
    @Before
    public void setUp()
    {
        app.applicationId = appId;
        
        queryBuilder = new QueryBuilder(cluster);
        
        instance = new CassandraApplicationRepository(session, queryBuilder);
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new CassandraApplicationRepository(session, null))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new CassandraApplicationRepository(null, queryBuilder))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSaveApplication() throws Exception
    {
    }

    @Test
    public void testDeleteApplication() throws Exception
    {
    }

    @Test
    public void testGetById() throws Exception
    {
    }

    @Test
    public void testContainsApplication() throws Exception
    {
    }

    @Test
    public void testGetApplicationsOwnedBy() throws Exception
    {
    }

    @Test
    public void testGetApplicationsByOrg() throws Exception
    {
    }

    @Test
    public void testSearchByName() throws Exception
    {
    }

    @Test
    public void testGetRecentlyCreated() throws Exception
    {
    }

}