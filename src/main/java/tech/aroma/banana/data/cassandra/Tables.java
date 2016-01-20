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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

/**
 *
 * @author SirWellington
 */
@Internal
@NonInstantiable
class Tables
{
    private final static Logger LOG = LoggerFactory.getLogger(Tables.class);
    
    static class ApplicationsTable
    {
        static final String TABLE_NAME = "Applications";
        static final String TABLE_NAME_RECENTLY_CREATED = "Applications_Recently_Created";
        
        static final String APP_ID = "app_id";
        static final String APP_NAME = "name";
        static final String APP_DESCRIPTION = "app_description";
        static final String ORG_ID = "organization_id";
        static final String ORG_NAME = "organization_name";
        static final String OWNERS = "owners";
        static final String PROGRAMMING_LANGUAGE = "programming_language";
        static final String TIME_PROVISIONED = "time_provisioned";
        static final String TIER = "tier";
    }
    
    static class FollowTables
    {
        static final String TABLE_NAME_APP_FOLLOWERS = "Follow_Application_Followers";
        static final String TABLE_NAME_USER_FOLLOWING = "Follow_User_Followings";
        
        static final String APP_ID = "app_id";
        static final String USER_ID = "user_id";
        static final String APP_NAME = "app_name";
        static final String USER_FIRST_NAME = "first_name";
        static final String TIME_OF_FOLLOW = "time_of_follow";
    }
        
    
    static class MessagesTable
    {
        static final String TABLE_NAME = "Messages";
        static final String TABLE_NAME_TOTALS_BY_APP = "Messages_Totals_By_App";
        static final String TABLE_NAME_TOTALS_BY_TITLE = "Messages_Totals_By_Title";
        
        static final String MESSAGE_ID = "message_id";
        static final String APP_ID = "app_id";
        static final String APP_NAME = "app_name";
        static final String TITLE = "title";
        static final String BODY = "body";
        static final String URGENCY = "urgency";
        static final String HOSTNAME = "hostname";
        static final String IP_ADDRESS = "ip_address";
        static final String MAC_ADDRESS = "mac_address";
        static final String TIME_CREATED = "time_created";
        static final String TIME_RECEIVED = "time_received";
        static final String TOTAL_MESSAGES = "total_messages";
        
    }

    static class UsersTable
    {
        static final String TABLE_NAME = "Users";
        static final String TABLE_NAME_BY_EMAIL = "Users_By_Email";
        static final String TABLE_NAME_BY_GITHUB_PROFILE = "Users_By_Github_Profile";
        
        static final String USER_ID = "user_id";
        static final String FIRST_NAME = "first_name";
        static final String MIDDLE_NAME = "middle_name";
        static final String LAST_NAME = "last_name";
        static final String EMAILS = "emails";
        static final String EMAIL = "email";
        static final String ORGANIZATIONS = "organizations";
        static final String ROLES = "roles";
        static final String GENDER = "gender";
        static final String BIRTH_DATE = "birthdate";
        static final String GITHUB_PROFILE = "github_profile";
        
    }
    
}
