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
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.banana.data.UserRepository;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;


/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraDataModuleTest 
{
    
    @Mock(answer = RETURNS_MOCKS)
    private Cluster cluster;
    
    @Mock
    private Session mockSession;
    
    private AbstractModule binder = new AbstractModule()
    {
        @Override
        protected void configure()
        {
        }
        
        @Provides
        Session provideSession()
        {
            return mockSession;
        }
        
        @Provides
        Cluster provideCluster()
        {
            return cluster;
        }
        
    };
    
    private CassandraDataModule instance;

    @Before
    public void setUp()
    {
        instance = new CassandraDataModule();
        
    }

    @Test
    public void testConfigure()
    {
        Injector injector = Guice.createInjector(binder, instance);
        assertThat(injector, notNullValue());
        
        UserRepository userRepo = injector.getInstance(UserRepository.class);
        assertThat(userRepo, notNullValue());
    }

    @Test
    public void testProvideQueryBuilder()
    {
        QueryBuilder result = instance.provideQueryBuilder(cluster);
        assertThat(result, notNullValue());
    }

}