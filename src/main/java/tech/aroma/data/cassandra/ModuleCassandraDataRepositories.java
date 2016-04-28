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
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.CredentialRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.InboxRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.MessageRepository;
import tech.aroma.data.OrganizationRepository;
import tech.aroma.data.ReactionRepository;
import tech.aroma.data.TokenRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.Organization;
import tech.aroma.thrift.User;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.reactions.Reaction;


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
        bind(ActivityRepository.class).to(CassandraActivityRepository.class);
        bind(ApplicationRepository.class).to(CassandraApplicationRepository.class);
        bind(CredentialRepository.class).to(CassandraCredentialsRepository.class);
        bind(FollowerRepository.class).to(CassandraFollowerRepository.class);
        bind(InboxRepository.class).to(CassandraInboxRepository.class);
        bind(MediaRepository.class).to(CassandraMediaRepository.class);
        bind(MessageRepository.class).to(CassandraMessageRepository.class);
        bind(OrganizationRepository.class).to(CassandraOrganizationRepository.class);
        bind(ReactionRepository.class).to(CassandraReactionRepository.class);
        bind(TokenRepository.class).to(CassandraTokenRepository.class);
        bind(UserRepository.class).to(CassandraUserRepository.class);
    }

    @Provides
    Function<Row, Application> provideApplicationMapper()
    {
        return Mappers.appMapper();
    }

    @Provides
    Function<Row, Event> provideEventMapper()
    {
        return Mappers.eventMapper();
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
    Function<Row, List<Reaction>> provideReactionsMapper()
    {
        return Mappers.reactionsMapper();
    }
    
    @Provides
    Function<Row, User> provideUserMapper()
    {
        return Mappers.userMapper();
    }

}
