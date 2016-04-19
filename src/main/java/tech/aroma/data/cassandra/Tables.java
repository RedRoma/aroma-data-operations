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

 
package tech.aroma.data.cassandra;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

/**
 * Contains all of the Tables and Column names used by the Aroma Service in Cassandra.
 * 
 * @author SirWellington
 */
@Internal
@NonInstantiable
class Tables
{
    private final static Logger LOG = LoggerFactory.getLogger(Tables.class);
    
    static class Activity
    {
        static final String TABLE_NAME = "Activity";
        
        static final String USER_ID = "user_id";
        static final String EVENT_ID = "event_id";
        static final String APP_ID = "app_id";
        static final String ACTOR_ID = "actor_id";
        static final String TIME_OF_EVENT = "time_of_event";
        static final String APP_NAME = "app_name";
        static final String ACTOR_FIRST_NAME = "actor_first_name";
        static final String ACTOR_MIDDLE_NAME = "actor_middle_name";
        static final String ACTOR_LAST_NAME = "actor_last_name";
        static final String ACTOR_FULL_NAME = "actor_full_name";
        
        static final String SERIALIZED_EVENT = "serialized_event";
    }
    
    static class Applications
    {
        static final String TABLE_NAME = "Applications";
        static final String TABLE_NAME_RECENTLY_CREATED = "Applications_Recently_Created";
        
        static final String APP_ID = "app_id";
        static final String APP_NAME = "name";
        static final String APP_DESCRIPTION = "app_description";
        static final String ICON_MEDIA_ID = "app_icon_media_id";
        static final String ORG_ID = "organization_id";
        static final String ORG_NAME = "organization_name";
        static final String OWNERS = "owners";
        static final String PROGRAMMING_LANGUAGE = "programming_language";
        static final String TIME_PROVISIONED = "time_provisioned";
        static final String TIME_OF_TOKEN_EXPIRATION = "time_of_token_expiration";
        static final String TIER = "tier";
    }
    
    static class Credentials
    {
        static final String TABLE_NAME = "Credentials";
        
        static final String USER_ID = "user_id";
        static final String ENCRYPTED_PASSWORD = "encrypted_password";
        static final String TIME_CREATED = "time_created";
    }
    
    static class Follow
    {
        static final String TABLE_NAME_APP_FOLLOWERS = "Follow_Application_Followers";
        static final String TABLE_NAME_USER_FOLLOWING = "Follow_User_Followings";
        
        static final String APP_ID = "app_id";
        static final String USER_ID = "user_id";
        static final String APP_NAME = "app_name";
        static final String USER_FIRST_NAME = "user_first_name";
        static final String TIME_OF_FOLLOW = "time_of_follow";
    }
    
    static class Inbox
    {
        static final String TABLE_NAME = "Inbox";
      
        static final String USER_ID = Users.USER_ID;
        static final String MESSAGE_ID = Messages.MESSAGE_ID;
        static final String APP_ID = Messages.APP_ID;
        static final String APP_NAME = Messages.APP_NAME;
        static final String TITLE = Messages.TITLE;
        static final String BODY = Messages.BODY;
        static final String URGENCY = Messages.URGENCY;
        static final String HOSTNAME = Messages.HOSTNAME;
        static final String IP_ADDRESS = Messages.IP_ADDRESS;
        static final String MAC_ADDRESS = Messages.MAC_ADDRESS;
        static final String TIME_CREATED = Messages.TIME_CREATED;
        static final String TIME_RECEIVED = Messages.TIME_RECEIVED;
        static final String TOTAL_MESSAGES = Messages.TOTAL_MESSAGES;
    }
        
    static class Media
    {
        static final String TABLE_NAME = "Media";
        static final String TABLE_NAME_THUMBNAILS = "Media_Thumbnails";
        
        static final String MEDIA_ID = "media_id";
        static final String MEDIA_TYPE = "media_type";
        static final String CREATION_TIME = "creation_time";
        static final String EXPIRATION_TIME = "expiration_time";
        static final String BINARY = "binary";
        static final String DIMENSION = "dimension";
        static final String WIDTH = "width";
        static final String HEIGHT = "height";
    }
    
    static class Messages
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
    
    static class Organizations
    {
        static final String TABLE_NAME = "Organizations";
        static final String TABLE_NAME_MEMBERS = "Organizations_Members";
        static final String TABLE_NAME_MEMBERS_RECENT = "Organizations_Members_Recent";
        
        static final String ORG_ID = "org_id";
        static final String ORG_NAME = "org_name";
        static final String OWNERS = "owners";
        static final String ICON_LINK = "icon_link";
        static final String INDUSTRY = "industry";
        static final String EMAIL = "contact_email";
        static final String GITHUB_PROFILE = "github_profile";
        static final String STOCK_NAME = "stock_name";
        static final String TIER = "tier";
        static final String DESCRIPTION = "description";
        static final String WEBSITE = "website";
        
        static final String USER_ID = "user_id";
        static final String USER_FIRST_NAME = "user_first_name";
        static final String USER_MIDDLE_NAME = "user_middle_name";
        static final String USER_LAST_NAME = "user_last_name";
        static final String USER_ROLES = "user_roles";
        static final String USER_EMAIL = "user_email";
    }

    static class Tokens
    {
        static final String TABLE_NAME = "Tokens";
        static final String TABLE_NAME_BY_OWNER = "Tokens_By_Owner";
        
        static final String TOKEN_ID = "token_id";
        static final String OWNER_ID = "owner_id";
        static final String OWNER_NAME = "owner_name";
        static final String FEATURES = "features";
        static final String TIME_OF_EXPIRATION = "time_of_expiration";
        static final String TIME_OF_CREATION = "time_of_creation";
        static final String ORG_ID = "organization_id";
        static final String TOKEN_TYPE = "token_type";
    }
    
    static class Users
    {
        static final String TABLE_NAME = "Users";
        static final String TABLE_NAME_RECENT = "Users_Recent";
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
        static final String PROFILE_IMAGE_ID = "profile_image_id";
        static final String TIME_ACCOUNT_CREATED = "time_account_created";
        
    }
    
}
