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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;

/**
 * Connection information to connect to a Cassandra Cluster for use in Integration testing.
 *
 * @author SirWellington
 */
@Internal
class TestSessions
{

    private final static Logger LOG = LoggerFactory.getLogger(TestSessions.class);

    static Cluster createTestCluster()
    {
        return Cluster.builder()
            .addContactPoint("cassandra-01.aroma.tech")
            .withPort(9042)
            .withCredentials("cassandra", "cassandra")
            .build();
    }

    static Session createTestSession(Cluster cluster)
    {
        return cluster.connect("Banana");
    }

    static QueryBuilder returnTestQueryBuilder(Cluster cluster)
    {
        return new QueryBuilder(cluster);
    }
}
