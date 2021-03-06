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

import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ExponentialReconnectionPolicy;
import com.datastax.driver.core.policies.ReconnectionPolicy;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * Provides bindings that connect to a Development Cassandra Cluster intended 
 * for use with Development and Testing purposes.
 *
 * @author SirWellington
 */
public final class ModuleCassandraDevCluster extends AbstractModule
{

    private final static Logger LOG = LoggerFactory.getLogger(ModuleCassandraDevCluster.class);

    @Override
    protected void configure()
    {
    }
    
    @Provides
    ReconnectionPolicy provideReconnectPolicy()
    {
        long baseAttempt = TimeUnit.SECONDS.toMillis(5);
        long maxTimeWaiting = TimeUnit.MINUTES.toMillis(1);
        
        ExponentialReconnectionPolicy policy = new ExponentialReconnectionPolicy(baseAttempt, maxTimeWaiting);
        return policy;
    }

    @Provides
    @Singleton
    Cluster provideCassandraCluster(ReconnectionPolicy reconnectionPolicy)
    {
        return Cluster.builder()
            .addContactPoint("cassandra-02.dev.redroma.tech")
            .withPort(9042)
            .withCredentials("cassandra", "NvrCXg300Utn1TBzi5Q9W550I4BeGt")
            .withReconnectionPolicy(reconnectionPolicy)
            .build();
    }

    @Provides
    @Singleton
    Session provideCassandraSession(@Required Cluster cluster)
    {
        checkThat(cluster).is(notNull());

        return cluster.connect("Aroma");
    }

}
