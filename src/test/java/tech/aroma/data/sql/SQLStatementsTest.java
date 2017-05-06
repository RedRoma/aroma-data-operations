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
public class SQLStatementsTest
{


    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testInserts() throws Exception
    {
        assertThat(SQLStatements.Inserts.MESSAGE, not(Matchers.isEmptyOrNullString()));
    }

    @Test
    public void testQueries() throws Exception
    {
        assertThat(SQLStatements.Queries.SELECT_MESSAGE, not(Matchers.isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_APP_MESSAGES, not(Matchers.isEmptyOrNullString()));
    }

}