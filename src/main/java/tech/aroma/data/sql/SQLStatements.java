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

package tech.aroma.data.sql;

import java.io.IOException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;

/**
 * Contains the SQL SQLStatements that allow queries and updates.
 *
 * @author SirWellington
 */
@Internal
final class SQLStatements
{

    private static final Logger LOG = LoggerFactory.getLogger(SQLStatements.class);

    static class Deletes
    {
        static final String ACTIVITY_EVENT = loadSQLFile("tech/aroma/sql/deletes/delete_activity_event.sql");
        static final String ACTIVITY_ALL_EVENTS = loadSQLFile("tech/aroma/sql/deletes/delete_all_activity_for_user.sql");
        static final String APPLICATION = loadSQLFile("tech/aroma/sql/deletes/delete_application.sql");
        static final String APPLICATION_OWNERS = loadSQLFile("tech/aroma/sql/deletes/delete_application_owners.sql");
        static final String APPLICATION_NON_OWNERS = loadSQLFile("tech/aroma/sql/deletes/delete_application_owners_not_in_set.sql");
        static final String INBOX_MESSAGE = loadSQLFile("tech/aroma/sql/deletes/delete_inbox_message.sql");
        static final String INBOX_ALL_MESSAGES = loadSQLFile("tech/aroma/sql/deletes/delete_all_inbox_message_for_user.sql");
        static final String MESSAGE = loadSQLFile("tech/aroma/sql/deletes/delete_message.sql");
        static final String ORGANIZATION = loadSQLFile("tech/aroma/sql/deletes/delete_organization.sql");
        static final String ORGANIZATION_MEMBER = loadSQLFile("tech/aroma/sql/deletes/delete_organization_member.sql");
        static final String ORGANIZATION_ALL_MEMBERS = loadSQLFile("tech/aroma/sql/deletes/delete_organization_all_members.sql");
        static final String REACTIONS = loadSQLFile("tech/aroma/sql/deletes/delete_reactions.sql");
        static final String TOKEN = loadSQLFile("tech/aroma/sql/deletes/delete_token.sql");
        static final String USER = loadSQLFile("tech/aroma/sql/deletes/delete_user.sql");
        static final String USER_DEVICE = loadSQLFile("tech/aroma/sql/deletes/delete_user_device.sql");
        static final String USER_DEVICES = loadSQLFile("tech/aroma/sql/deletes/delete_all_user_devices.sql");
    }

    static class Inserts
    {
        static final String ACTIVITY_EVENT = loadSQLFile("tech/aroma/sql/inserts/insert_activity.sql");

        static final String APPLICATION = loadSQLFile("tech/aroma/sql/inserts/insert_application.sql");
        static final String APPLICATION_OWNER = loadSQLFile("tech/aroma/sql/inserts/insert_application_owner.sql");

        static final String INBOX_MESSAGE = loadSQLFile("tech/aroma/sql/inserts/insert_inbox_message.sql");
        static final String MESSAGE = loadSQLFile("tech/aroma/sql/inserts/insert_message.sql");

        static final String ORGANIZATION = loadSQLFile("tech/aroma/sql/inserts/insert_organization.sql");
        static final String ORGANIZATION_MEMBER = loadSQLFile("tech/aroma/sql/inserts/insert_organization_member.sql");

        static final String REACTION = loadSQLFile("tech/aroma/sql/inserts/insert_reactions.sql");

        static final String TOKEN = loadSQLFile("tech/aroma/sql/inserts/insert_token.sql");

        static final String USER = loadSQLFile("tech/aroma/sql/inserts/insert_user.sql");

        static final String USER_DEVICE = loadSQLFile("tech/aroma/sql/inserts/add_user_device.sql");
        static final String USER_DEVICES = loadSQLFile("tech/aroma/sql/inserts/insert_user_devices.sql");
    }

    static class Queries
    {
        static final String CHECK_ACTIVITY_EVENT = loadSQLFile("tech/aroma/sql/queries/check_activity_event_exists.sql");
        static final String SELECT_ACTIVITY_EVENT = loadSQLFile("tech/aroma/sql/queries/select_activity_event.sql");
        static final String SELECT_ALL_ACTIVITY_FOR_USER = loadSQLFile("tech/aroma/sql/queries/select_all_activity_events_for_user.sql");


        static final String CHECK_APPLICATION = loadSQLFile("tech/aroma/sql/queries/check_application.sql");
        static final String SELECT_APPLICATION = loadSQLFile("tech/aroma/sql/queries/select_application.sql");
        static final String SELECT_RECENT_APPLICATION = loadSQLFile("tech/aroma/sql/queries/select_recent_applications.sql");
        static final String SEARCH_APPLICATION_BY_NAME = loadSQLFile("tech/aroma/sql/queries/search_application_by_name.sql");
        static final String SELECT_APPLICATION_BY_OWNER = loadSQLFile("tech/aroma/sql/queries/select_application_by_owner.sql");
        static final String SELECT_APPLICATION_BY_ORGANIZATION = loadSQLFile("tech/aroma/sql/queries/select_application_by_organization.sql");
        static final String SELECT_APPLICATION_OWNERS = loadSQLFile("tech/aroma/sql/queries/select_application_owners.sql");

        static final String CHECK_INBOX_MESSAGE = loadSQLFile("tech/aroma/sql/queries/check_inbox_has_message.sql");
        static final String COUNT_INBOX_MESSAGES = loadSQLFile("tech/aroma/sql/queries/count_inbox_messages.sql");
        static final String SELECT_INBOX_MESSAGES_FOR_USER = loadSQLFile("tech/aroma/sql/queries/select_inbox_messages_for_user.sql");


        static final String CHECK_MESSAGE = loadSQLFile("tech/aroma/sql/queries/check_message.sql");
        static final String COUNT_MESSAGES = loadSQLFile("tech/aroma/sql/queries/count_messages.sql");
        static final String SELECT_MESSAGE = loadSQLFile("tech/aroma/sql/queries/select_message.sql");
        static final String SELECT_MESSAGES_BY_APPLICATION = loadSQLFile("tech/aroma/sql/queries/select_app_messages.sql");
        static final String SELECT_MESSAGES_BY_HOSTNAME = loadSQLFile("tech/aroma/sql/queries/select_messages_by_hostname.sql");
        static final String SELECT_MESSAGES_BY_TITLE = loadSQLFile("tech/aroma/sql/queries/select_messages_by_title.sql");
        static final String CHECK_ORGANIZATION = loadSQLFile("tech/aroma/sql/queries/check_organization.sql");

        static final String CHECK_ORGANIZATION_HAS_MEMBER = loadSQLFile("tech/aroma/sql/queries/check_organization_has_member.sql");
        static final String SELECT_ORGANIZATION = loadSQLFile("tech/aroma/sql/queries/select_organization.sql");
        static final String SELECT_ORGANIZATION_MEMBERS = loadSQLFile("tech/aroma/sql/queries/select_organization_members.sql");
        static final String SEARCH_ORGANIZATION_BY_NAME = loadSQLFile("tech/aroma/sql/queries/search_organization_by_name.sql");
        static final String CHECK_TOKEN = loadSQLFile("tech/aroma/sql/queries/check_token.sql");

        static final String SELECT_REACTION = loadSQLFile("tech/aroma/sql/queries/select_reactions.sql");

        static final String SELECT_TOKEN = loadSQLFile("tech/aroma/sql/queries/select_token.sql");
        static final String SELECT_TOKENS_FOR_OWNER = loadSQLFile("tech/aroma/sql/queries/select_tokens_for_owner.sql");

        static final String CHECK_USER = loadSQLFile("tech/aroma/sql/queries/check_user.sql");
        static final String SELECT_USER = loadSQLFile("tech/aroma/sql/queries/select_user.sql");
        static final String SELECT_USER_BY_EMAIL = loadSQLFile("tech/aroma/sql/queries/select_user_by_email.sql");
        static final String SELECT_USER_BY_GITHUB = loadSQLFile("tech/aroma/sql/queries/select_user_by_github.sql");
        static final String SELECT_RECENT_USERS = loadSQLFile("tech/aroma/sql/queries/select_user_recently_created.sql");

        static final String SELECT_USER_DEVICES = loadSQLFile("tech/aroma/sql/queries/select_user_devices.sql");

    }

    private static String loadSQLFile(String path)
    {
        URL url = Resources.getResource(path);

        try
        {
            return Resources.toString(url, Charsets.UTF_8);
        }
        catch (IOException ex)
        {
            LOG.error("Failed to load file: {}", path, ex);
            throw new RuntimeException("Failed to load file: " + path, ex);
        }
    }
}
