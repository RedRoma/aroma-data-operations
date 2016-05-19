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


import com.datastax.driver.core.Row;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.cassandra.Tables.Activity;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Dimension;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.ImageType;
import tech.aroma.thrift.Industry;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.Organization;
import tech.aroma.thrift.ProgrammingLanguage;
import tech.aroma.thrift.Role;
import tech.aroma.thrift.Tier;
import tech.aroma.thrift.Urgency;
import tech.aroma.thrift.User;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.TokenType;
import tech.aroma.thrift.channels.MobileDevice;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.thrift.ThriftObjects;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static tech.aroma.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.data.cassandra.Tables.Applications.APP_ID;
import static tech.aroma.data.cassandra.Tables.Messages.MESSAGE_ID;

/**
 * This class contains a lot of data-marshalling logic to transform
 * Cassandra Rows into the appropriate Aroma Thrift Objects.
 * 
 * @author SirWellington
 */
@Internal
@NonInstantiable
final class Mappers
{
    private final static Logger LOG = LoggerFactory.getLogger(Mappers.class);

    Mappers() throws IllegalAccessException
    {
        throw new IllegalAccessException("cannot instantiate");
    }
    
    
    //==========================================================
    // APP MAPPER
    //==========================================================
    
    static Function<Row, Application> appMapper()
    {
        return row ->
        {
            Application app = new Application();
            
            //App ID
            UUID appId = row.getUUID(Tables.Applications.APP_ID);
            if (appId != null)
            {
                app.setApplicationId(appId.toString());
            }
            
            //App Name
            app.setName(row.getString(Tables.Applications.APP_NAME));
            
            //App Description
            if (doesRowContainColumn(row, Tables.Applications.APP_DESCRIPTION))
            {
                app.setApplicationDescription(row.getString(Tables.Applications.APP_DESCRIPTION));
            }
            
            //Media ID
            if (doesRowContainColumn(row, Tables.Applications.ICON_MEDIA_ID))
            {
                UUID iconMediaId = row.getUUID(Tables.Applications.ICON_MEDIA_ID);
                if (iconMediaId != null)
                {
                    app.setApplicationIconMediaId(iconMediaId.toString());
                }
            }

            //Programming Language
            if (doesRowContainColumn(row, Tables.Applications.PROGRAMMING_LANGUAGE))
            {
                String programmingLanguage = row.getString(Tables.Applications.PROGRAMMING_LANGUAGE);
                if (!isNullOrEmpty(programmingLanguage))
                {
                    ProgrammingLanguage language = ProgrammingLanguage.valueOf(programmingLanguage);
                    app.setProgrammingLanguage(language);
                }
            }

            //Time Provisioned
            if (doesRowContainColumn(row, Tables.Applications.TIME_PROVISIONED))
            {
                Date timeOfProvisioning = row.getTimestamp(Tables.Applications.TIME_PROVISIONED);
                if (timeOfProvisioning != null)
                {
                    app.setTimeOfProvisioning(timeOfProvisioning.getTime());
                }
            }

            //Token Expiration
            if (doesRowContainColumn(row, Tables.Applications.TIME_OF_TOKEN_EXPIRATION))
            {
                Date tokenExpiration = row.getTimestamp(Tables.Applications.TIME_OF_TOKEN_EXPIRATION);
                if (tokenExpiration != null)
                {
                    app.setTimeOfTokenExpiration(tokenExpiration.getTime());
                }
            }

            //Owners
            if (doesRowContainColumn(row, Tables.Applications.OWNERS))
            {
                //Transform the UUIDs to Strings
                Set<String> owners = row.getSet(Tables.Applications.OWNERS, UUID.class)
                    .stream()
                    .map(UUID::toString)
                    .collect(Collectors.toSet());

                app.setOwners(owners);
            }

            //ORG ID
            if (doesRowContainColumn(row, Tables.Applications.ORG_ID))
            {
                UUID orgId = row.getUUID(Tables.Applications.ORG_ID);
                if (orgId != null)
                {
                    app.setOrganizationId(orgId.toString());
                }
            }

            //Tier
            if (doesRowContainColumn(row, Tables.Applications.TIER))
            {
                String tier = row.getString(Tables.Applications.TIER);
                
                if (!isNullOrEmpty(tier))
                {
                    app.setTier(Tier.valueOf(tier));
                }
            }

            return app;
        };
    }
    

    //==========================================================
    // EVENT MAPPER
    //==========================================================

    static Function<Row, Event> eventMapper()
    {
        return row ->
        {
            Event event = new Event();
            
            //UUIDs
            UUID eventId = row.getUUID(Activity.EVENT_ID);
            UUID appId = row.getUUID(Activity.APP_ID);
            UUID actorId = row.getUUID(Activity.ACTOR_ID);
            
            //Serialized Event
            String serializedEvent = row.getString(Activity.SERIALIZED_EVENT);
            
            //Event ID
            if (eventId != null)
            {
                event.setEventId(eventId.toString());
            }
            
            //App ID
            if (appId != null)
            {
                event.setApplicationId(appId.toString());
            }
            
            //Actor ID
            if (actorId != null)
            {
                event.setUserIdOfActor(actorId.toString());
            }
            
            //Time of Event
            Date timeOfEvent = row.getTimestamp(Activity.TIME_OF_EVENT);
            if (timeOfEvent != null)
            {
                event.setTimestamp(timeOfEvent.getTime());
            }

            //Serialized Event
            if (!isNullOrEmpty(serializedEvent))
            {
                try
                {
                    event = ThriftObjects.fromJson(event, serializedEvent);
                }
                catch (TException ex)
                {
                    LOG.warn("Failed to deserialize {} as an Event", serializedEvent, ex);
                }
            }

            return event;
            
        };
    }
    
    //==========================================================
    // IMAGE MAPPER
    //==========================================================

    static Function<Row, Image> imageMapper()
    {
        return row ->
        {
            
            Image image = new Image();
            
            //Binary
            ByteBuffer binary = row.getBytes(Tables.Media.BINARY);
            image.setData(binary);
            
            //Dimensions
            int width = row.getInt(Tables.Media.WIDTH);
            int height = row.getInt(Tables.Media.HEIGHT);
            image.setDimension(new Dimension(width, height));
            
            //Media Type
            String mediaType = row.getString(Tables.Media.MEDIA_TYPE);
            
            try
            {
                image.setImageType(ImageType.valueOf(mediaType));
            }
            catch (Exception ex)
            {
                LOG.warn("Could not find ImageType: {}", mediaType, ex);
            }
            
            return image;
        };
    }

    //==========================================================
    // MESSAGE MAPPER
    //==========================================================

    static Function<Row, Message> messageMapper()
    {
        return row ->
        {
            Message message = new Message();
            
            //UUIDs
            UUID msgId = row.getUUID(MESSAGE_ID);
            UUID appId = row.getUUID(APP_ID);
            
            message.setMessageId(msgId.toString())
                .setApplicationId(appId.toString())
                .setTitle(row.getString(Tables.Messages.TITLE))
                .setDeviceName(row.getString(Tables.Messages.DEVICE_NAME))
                .setHostname(row.getString(Tables.Messages.HOSTNAME))
                .setMacAddress(row.getString(Tables.Messages.MAC_ADDRESS))
                .setBody(row.getString(Tables.Messages.BODY))
                .setApplicationName(row.getString(Tables.Messages.APP_NAME));
            
            //Time Created & Received
            Date timeCreated = row.getTimestamp(Tables.Messages.TIME_CREATED);
            Date timeReceived = row.getTimestamp(Tables.Messages.TIME_RECEIVED);
            
            if (timeCreated != null)
            {
                message.setTimeOfCreation(timeCreated.getTime());
            }
            
            if (timeReceived != null)
            {
                message.setTimeMessageReceived(timeReceived.getTime());
            }

            //Urgency
            String urgency = row.getString(Tables.Messages.URGENCY);
            if (!isNullOrEmpty(urgency))
            {
                message.setUrgency(Urgency.valueOf(urgency));
            }
            
            return message;
        };
    }
    
    //==========================================================
    // ORG MAPPER
    //==========================================================
    static Function<Row, Organization> orgMapper()
    {
        return row ->
        {
            Organization org = new Organization();
            
            //Org ID
            UUID orgUuid = row.getUUID(Tables.Organizations.ORG_ID);
            
            //Owners
            List<String> owners = Lists.create();
            if (doesRowContainColumn(row, Tables.Organizations.OWNERS))
            {
                Set<UUID> ownerIds = row.getSet(Tables.Organizations.OWNERS, UUID.class);
                owners = ownerIds.stream()
                    .map(UUID::toString)
                    .collect(toList());
            }

            //Tier
            String tier = row.getString(Tables.Organizations.TIER);
            if (!isNullOrEmpty(tier))
            {
                org.setTier(Tier.valueOf(tier));
            }

            //Industry
            String industry = row.getString(Tables.Organizations.INDUSTRY);
            if (!isNullOrEmpty(industry))
            {
                org.setIndustry(Industry.valueOf(industry));
            }
            
            //Other Info
            org.setOrganizationId(orgUuid.toString())
                .setOrganizationName(row.getString(Tables.Organizations.ORG_NAME))
                .setLogoLink(row.getString(Tables.Organizations.ICON_LINK))
                .setOrganizationDescription(row.getString(Tables.Organizations.DESCRIPTION))
                .setGithubProfile(row.getString(Tables.Organizations.GITHUB_PROFILE))
                .setOrganizationEmail(row.getString(Tables.Organizations.EMAIL))
                .setStockMarketSymbol(row.getString(Tables.Organizations.STOCK_NAME))
                .setWebsite(row.getString(Tables.Organizations.WEBSITE))
                .setOwners(owners);
            
            return org;
        };
    }

    //==========================================================
    // TOKEN MAPPER
    //==========================================================
    static Function<Row, AuthenticationToken> tokenMapper()
    {
        return row ->
        {
            AuthenticationToken token = new AuthenticationToken();
            
            //Time of Creation
            Date timeOfCreation = row.getTimestamp(Tables.Tokens.TIME_OF_CREATION);
            if (timeOfCreation != null)
            {
                token.setTimeOfCreation(timeOfCreation.getTime());
            }
            
            //Time of Expiration
            Date timeOfExpiration = row.getTimestamp(Tables.Tokens.TIME_OF_EXPIRATION);
            if (timeOfExpiration != null)
            {
                token.setTimeOfExpiration(timeOfExpiration.getTime());
            }
            
            //Org ID
            String orgId = null;
            if(doesRowContainColumn(row, Tables.Tokens.ORG_ID))
            {
                UUID orgUuid = row.getUUID(Tables.Tokens.ORG_ID);
                orgId = orgUuid != null ? orgUuid.toString() : orgId;
            }
            
            //Token Type
            String tokenType = row.getString(Tables.Tokens.TOKEN_TYPE);
            if (!isNullOrEmpty(tokenType))
            {
                token.setTokenType(TokenType.valueOf(tokenType));
            }
            
            token
                .setTokenId(row.getUUID(Tables.Tokens.TOKEN_ID).toString())
                .setOwnerId(row.getUUID(Tables.Tokens.OWNER_ID).toString())
                .setOrganizationId(orgId)
                .setOwnerName(row.getString(Tables.Tokens.OWNER_NAME));
            
            return token;
        };
    }
  
    //==========================================================
    // REACTIONS MAPPER
    //==========================================================

    static Function<Row, List<Reaction>> reactionsMapper()
    {
        return row ->
        {
            List<Reaction> reactions = Lists.create();
            
            if (!doesRowContainColumn(row, Tables.Reactions.SERIALIZED_REACTIONS))
            {
                return reactions;
            }
            
            List<String> serializedReactions = row.getList(Tables.Reactions.SERIALIZED_REACTIONS, String.class);
            
            return serializedReactions.stream()
                .map(Mappers::deserializeReaction)
                .filter(Objects::nonNull)
                .collect(toList());
            
        };
    }
    
    static Reaction deserializeReaction(String json)
    {
        try
        {
            return ThriftObjects.fromJson(new Reaction(), json);
        }
        catch (TException ex)
        {
            LOG.warn("Failed to deserialize Reaction: {}", json, ex);
            return null;
        }
    }

    //==========================================================
    // USERS MAPPER
    //==========================================================
    static Function<Row, User> userMapper()
    {
        return row ->
        {
            User user = new User();
            
            String email = null;
            
            if (doesRowContainColumn(row, Tables.Users.EMAIL))
            {
                email = row.getString(Tables.Users.EMAIL);
            }
            else if (doesRowContainColumn(row, Tables.Users.EMAILS))
            {
                Set<String> emails = row.getSet(Tables.Users.EMAILS, String.class);
                email = emails.stream().findFirst().orElse(null);
            }
            
            if (doesRowContainColumn(row, Tables.Users.BIRTH_DATE))
            {
                Date birthDate = row.getTimestamp(Tables.Users.BIRTH_DATE);
                if (birthDate != null)
                {
                    user.setBirthdate(birthDate.getTime());
                }
            }
            
            Set<Role> roles = Sets.create();

            if (doesRowContainColumn(row, Tables.Users.ROLES))
            {
                Set<String> set = row.getSet(Tables.Users.ROLES, String.class);
                roles = Sets.nullToEmpty(set)
                    .stream()
                    .map(Role::valueOf)
                    .collect(toSet());
            }

            if(doesRowContainColumn(row, Tables.Users.PROFILE_IMAGE_ID))
            {
                String profileImageLink = row.getString(Tables.Users.PROFILE_IMAGE_ID);
                user.setProfileImageLink(profileImageLink);
            }
            
            if (doesRowContainColumn(row, Tables.Users.GITHUB_PROFILE))
            {
                String githubProfile = row.getString(Tables.Users.GITHUB_PROFILE);
                user.setGithubProfile(githubProfile);
            }
            
            if(doesRowContainColumn(row, Tables.Users.TIME_ACCOUNT_CREATED))
            {
                Date timeCreated = row.getTimestamp(Tables.Users.TIME_ACCOUNT_CREATED);
                if(timeCreated != null)
                {
                    user.setTimeUserJoined(timeCreated.getTime());
                }
            }
            
            if (doesRowContainColumn(row, Tables.Users.FIRST_NAME))
            {
                user.setFirstName(row.getString(Tables.Users.FIRST_NAME));
            }

            if (doesRowContainColumn(row, Tables.Users.MIDDLE_NAME))
            {
                user.setMiddleName(row.getString(Tables.Users.MIDDLE_NAME));
            }

            if (doesRowContainColumn(row, Tables.Users.LAST_NAME))
            {
                user.setLastName(row.getString(Tables.Users.LAST_NAME));
            }
            
            return user
                .setUserId(row.getUUID(Tables.Users.USER_ID).toString())
                .setEmail(email)
                .setRoles(roles);
        };
    }
    
    
    //==========================================================
    // USER PREFERENCES MAPPER
    //==========================================================
    
    /**
     * Extracts {@linkplain MobileDevice Mobile Devices} from a {@linkplain Tables.UserPreferences User Preferences Row}.
     * 
     * @return 
     */
    static Function<Row, Set<MobileDevice>> mobileDeviceMapper()
    {
        return row ->
        {
            if (doesRowContainColumn(row, Tables.UserPreferences.SERIALIZED_DEVICES))
            {
                Set<String> serializedDevices = row.getSet(Tables.UserPreferences.SERIALIZED_DEVICES, String.class);
                
                return Sets.nullToEmpty(serializedDevices)
                    .stream()
                    .map(Mappers::deserializeDevice)
                    .filter(Objects::nonNull)
                    .collect(toSet());
            }
            
            return Sets.emptySet();
        };
    }

    private static MobileDevice deserializeDevice(String serializedDevice)
    {
        try
        {
            return ThriftObjects.fromJson(new MobileDevice(), serializedDevice);
        }
        catch(Exception ex)
        {
            LOG.error("Failed to Deserialized Device {}", serializedDevice, ex);
            //Nulls will be filtered out in the Functional Operations.
            return null;
        }
    }

    //==========================================================
    // UTILITY FUNCTIONS
    //==========================================================

    private static boolean doesRowContainColumn(Row row, String column)
    {
        try
        {
            row.isNull(column);
            //No exception means it's there
            return true;
        }
        catch(Exception ex)
        {
            //Exception means it is not definied.
            return false;
        }
    }
}
