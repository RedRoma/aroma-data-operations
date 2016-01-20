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
import tech.aroma.banana.data.cassandra.Tables.Applications;
import tech.aroma.banana.data.cassandra.Tables.MessagesTable;
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
import static tech.aroma.banana.data.cassandra.Tables.MessagesTable.MESSAGE_ID;

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
            
            UUID appId = row.getUUID(APP_ID);
            
            if (appId != null)
            {
                app.setApplicationId(appId.toString());
            }
            
            String programmingLanguage = row.getString(Applications.PROGRAMMING_LANGUAGE);
            if (!isNullOrEmpty(programmingLanguage))
            {
                ProgrammingLanguage language = ProgrammingLanguage.valueOf(programmingLanguage);
                app.setProgrammingLanguage(language);
            }
            
            Date timeOfProvisioning = row.getTimestamp(Applications.TIME_PROVISIONED);
            if (timeOfProvisioning != null)
            {
                app.setTimeOfProvisioning(timeOfProvisioning.getTime());
            }
            
            //Transform the UUIDs to Strings
            Set<String> owners = row.getSet(Applications.OWNERS, UUID.class)
                .stream()
                .map(UUID::toString)
                .collect(Collectors.toSet());
            
            app.setOwners(owners);
            
            UUID orgId = row.getUUID(Applications.ORG_ID);
            if (orgId != null)
            {
                app.setOrganizationId(orgId.toString());
            }
            
            String tier = row.getString(Applications.TIER);
            if (!isNullOrEmpty(tier))
            {
                app.setTier(Tier.valueOf(tier));
            }
            
            app.setName(row.getString(Applications.APP_NAME))
                .setApplicationDescription(row.getString(Applications.APP_DESCRIPTION));
            
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
                .setTitle(row.getString(MessagesTable.TITLE))
                .setHostname(row.getString(MessagesTable.HOSTNAME))
                .setMacAddress(row.getString(MessagesTable.MAC_ADDRESS))
                .setBody(row.getString(MessagesTable.BODY))
                .setApplicationName(row.getString(MessagesTable.APP_NAME));
            
            Date timeCreated = row.getTimestamp(MessagesTable.TIME_CREATED);
            Date timeReceived = row.getTimestamp(MessagesTable.TIME_RECEIVED);
            
            if (timeCreated != null)
            {
                message.setTimeOfCreation(timeCreated.getTime());
            }
            
            if (timeReceived != null)
            {
                message.setTimeMessageReceived(timeReceived.getTime());
            }
            
            String urgency = row.getString(MessagesTable.URGENCY);
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
            
            if (doesRowContainColumn(row, Tables.UsersTable.EMAIL))
            {
                email = row.getString(Tables.UsersTable.EMAIL);
            }
            else if (doesRowContainColumn(row, Tables.UsersTable.EMAILS))
            {
                Set<String> emails = row.getSet(Tables.UsersTable.EMAILS, String.class);
                email = emails.stream().findFirst().orElse(null);
            }
            
            Date birthDate = row.getTimestamp(Tables.UsersTable.BIRTH_DATE);
            
            return new User()
                .setUserId(row.getUUID(Tables.UsersTable.USER_ID).toString())
                .setFirstName(row.getString(Tables.UsersTable.FIRST_NAME))
                .setMiddleName(row.getString(Tables.UsersTable.MIDDLE_NAME))
                .setLastName(row.getString(Tables.UsersTable.LAST_NAME))
                .setEmail(email)
                .setGithubProfile(row.getString(Tables.UsersTable.GITHUB_PROFILE))
                .setRoles(row.getSet(Tables.UsersTable.ROLES, Role.class));
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
