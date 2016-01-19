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
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.UserRepository;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
public final class CassandraDataModule extends AbstractModule
{
    private final static Logger LOG = LoggerFactory.getLogger(CassandraDataModule.class);

    @Override
    protected void configure()
    {
        bind(UserRepository.class).to(CassandraUserRepository.class).in(Singleton.class);
    }

    @Provides
    QueryBuilder provideQueryBuilder(Cluster cluster)
    {
        checkThat(cluster).is(notNull());
        
        return new QueryBuilder(cluster);
    }
    
}
