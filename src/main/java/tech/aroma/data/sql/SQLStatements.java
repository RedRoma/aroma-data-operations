package tech.aroma.data.sql;

import java.io.IOException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import tech.sirwellington.alchemy.annotations.access.Internal;

/**
 * Contains the SQL SQLStatements that allow queries and updates.
 *
 * @author SirWellington
 */
@Internal
final class SQLStatements
{

    static class Deletes
    {
        static final String MESSAGE = loadSQLFile("/deletes/delete_message.sql");
    }

    static class Inserts
    {
        static final String MESSAGE = loadSQLFile("/inserts/insert_message.sql");
    }

    static class Queries
    {
        static final String SELECT_MESSAGE = loadSQLFile("/queries/select_message.sql");
        static final String SELECT_APP_MESSAGES = loadSQLFile("/queries/select_app_messages.sql");
        static final String CHECK_MESSAGE = loadSQLFile("/queries/check_message.sql");
        static final String SELECT_MESSAGES_BY_HOSTNAME = loadSQLFile("/queries/select_messages_by_hostname.sql");
    }

    static String loadSQLFile(String name)
    {
        final String path = "tech/aroma/sql";
        final String fullPath = path + "/" + name;

        URL url = Resources.getResource(fullPath);

        try
        {
            return Resources.toString(url, Charsets.UTF_8);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Failed to load file: " + name, ex);
        }
    }
}
