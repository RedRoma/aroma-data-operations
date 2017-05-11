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
        static final String MESSAGE = loadSQLFile("/deletes/delete_message.sql");
        static final String ORGANIZATION = loadSQLFile("/deletes/delete_organization.sql");
        static final String ORGANIZATION_MEMBER = loadSQLFile("/deletes/delete_organization_member.sql");
        static final String ORGANIZATION_ALL_MEMBERS = loadSQLFile("/deletes/delete_organization_all_members.sql");
    }

    static class Inserts
    {
        static final String MESSAGE = loadSQLFile("/inserts/insert_message.sql");

        static final String ORGANIZATION = loadSQLFile("/inserts/insert_organization.sql");
        static final String ORGANIZATION_MEMBER = loadSQLFile("/inserts/insert_organization_member.sql");
    }

    static class Queries
    {
        static final String CHECK_MESSAGE = loadSQLFile("/queries/check_message.sql");
        static final String COUNT_MESSAGES = loadSQLFile("/queries/count_messages.sql");
        static final String SELECT_MESSAGE = loadSQLFile("/queries/select_message.sql");
        static final String SELECT_MESSAGES_BY_APPLICATION = loadSQLFile("/queries/select_app_messages.sql");
        static final String SELECT_MESSAGES_BY_HOSTNAME = loadSQLFile("/queries/select_messages_by_hostname.sql");
        static final String SELECT_MESSAGES_BY_TITLE = loadSQLFile("/queries/select_messages_by_title.sql");

        static final String CHECK_ORGANIZATION = loadSQLFile("/queries/check_organization.sql");
        static final String CHECK_ORGANIZATION_HAS_MEMBER = loadSQLFile("/queries/check_organization_has_member.sql");
        static final String SELECT_ORGANIZATION = loadSQLFile("/queries/select_organization.sql");
        static final String SELECT_ORGANIZATION_MEMBERS = loadSQLFile("/queries/select_organization_members.sql");
        static final String SEARCH_ORGANIZATION_BY_NAME = loadSQLFile("/queries/search_organization_by_name.sql");
    }

    static String loadSQLFile(String name)
    {
        final String path = "tech/aroma/sql";
        final String fullPath = path + name;

        URL url = Resources.getResource(fullPath);

        try
        {
            return Resources.toString(url, Charsets.UTF_8);
        }
        catch (IOException ex)
        {
            LOG.error("Failed to load file: {}", name, ex);
            throw new RuntimeException("Failed to load file: " + name, ex);
        }
    }
}
