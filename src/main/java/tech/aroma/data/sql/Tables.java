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
final class Tables
{

    @NonInstantiable
    static class Applications
    {
        final static String APP_ID = "app_id";
        final static String APP_NAME = "app_name";
    }

    @NonInstantiable
    static class Messages
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
        static final String APP_Id = Applications.APP_ID;
        static final String APP_NAME = Applications.APP_NAME;
        static final String TOTAL_MESSAGES = "total_messages";
        static final String REQUEST_TIME = "request_time";
    }
}
