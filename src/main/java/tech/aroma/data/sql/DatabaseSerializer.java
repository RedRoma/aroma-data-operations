package tech.aroma.data.sql;

import java.sql.SQLException;
import java.time.Duration;

import org.apache.thrift.TBase;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.sirwellington.alchemy.annotations.arguments.*;

/**
 * Responsible for saving an object in the Database.
 *
 * @author SirWellington
 */
interface DatabaseSerializer<T extends TBase<?, ?>>
{
    /**
     * Saves the object to the Database.
     *
     * @param object The Object to save.
     * @param timeToLive The lifetime given to the object.
     *                   This parameter is optional and can be null.
     * @param statement The SQL Statement to use when calling the database.
     * @param database The database to write to.
     */
    void save(@Required T object,
              @Optional Duration timeToLive,
              @NonEmpty String statement,
              @Required JdbcTemplate database) throws SQLException;
}
