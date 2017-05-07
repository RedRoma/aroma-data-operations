/**
 * @author SirWellington
 */

package tech.aroma.data.sql;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class Resources
{

    public static Logger LOG = LoggerFactory.getLogger(Resources.class);

    private static final Injector injector = Guice.createInjector(new ModuleTesting());


    public static JdbcTemplate connectToDatabase()
    {
        return injector.getInstance(JdbcTemplate.class);
    }
}
