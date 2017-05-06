package tech.aroma.data.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * Responsible for deserializing Aroma Objects from a {@link java.sql.ResultSet}.
 *
 * @author SirWellington
 */
@FunctionalInterface
interface DatabaseDeserializer<T> extends RowMapper<T>
{
    T deserializer(ResultSet resultSet) throws SQLException;

    @Override
    default T mapRow(ResultSet rs, int rowNum) throws SQLException
    {
        return deserializer(rs);
    }

}
