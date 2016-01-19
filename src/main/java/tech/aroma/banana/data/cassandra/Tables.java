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

/**
 *
 * @author SirWellington
 */
class Tables 
{
    private final static Logger LOG = LoggerFactory.getLogger(Tables.class);
    
    static class MessagesTable
    {
        static final String TABLE_NAME = "Banana.Messages";
        static final String MESSAGE_ID = "message_id";
        static final String APPLICATION_ID = "app_id";
        static final String TITLE = "title";
        static final String BODY = "body";
        static final String URGENCY = "urgency";
        static final String HOSTNAME = "hostname";
        static final String IP_ADDRESS = "ip_address";
        static final String MAC_ADDRESS = "mac_address";
        static final String TIME_CREATED = "time_created";
        static final String TIME_RECEIVED = "time_received";
        
    }

    static class UsersTable
    {
        static final String TABLE_NAME = "Banana.Users";
        static final String TABLE_NAME_BY_EMAIL = "Banana.Users_By_Email";
        static final String TABLE_NAME_BY_GITHUB_PROFILE = "Banana.Users_By_Github_Profile";
        
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
