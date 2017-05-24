package tech.aroma.data.sql.serializers;

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
    static final class Activity
    {

        static final String EVENT_ID = "event_id";
        static final String RECEPIENT_USER_ID = "recepient_user_id";
        static final String APP_ID = Applications.APP_ID;
        static final String ACTOR_USER_ID = "actor_user_id";
        static final String TIME_OF_EVENT = "time_of_event";
        static final String EVENT_TYPE = "event_type";
        static final String SERIALIZED_EVENT = "serialized_event";
    }

    @NonInstantiable
    static final class Applications
    {
        static final String APP_ID = "app_id";
        static final String APP_NAME = "app_name";

        static final String APP_DESCRIPTION = "app_description";
        static final String ORG_ID = "organization_id";
        static final String OWNERS = "owners";
        static final String PROGRAMMING_LANGUAGE = "programming_language";
        static final String TIER = "tier";
        static final String TIME_PROVISIONED = "time_provisioned";
        static final String TIME_LAST_UPDATED = "time_last_updated";
        static final String TIME_OF_TOKEN_EXPIRATION = "time_of_token_expiration";
        static final String ICON_MEDIA_ID = "app_icon_media_id";
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
    static final class Organizations
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
    static final class Reactions
    {
        static final String OWNER_ID = "owner_id";
        static final String SERIALIZED_REACTIONS = "serialized_reaction";
    }

    @NonInstantiable
    static final class Tokens
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

    @NonInstantiable
    static final class Users
    {
        static final String USER_ID = "user_id";
        static final String FIRST_NAME = "first_name";
        static final String MIDDLE_NAME = "middle_name";
        static final String LAST_NAME = "last_name";
        static final String FULL_NAME = "full_name";
        static final String EMAIL = "email";
        static final String ROLES = "roles";
        static final String BIRTH_DATE = "birthdate";
        static final String GITHUB_PROFILE = "github_profile";
        static final String PROFILE_IMAGE_ID = "profile_image_id";
        static final String TIME_ACCOUNT_CREATED = "time_account_created";
        static final String TIME_LAST_UPDATED = "time_last_updated";
    }
}
