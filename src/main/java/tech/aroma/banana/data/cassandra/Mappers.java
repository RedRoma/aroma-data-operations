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


import com.datastax.driver.core.Row;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.Industry;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.Organization;
import tech.aroma.banana.thrift.ProgrammingLanguage;
import tech.aroma.banana.thrift.Role;
import tech.aroma.banana.thrift.Tier;
import tech.aroma.banana.thrift.Urgency;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.TokenType;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

import static java.util.stream.Collectors.toList;
import static tech.aroma.banana.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.banana.data.cassandra.Tables.Applications.APP_ID;
import static tech.aroma.banana.data.cassandra.Tables.Messages.MESSAGE_ID;

/**
 * This class contains a lot of data-marshalling logic to transform
 * Cassandra Rows into the appropriate Banana Thrift Objects.
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
    
    
    static Function<Row, Application> appMapper()
    {
        return row ->
        {
            Application app = new Application();
            
            UUID appId = row.getUUID(Tables.Applications.APP_ID);
            
            if (appId != null)
            {
                app.setApplicationId(appId.toString());
            }
            
            String programmingLanguage = row.getString(Tables.Applications.PROGRAMMING_LANGUAGE);
            if (!isNullOrEmpty(programmingLanguage))
            {
                ProgrammingLanguage language = ProgrammingLanguage.valueOf(programmingLanguage);
                app.setProgrammingLanguage(language);
            }
            
            Date timeOfProvisioning = row.getTimestamp(Tables.Applications.TIME_PROVISIONED);
            if (timeOfProvisioning != null)
            {
                app.setTimeOfProvisioning(timeOfProvisioning.getTime());
            }
            
            //Transform the UUIDs to Strings
            Set<String> owners = row.getSet(Tables.Applications.OWNERS, UUID.class)
                .stream()
                .map(UUID::toString)
                .collect(Collectors.toSet());
            
            app.setOwners(owners);
            
            UUID orgId = row.getUUID(Tables.Applications.ORG_ID);
            if (orgId != null)
            {
                app.setOrganizationId(orgId.toString());
            }
         
            app.setName(row.getString(Tables.Applications.APP_NAME))
                .setApplicationDescription(row.getString(Tables.Applications.APP_DESCRIPTION))
                .setTier(row.get(Tables.Applications.TIER, Tier.class));
            
            return app;
        };
    }
    
    static Function<Row, Message> messageMapper()
    {
        return row ->
        {
            Message message = new Message();
            
            UUID msgId = row.getUUID(MESSAGE_ID);
            UUID appId = row.getUUID(APP_ID);
            
            message.setMessageId(msgId.toString())
                .setApplicationId(appId.toString())
                .setTitle(row.getString(Tables.Messages.TITLE))
                .setHostname(row.getString(Tables.Messages.HOSTNAME))
                .setMacAddress(row.getString(Tables.Messages.MAC_ADDRESS))
                .setBody(row.getString(Tables.Messages.BODY))
                .setApplicationName(row.getString(Tables.Messages.APP_NAME));
            
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
            
            String urgency = row.getString(Tables.Messages.URGENCY);
            if (!isNullOrEmpty(urgency))
            {
                message.setUrgency(Urgency.valueOf(urgency));
            }
            
            
            return message;
        };
    }
    
    static Function<Row, Organization> orgMapper()
    {
        return row ->
        {
            Organization org = new Organization();
            
            UUID orgUuid = row.getUUID(Tables.Organizations.ORG_ID);
            
            List<String> owners = Lists.create();
            
            if (doesRowContainColumn(row, Tables.Organizations.OWNERS))
            {
                Set<UUID> ownerIds = row.getSet(Tables.Organizations.OWNERS, UUID.class);
                owners = ownerIds.stream()
                    .map(UUID::toString)
                    .collect(toList());
            }
            
            org.setOrganizationId(orgUuid.toString())
                .setOrganizationName(row.getString(Tables.Organizations.ORG_NAME))
                .setLogoLink(row.getString(Tables.Organizations.ICON_LINK))
                .setOrganizationDescription(row.getString(Tables.Organizations.DESCRIPTION))
                .setIndustry(row.get(Tables.Organizations.INDUSTRY, Industry.class))
                .setGithubProfile(row.getString(Tables.Organizations.GITHUB_PROFILE))
                .setOrganizationEmail(row.getString(Tables.Organizations.EMAIL))
                .setStockMarketSymbol(row.getString(Tables.Organizations.STOCK_NAME))
                .setTier(row.get(Tables.Organizations.TIER, Tier.class))
                .setWebsite(row.getString(Tables.Organizations.WEBSITE))
                .setOwners(owners);
            
            return org;
        };
    }
    
    static Function<Row, AuthenticationToken> tokenMapper()
    {
        return row ->
        {
            AuthenticationToken token = new AuthenticationToken();
            
            Date timeOfCreation = row.getTimestamp(Tables.Tokens.TIME_OF_CREATION);
            Date timeOfExpiration = row.getTimestamp(Tables.Tokens.TIME_OF_EXPIRATION);
            
            if (timeOfCreation != null)
            {
                token.setTimeOfCreation(timeOfCreation.getTime());
            }
            
            if (timeOfExpiration != null)
            {
                token.setTimeOfExpiration(timeOfExpiration.getTime());
            }
            
            String orgId = null;
            if(doesRowContainColumn(row, Tables.Tokens.ORG_ID))
            {
                UUID orgUuid = row.getUUID(Tables.Tokens.ORG_ID);
                orgId = orgUuid != null ? orgUuid.toString() : orgId;
            }
            
            token
                .setTokenId(row.getUUID(Tables.Tokens.TOKEN_ID).toString())
                .setOwnerId(row.getUUID(Tables.Tokens.OWNER_ID).toString())
                .setOrganizationId(orgId)
                .setOwnerName(row.getString(Tables.Tokens.OWNER_NAME))
                .setTokenType(row.get(Tables.Tokens.TOKEN_TYPE, TokenType.class));
            
            return token;
        };
    }
    
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

            if(doesRowContainColumn(row, Tables.Users.ROLES))
            {
                roles = row.getSet(Tables.Users.ROLES, Role.class);
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
            
            return user
                .setUserId(row.getUUID(Tables.Users.USER_ID).toString())
                .setFirstName(row.getString(Tables.Users.FIRST_NAME))
                .setMiddleName(row.getString(Tables.Users.MIDDLE_NAME))
                .setLastName(row.getString(Tables.Users.LAST_NAME))
                .setEmail(email)
                .setRoles(roles);
        };
    }
    
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
