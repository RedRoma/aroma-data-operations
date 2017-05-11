package tech.aroma.data.sql.serializers;

import tech.aroma.thrift.Organization;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

/**
 * Contains the internal structure of the SQL Tables
 * used to serialize Aroma objects.
 *
 * @author SirWellington
 */
@Internal
@NonInstantiable
final class Tables
{

    @NonInstantiable
    static final class Applications
    {
        final static String APP_ID = "app_id";
        final static String APP_NAME = "app_name";
    }

    @NonInstantiable
    static final class Messages
    {
        static final String TABLE_NAME = "Messages";
        static final String TABLE_NAME_TOTALS_BY_APP = "Messages_Totals_By_App";
        static final String TABLE_NAME_TOTALS_BY_TITLE = "Messages_Totals_By_Title";

        static final String MESSAGE_ID = "message_id";
        static final String TITLE = "title";
        static final String BODY = "body";
        static final String PRIORITY = "priority";
        static final String TIME_CREATED = "time_created";
        static final String TIME_RECEIVED = "time_received";
        static final String HOSTNAME = "hostname";
        static final String IP_ADDRESS = "ip_address";
        static final String DEVICE_NAME = "device_name";
        static final String APP_ID = Applications.APP_ID;
        static final String APP_NAME = Applications.APP_NAME;
        static final String TOTAL_MESSAGES = "total_messages";
        static final String REQUEST_TIME = "request_time";
    }

    @NonInstantiable
    static class Organizations
    {
        static final String TABLE_NAME = "Organizations";
        static final String TABLE_NAME_MEMBERS = "Organizations_Members";

        static final String ORG_ID = "organization_id";
        static final String ORG_NAME = "organization_name";
        static final String OWNERS = "owners";
        static final String ICON_LINK = "icon_link";
        static final String INDUSTRY = "industry";
        static final String EMAIL = "contact_email";
        static final String GITHUB_PROFILE = "github_profile";
        static final String STOCK_NAME = "stock_name";
        static final String TIER = "tier";
        static final String DESCRIPTION = "description";
        static final String WEBSITE = "website";

    }

    @NonInstantiable
    static class Tokens
    {
        static final String TABLE_NAME = "Tokens";

        static final String TOKEN_ID = "token_id";
        static final String OWNER_ID = "owner_id";
        static final String OWNER_NAME = "owner_name";
        static final String TIME_OF_EXPIRATION = "time_of_expiration";
        static final String TIME_OF_CREATION = "time_of_creation";
        static final String ORG_ID = Organizations.ORG_ID;
        static final String TOKEN_TYPE = "token_type";
        static final String TOKEN_STATUS = "token_status";
    }
}
