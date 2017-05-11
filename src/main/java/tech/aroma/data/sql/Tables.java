package tech.aroma.data.sql;

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
public final class Tables
{

    @NonInstantiable
    public static final class Applications
    {
        public final static String APP_ID = "app_id";
        public final static String APP_NAME = "app_name";
    }

    @NonInstantiable
    public static final class Messages
    {
        public static final String TABLE_NAME = "Messages";
        public static final String TABLE_NAME_TOTALS_BY_APP = "Messages_Totals_By_App";
        public static final String TABLE_NAME_TOTALS_BY_TITLE = "Messages_Totals_By_Title";

        public static final String MESSAGE_ID = "message_id";
        public static final String TITLE = "title";
        public static final String BODY = "body";
        public static final String PRIORITY = "priority";
        public static final String TIME_CREATED = "time_created";
        public static final String TIME_RECEIVED = "time_received";
        public static final String HOSTNAME = "hostname";
        public static final String IP_ADDRESS = "ip_address";
        public static final String DEVICE_NAME = "device_name";
        public static final String APP_ID = Applications.APP_ID;
        public static final String APP_NAME = Applications.APP_NAME;
        public static final String TOTAL_MESSAGES = "total_messages";
        public static final String REQUEST_TIME = "request_time";
    }

    @NonInstantiable
    public static class Organizations
    {
        public static final String TABLE_NAME = "Organizations";
        public static final String TABLE_NAME_MEMBERS = "Organizations_Members";

        public static final String ORG_ID = "org_id";
        public static final String ORG_NAME = "org_name";
        public static final String OWNERS = "owners";
        public static final String ICON_LINK = "icon_link";
        public static final String INDUSTRY = "industry";
        public static final String EMAIL = "contact_email";
        public static final String GITHUB_PROFILE = "github_profile";
        public static final String STOCK_NAME = "stock_name";
        public static final String TIER = "tier";
        public static final String DESCRIPTION = "description";
        public static final String WEBSITE = "website";

        public static final String USER_ID = "user_id";
        public static final String USER_FIRST_NAME = "user_first_name";
        public static final String USER_MIDDLE_NAME = "user_middle_name";
        public static final String USER_LAST_NAME = "user_last_name";
        public static final String USER_ROLES = "user_roles";
        public static final String USER_EMAIL = "user_email";
    }
}
