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
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.util.function.Function;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.FollowerRepository;
import tech.aroma.banana.data.InboxRepository;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.data.OrganizationRepository;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.Organization;
import tech.aroma.banana.thrift.User;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

/**
 * Provides the Banana Repositories backed by a Cassandra Cluster.
 * 
 * This Module does not supply an actual Cassandra {@link Cluster} or {@link Session}.
 * 
 * @author SirWellington
 */
public final class ModuleCassandraDataRepositories extends AbstractModule
{

    private final static Logger LOG = LoggerFactory.getLogger(ModuleCassandraDataRepositories.class);

    @Override
    protected void configure()
    {
        bind(ApplicationRepository.class).to(CassandraApplicationRepository.class).in(Singleton.class);
        bind(FollowerRepository.class).to(CassandraFollowerRepository.class).in(Singleton.class);
        bind(InboxRepository.class).to(CassandraInboxRepository.class).in(Singleton.class);
        bind(MessageRepository.class).to(CassandraMessageRepository.class).in(Singleton.class);
        bind(OrganizationRepository.class).to(CassandraOrganizationRepository.class).in(Singleton.class);
        bind(UserRepository.class).to(CassandraUserRepository.class).in(Singleton.class);
    }

    @Provides
    QueryBuilder provideQueryBuilder(Cluster cluster)
    {
        checkThat(cluster).is(notNull());

        return new QueryBuilder(cluster);
    }

    @Provides
    Function<Row, Application> provideApplicationMapper()
    {
        return Mappers.appMapper();
    }

    @Provides
    Function<Row, Message> provideMessageMapper()
    {
        return Mappers.messageMapper();
    }

    @Provides
    Function<Row, Organization> provideOrganizationMapper()
    {
        return Mappers.orgMapper();
    }

    @Provides
    Function<Row, User> provideUserMapper()
    {
        return Mappers.userMapper();
    }

}
