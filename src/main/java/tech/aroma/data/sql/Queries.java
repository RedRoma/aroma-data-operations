package tech.aroma.data.sql;

import java.io.IOException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import tech.sirwellington.alchemy.annotations.access.Internal;

/**
 * Contains the SQL Queries that allow queries and updates.
 *
 * @author SirWellington
 */
@Internal
final class Queries
{

    //INSERT STATEMENTS
    static final String INSERT_MESSAGE = loadSQLFile("insert_message.sql");

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
