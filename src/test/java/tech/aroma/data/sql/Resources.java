/**
 * @author SirWellington
 */

package tech.aroma.data.sql;

import com.google.inject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.data.sql.serializers.ModuleSerializers;
import tech.aroma.thrift.Message;

public class Resources
{

    public static Logger LOG = LoggerFactory.getLogger(Resources.class);

    private static final Injector injector = Guice.createInjector(new ModuleTesting(),
                                                                  new ModuleSerializers());


    public static JdbcTemplate connectToDatabase()
    {
        return injector.getInstance(JdbcTemplate.class);
    }

    public static DatabaseSerializer<Message> getMessageSerializer()
    {
        TypeLiteral<DatabaseSerializer<Message>> literal = new TypeLiteral<DatabaseSerializer<Message>>()
        {
        };
        return injector.getInstance(Key.get(literal));
    }
}
