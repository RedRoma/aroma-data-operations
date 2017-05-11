package tech.aroma.data.sql;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

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
    public void testDeletes() throws Exception
    {
        assertThat(SQLStatements.Deletes.MESSAGE, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.ORGANIZATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.ORGANIZATION_MEMBER, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.ORGANIZATION_ALL_MEMBERS, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.TOKEN, not(isEmptyOrNullString()));
    }

    @Test
    public void testInserts() throws Exception
    {
        assertThat(SQLStatements.Inserts.MESSAGE, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Inserts.ORGANIZATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Inserts.ORGANIZATION_MEMBER, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Inserts.TOKEN, not(isEmptyOrNullString()));
    }

    @Test
    public void testQueries() throws Exception
    {
        assertThat(SQLStatements.Queries.CHECK_MESSAGE, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.COUNT_MESSAGES, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Queries.SELECT_MESSAGE, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_MESSAGES_BY_APPLICATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_MESSAGES_BY_HOSTNAME, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_MESSAGES_BY_TITLE, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Queries.CHECK_ORGANIZATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.CHECK_ORGANIZATION_HAS_MEMBER, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_ORGANIZATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_ORGANIZATION_MEMBERS, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SEARCH_ORGANIZATION_BY_NAME, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Queries.CHECK_TOKEN, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_TOKEN, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_TOKENS_FOR_OWNER, not(isEmptyOrNullString()));
    }

}