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
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.CredentialRepository;
import tech.aroma.banana.data.FollowerRepository;
import tech.aroma.banana.data.InboxRepository;
import tech.aroma.banana.data.MediaRepository;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.data.OrganizationRepository;
import tech.aroma.banana.data.TokenRepository;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.Organization;
import tech.aroma.thrift.User;
import tech.aroma.thrift.authentication.AuthenticationToken;


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
        bind(ApplicationRepository.class).to(CassandraApplicationRepository.class).asEagerSingleton();
        bind(CredentialRepository.class).to(CassandraCredentialsRepository.class).asEagerSingleton();
        bind(FollowerRepository.class).to(CassandraFollowerRepository.class).asEagerSingleton();
        bind(InboxRepository.class).to(CassandraInboxRepository.class).asEagerSingleton();
        bind(MediaRepository.class).to(CassandraMediaRepository.class).asEagerSingleton();
        bind(MessageRepository.class).to(CassandraMessageRepository.class).asEagerSingleton();
        bind(OrganizationRepository.class).to(CassandraOrganizationRepository.class).asEagerSingleton();
        bind(TokenRepository.class).to(CassandraTokenRepository.class).asEagerSingleton();
        bind(UserRepository.class).to(CassandraUserRepository.class).asEagerSingleton();
    }

    @Provides
    Function<Row, Application> provideApplicationMapper()
    {
        return Mappers.appMapper();
    }

    @Provides
    Function<Row, Image> provideImageMapper()
    {
        return Mappers.imageMapper();
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
    Function<Row, AuthenticationToken> provideTokenMapper()
    {
        return Mappers.tokenMapper();
    }
    
    @Provides
    Function<Row, User> provideUserMapper()
    {
        return Mappers.userMapper();
    }

}
