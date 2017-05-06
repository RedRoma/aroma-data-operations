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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;


/**
 * Connection information to connect to a Cassandra Cluster for use in Integration testing.
 *
 * @author SirWellington
 */
@Internal
final class TestCassandraProviders
{

    private final static Logger LOG = LoggerFactory.getLogger(TestCassandraProviders.class);

    private static final Injector INJECTOR = Guice.createInjector(new ModuleCassandraDevCluster());

    static Cluster createTestCluster()
    {
        return INJECTOR.getInstance(Cluster.class);
    }

    static Session getTestSession()
    {
        Cluster cluster = createTestCluster();
        return cluster.connect("Aroma_Tests");
//        return INJECTOR.getInstance(Session.class);
    }
}
