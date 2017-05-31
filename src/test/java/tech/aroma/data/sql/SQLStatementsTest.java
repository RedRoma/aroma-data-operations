package tech.aroma.data.sql;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
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
        assertThat(SQLStatements.Deletes.ACTIVITY_EVENT, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.ACTIVITY_ALL_EVENTS, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.APPLICATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.APPLICATION_OWNERS, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.APPLICATION_NON_OWNERS, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.INBOX_MESSAGE, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.INBOX_ALL_MESSAGES, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.MEDIA, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.MEDIA_THUMBNAIL, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.ALL_MEDIA_THUMBNAILS, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.MESSAGE, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.ORGANIZATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.ORGANIZATION_MEMBER, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.ORGANIZATION_ALL_MEMBERS, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.REACTIONS, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.TOKEN, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.USER, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.USER_DEVICE, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Deletes.ALL_USER_DEVICES, not(isEmptyOrNullString()));
    }

    @Test
    public void testInserts() throws Exception
    {
        assertThat(SQLStatements.Inserts.ACTIVITY_EVENT, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Inserts.APPLICATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Inserts.APPLICATION_OWNER, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Inserts.INBOX_MESSAGE, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Inserts.MEDIA, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Inserts.MEDIA_THUMBNAIL, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Inserts.MESSAGE, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Inserts.ORGANIZATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Inserts.ORGANIZATION_MEMBER, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Inserts.REACTION, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Inserts.TOKEN, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Inserts.USER, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Inserts.ADD_USER_DEVICE, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Inserts.USER_DEVICES, not(isEmptyOrNullString()));
    }

    @Test
    public void testQueries() throws Exception
    {
        assertThat(SQLStatements.Queries.CHECK_ACTIVITY_EVENT, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_ACTIVITY_EVENT, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_ALL_ACTIVITY_FOR_USER, not(isEmptyOrNullString()));


        assertThat(SQLStatements.Queries.CHECK_APPLICATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_APPLICATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_RECENT_APPLICATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SEARCH_APPLICATION_BY_NAME, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_APPLICATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_APPLICATION_OWNERS, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_APPLICATION_BY_ORGANIZATION, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_APPLICATION_BY_OWNER, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Queries.CHECK_INBOX_MESSAGE, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.COUNT_INBOX_MESSAGES, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_INBOX_MESSAGES_FOR_USER, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Queries.CHECK_MEDIA, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.CHECK_MEDIA_THUMBNAIL, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_MEDIA, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_MEDIA_THUMBNAIL, not(isEmptyOrNullString()));

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

        assertThat(SQLStatements.Queries.SELECT_REACTION, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Queries.CHECK_TOKEN, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_TOKEN, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_TOKENS_FOR_OWNER, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Queries.CHECK_USER, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_USER, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_USER_BY_EMAIL, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_USER_BY_GITHUB, not(isEmptyOrNullString()));
        assertThat(SQLStatements.Queries.SELECT_RECENT_USERS, not(isEmptyOrNullString()));

        assertThat(SQLStatements.Queries.SELECT_USER_DEVICES, not(isEmptyOrNullString()));
    }

}