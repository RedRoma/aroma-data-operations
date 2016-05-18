//Assumes the existence of a 'Aroma' Keyspace

/*
    Used to store User Preferences and secondary information about Users, such as:
	+ Mobile Devices they own or use (for push notifications)
	+ Their Personal Reactions which are run on messages that show up in their Inbox.
	+ User Activity
*/
CREATE TABLE IF NOT EXISTS Aroma.User_Preferences
(
    user_id uuid,
    //A Serialized Set of Mobile Devices used by this user, (Android, or iOS), Serialized as a JSON String
	serialized_devices set<text>,
    last_sign_in_time timestamp,
    /* These represent a User's personal reactions, serialized as a  */
    serialized_reactions list<text>,

	PRIMARY KEY (user_id)
);
