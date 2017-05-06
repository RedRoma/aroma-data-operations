package tech.aroma.data.sql;


import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class QueriesTest
{


    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void loadSQLFile() throws Exception
    {
        assertThat(Queries.INSERT_MESSAGE, not(Matchers.isEmptyOrNullString()));
    }

}