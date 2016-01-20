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
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.ProgrammingLanguage;
import tech.aroma.banana.thrift.Role;
import tech.aroma.banana.thrift.Tier;
import tech.aroma.banana.thrift.Urgency;
import tech.aroma.banana.thrift.User;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

import static tech.aroma.banana.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.banana.data.cassandra.Tables.Applications.APP_ID;
import static tech.aroma.banana.data.cassandra.Tables.Messages.MESSAGE_ID;

/**
 *
 * @author SirWellington
 */
@Internal
@NonInstantiable
final class Mappers
{
    private final static Logger LOG = LoggerFactory.getLogger(Mappers.class);
    
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
            
            String tier = row.getString(Tables.Applications.TIER);
            if (!isNullOrEmpty(tier))
            {
                app.setTier(Tier.valueOf(tier));
            }
            
            app.setName(row.getString(Tables.Applications.APP_NAME))
                .setApplicationDescription(row.getString(Tables.Applications.APP_DESCRIPTION));
            
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
    
    static Function<Row, User> userMapper()
    {
        return row ->
        {
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
            
            Date birthDate = null;

            if (doesRowContainColumn(row, Tables.Users.BIRTH_DATE))
            {
                birthDate = row.getTimestamp(Tables.Users.BIRTH_DATE);
            }

            return new User()
                .setUserId(row.getUUID(Tables.Users.USER_ID).toString())
                .setFirstName(row.getString(Tables.Users.FIRST_NAME))
                .setMiddleName(row.getString(Tables.Users.MIDDLE_NAME))
                .setLastName(row.getString(Tables.Users.LAST_NAME))
                .setEmail(email)
                .setGithubProfile(row.getString(Tables.Users.GITHUB_PROFILE))
                .setRoles(row.getSet(Tables.Users.ROLES, Role.class));
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
