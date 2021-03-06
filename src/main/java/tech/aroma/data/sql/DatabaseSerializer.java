package tech.aroma.data.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;

/**
 * Responsible for saving and retrieving Aroma objects to/from the Database.
 *
 * @author SirWellington
 */
public interface DatabaseSerializer<T> extends RowMapper<T>
{
    /**
     * Saves the object to the Database.
     *
     * @param object    The Object to save.
     * @param statement The SQL Statement to use when calling the database.
     * @param database  The database to write to.
     */
    void save(@Required T object,
              @Required @NonEmpty String statement,
              @Required JdbcOperations database) throws SQLException;


    /**
     * Deserializes Aroma objects from a {@link ResultSet}.
     *
     * @param row The result set to extract data from
     * @return An Aroma object representation from the
     * @throws SQLException
     */
    T deserialize(@Required ResultSet row) throws SQLException;

    @Override
    default T mapRow(ResultSet rs, int rowNum) throws SQLException
    {
        return deserialize(rs);
    }
}
