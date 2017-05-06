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

import com.datastax.driver.core.Row;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.cassandra.Tables.Activity;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.Organization;
import tech.aroma.thrift.User;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.channels.MobileDevice;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;
import tech.sirwellington.alchemy.thrift.ThriftObjects;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.ChannelGenerators.mobileDevices;
import static tech.aroma.thrift.generators.EventGenerators.events;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
import static tech.aroma.thrift.generators.TokenGenerators.authenticationTokens;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MappersTest
{
    @GenerateString(GenerateString.Type.UUID)
    private String userId;

    @Mock
    private Row row;
    
    private AuthenticationToken token;

    @GeneratePojo
    private Application app;

    private MobileDevice device;
    
    @GeneratePojo
    private Message message;

    @GeneratePojo
    private User user;

    @GeneratePojo
    private Organization org;
    
    private Event event;
    
    private Reaction reaction;
    
    private List<Reaction> reactions;
    
    private List<String> serializedReactions;

    @Before
    public void setUp()
    {
        user.userId = userId;
        
        device = one(mobileDevices());
        
        event = one(events());
        reaction = one(reactions());
        reactions = listOf(reactions(), 10);
        
        serializedReactions = reactions.parallelStream()
            .map(this::toJson)
            .filter(Objects::nonNull)
            .collect(toList());
        
        token = one(authenticationTokens());
    }
    
    private <T extends TBase> String toJson(T object)
    {
        try
        {
            return ThriftObjects.toJson(object);
        }
        catch (TException ex)
        {
            return null;
        }
    }

    @Test
    @DontRepeat
    public void testCannotInstantiate()
    {
        assertThrows(() -> new Mappers())
            .isInstanceOf(IllegalAccessException.class);
    }

    @Test
    public void testAppMapper()
    {
        Function<Row, Application> mapper = Mappers.appMapper();
        assertThat(mapper, notNullValue());
        
    }

    @Test
    public void testMessageMapper()
    {
        Function<Row, Message> mapper = Mappers.messageMapper();
        assertThat(mapper, notNullValue());
        
    }

    @Test
    public void testOrgMapper()
    {
        Function<Row, Organization> mapper = Mappers.orgMapper();
        assertThat(mapper, notNullValue());
    }

    @Test
    public void testUserMapper()
    {
        Function<Row, User> mapper = Mappers.userMapper();
        assertThat(mapper, notNullValue());
        
        Row userRow = rowFor(user);
        User result = mapper.apply(userRow);
        
        assertThat(result, notNullValue());
        assertThat(result.userId, is(user.userId));
        assertThat(result.firstName, is(user.firstName));
        assertThat(result.middleName, is(user.middleName));
        assertThat(result.lastName, is(user.lastName));
        assertThat(result.birthdate, is(user.birthdate));
    }
    
    private Row rowFor(User user)
    {
        Row userRow = mock(Row.class);
        
        when(userRow.getUUID(Tables.Users.USER_ID)).thenReturn(UUID.fromString(user.userId));
        when(userRow.getString(Tables.Users.FIRST_NAME)).thenReturn(user.firstName);
        when(userRow.getString(Tables.Users.MIDDLE_NAME)).thenReturn(user.middleName);
        when(userRow.getString(Tables.Users.LAST_NAME)).thenReturn(user.lastName);
        when(userRow.getTimestamp(Tables.Users.BIRTH_DATE)).thenReturn(new Date(user.birthdate));
        
        return userRow;
    }

    @Test
    public void testEventMapper() throws TException
    {
        Function<Row, Event> instance = Mappers.eventMapper();
        assertThat(instance, notNullValue());
        
        Row eventRow = rowFor(event);
        Event result = instance.apply(eventRow);
        assertThat(result, is(event));
    }

    private Row rowFor(Event event) throws TException
    {
        Row eventRow = mock(Row.class);
        
        String serializedEvent = ThriftObjects.toJson(event);
        UUID actorId = UUID.fromString(event.userIdOfActor);
        UUID appId = UUID.fromString(event.applicationId);
        UUID eventId = UUID.fromString(event.eventId);
        
        when(eventRow.getUUID(Activity.ACTOR_ID)).thenReturn(actorId);
        when(eventRow.getUUID(Activity.APP_ID)).thenReturn(appId);
        when(eventRow.getUUID(Activity.EVENT_ID)).thenReturn(eventId);
        when(eventRow.getString(Activity.SERIALIZED_EVENT)).thenReturn(serializedEvent);
        
        return eventRow;
    }

    @Test
    public void testReactionsMapper()
    {
        Function<Row, List<Reaction>> mapper = Mappers.reactionsMapper();
        assertThat(mapper, notNullValue());

        when(row.getList(Tables.Reactions.SERIALIZED_REACTIONS, String.class))
            .thenReturn(serializedReactions);
        
        List<Reaction> result = mapper.apply(row);
        assertThat(result, is(reactions));
    }

    @Test
    public void testDeserializeReaction() throws TException
    {
        String json = ThriftObjects.toJson(reaction);
        
        Reaction result = Mappers.deserializeReaction(json);
        assertThat(result, is(reaction));
    }

    @Test
    public void testMobileDeviceMapper() throws TException
    {
        String serializedDevice = ThriftObjects.toJson(device);
        Set<String> expected = Sets.createFrom(serializedDevice);
        
        when(row.getSet(Tables.UserPreferences.SERIALIZED_DEVICES, String.class))
            .thenReturn(expected);
        
        Function<Row, Set<MobileDevice>> mapper = Mappers.mobileDeviceMapper();
        assertThat(mapper, notNullValue());
        
        Set<MobileDevice> result = mapper.apply(row);
        assertThat(result, is(Sets.createFrom(device)));
    }

    
    @Test
    public void testAuthenticationTokenMapper() throws Exception
    {
        Function<Row, AuthenticationToken> mapper = Mappers.tokenMapper();
        assertThat(mapper, notNullValue());
        
        token.unsetOrganizationName();
        Row tokenRow = rowFor(token);
        
        AuthenticationToken result = mapper.apply(tokenRow);
        assertThat(result, is(token));
    }
    
    private Row rowFor(AuthenticationToken token)
    {
        Row tokenRow = mock(Row.class);
        
        when(tokenRow.getTimestamp(Tables.Tokens.TIME_OF_EXPIRATION))
            .thenReturn(new Date(token.timeOfExpiration));
        
        when(tokenRow.getTimestamp(Tables.Tokens.TIME_OF_CREATION))
            .thenReturn(new Date(token.timeOfCreation));
        
        when(tokenRow.getUUID(Tables.Tokens.ORG_ID))
            .thenReturn(UUID.fromString(token.organizationId));
        
        when(tokenRow.getUUID(Tables.Tokens.TOKEN_ID))
            .thenReturn(UUID.fromString(token.tokenId));
        
        when(tokenRow.getString(Tables.Tokens.TOKEN_TYPE))
            .thenReturn(token.tokenType.toString());
        
        when(tokenRow.getString(Tables.Tokens.TOKEN_STATUS))
            .thenReturn(token.status.toString());
        
        when(tokenRow.getUUID(Tables.Tokens.OWNER_ID))
            .thenReturn(UUID.fromString(token.ownerId));
        
        when(tokenRow.getString(Tables.Tokens.OWNER_NAME))
            .thenReturn(token.ownerName);
        
        return tokenRow;
    }

}
