package tech.aroma.data.sql;

import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

/**
 * Created by sirwellington on 5/6/17.

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
        final static String MESSAGE_ID = "message_id";
        final static String TITLE = "title";
        final static String BODY = "body";
        final static String PRIORITY = "priority";
        final static String TIME_CREATED = "time_created";
        final static String TIME_RECEIVED = "time_received";
        final static String HOSTNAME = "hostname";
        final static String IP_ADDRESS = "ip_address";
        final static String DEVICE_NAME = "device_name";
        final static String APP_Id = Applications.APP_ID;
        final static String APP_NAME = Applications.APP_NAME;
        final static String TOTAL_MESSAGES = "total_messages";
        final static String REQUEST_TIME = "request_time";
    }
}
