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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ReconnectionPolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class ModuleCassandraDevClusterTest
{

    private ModuleCassandraDevCluster instance;

    @Before
    public void setUp()
    {
        instance = new ModuleCassandraDevCluster();
    }

    @Test
    public void testModule()
    {
        Injector injector = Guice.createInjector(instance);

        Cluster cluster = injector.getInstance(Cluster.class);
        assertThat(cluster, notNullValue());

        Session session = injector.getInstance(Session.class);
        assertThat(session, notNullValue());

        QueryBuilder queryBuilder = injector.getInstance(QueryBuilder.class);
        assertThat(queryBuilder, notNullValue());
    }

    @Test
    public void testProvideCassandraCluster()
    {
        ReconnectionPolicy policy = instance.provideReconnectPolicy();
        Cluster cluster = instance.provideCassandraCluster(policy);
        assertThat(cluster, notNullValue());
    }

    @Test
    public void testProvideCassandraSession()
    {
        ReconnectionPolicy policy = instance.provideReconnectPolicy();
        Cluster cluster = instance.provideCassandraCluster(policy);

        Session session = instance.provideCassandraSession(cluster);
        assertThat(session, notNullValue());
    }

    @Test
    public void testProvideCQLBuilder()
    {
        ReconnectionPolicy policy = instance.provideReconnectPolicy();
        Cluster cluster = instance.provideCassandraCluster(policy);

        QueryBuilder queryBuilder = instance.provideCQLBuilder(cluster);
        assertThat(queryBuilder, notNullValue());
    }

    @Test
    public void testProvideReconnectPolicy()
    {
        ReconnectionPolicy result = instance.provideReconnectPolicy();
        assertThat(result, notNullValue());
    }

}
