/**
 * @author SirWellington
 */

package tech.aroma.data.sql;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.sirwellington.alchemy.http.AlchemyHttp;

public class ModuleTesting extends AbstractModule
{


    @Override
    protected void configure()
    {

    }

    @Provides
    DataSource provideDataSource() throws MalformedURLException, SQLException
    {
        String url = createProductionTestConnection();

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);

        try (Connection conn = dataSource.getConnection())
        {
            System.out.println("Connected");
        }

        return dataSource;
    }


    @Provides
    JdbcTemplate provideJdbcTemplate(DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }

    private String createConnectionFromPostgression() throws MalformedURLException
    {
        //Using http://www.postgression.com/
        String api = "http://api.postgression.com";
        AlchemyHttp http = AlchemyHttp.newDefaultInstance();

        String connectionURL = http.go()
                                   .get()
                                   .expecting(String.class)
                                   .at(api);

        return connectionURL;
    }

    private String createProductionTestConnection()
    {
        int port = 5432;
        String database = "aroma";
        String host = "database.aroma.tech";
        String user = "aroma_test_user";
        String password = "VNotaxtzMu0iggtORpArvZBPOzRdhX";

        String applicationName = "AromaService";

        String url = String.format("jdbc:postgresql://%s:%d/%s?user=%s&password=%s&ApplicationName=%s",
                                   host,
                                   port,
                                   database,
                                   user,
                                   password,
                                   applicationName);

        return url;
    }
}
