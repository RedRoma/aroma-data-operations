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
import tech.aroma.banana.data.cassandra.Tables.ApplicationsTable;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.ProgrammingLanguage;
import tech.aroma.banana.thrift.Role;
import tech.aroma.banana.thrift.Tier;
import tech.aroma.banana.thrift.User;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

import static tech.aroma.banana.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.banana.data.cassandra.Tables.ApplicationsTable.APP_ID;

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
            
            String programmingLanguage = row.getString(ApplicationsTable.PROGRAMMING_LANGUAGE);
            if (!isNullOrEmpty(programmingLanguage))
            {
                ProgrammingLanguage language = ProgrammingLanguage.valueOf(programmingLanguage);
                app.setProgrammingLanguage(language);
            }
            
            Date timeOfProvisioning = row.getTimestamp(ApplicationsTable.TIME_PROVISIONED);
            if (timeOfProvisioning != null)
            {
                app.setTimeOfProvisioning(timeOfProvisioning.getTime());
            }
            
            //Transform the UUIDs to Strings
            Set<String> owners = row.getSet(ApplicationsTable.OWNERS, UUID.class)
                .stream()
                .map(UUID::toString)
                .collect(Collectors.toSet());
            
            app.setOwners(owners);
            
            UUID orgId = row.getUUID(ApplicationsTable.ORG_ID);
            if (orgId != null)
            {
                app.setOrganizationId(orgId.toString());
            }
            
            String tier = row.getString(ApplicationsTable.TIER);
            if (!isNullOrEmpty(tier))
            {
                app.setTier(Tier.valueOf(tier));
            }
            
            app.setName(row.getString(ApplicationsTable.APP_NAME))
               .setApplicationDescription(row.getString(ApplicationsTable.APP_DESCRIPTION));

            return app;
        };
    }
    
    static Function<Row, User> userMapper()
    {
        return row ->
        {
            return new User()
                .setUserId(row.getUUID(Tables.UsersTable.USER_ID).toString())
                .setEmail(row.getString(Tables.UsersTable.EMAILS))
                .setFirstName(row.getString(Tables.UsersTable.FIRST_NAME))
                .setMiddleName(row.getString(Tables.UsersTable.MIDDLE_NAME))
                .setLastName(row.getString(Tables.UsersTable.LAST_NAME))
                .setGithubProfile(row.getString(Tables.UsersTable.GITHUB_PROFILE))
                .setRoles(row.getSet(Tables.UsersTable.ROLES, Role.class));
        };
    }

}
