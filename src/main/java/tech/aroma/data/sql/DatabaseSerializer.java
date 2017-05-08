package tech.aroma.data.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

import org.apache.thrift.TBase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import tech.sirwellington.alchemy.annotations.arguments.*;

/**
 * Responsible for saving and retrieving Aroma objects to/from the Database.
 *
 * @author SirWellington
 */
public interface DatabaseSerializer<T extends TBase<?, ?>> extends RowMapper<T>
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


    /**
     * Deserializes Aroma objects from a {@link ResultSet}.
     *
     * @param resultSet The result set to extract data from
     * @return An Aroma object representation from the
     * @throws SQLException
     */
    T deserialize(@Required ResultSet resultSet) throws SQLException;

    @Override
    default T mapRow(ResultSet rs, int rowNum) throws SQLException
    {
        return deserialize(rs);
    }
}
