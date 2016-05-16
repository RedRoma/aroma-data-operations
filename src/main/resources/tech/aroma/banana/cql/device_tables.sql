//Assumes the existence of a 'Aroma' Keyspace

/*
	Used to store encrypted User Devices for Push Notification Events.
    Each user can own 0 or more devices.
*/
CREATE TABLE IF NOT EXISTS Aroma.User_Devices
(
	user_id uuid,
    //The Thrift structures representing a Device (Android, or iOS) Serialized as a JSON String
	serialized_devices list<text>,
	time_registered timestamp,

	PRIMARY KEY (user_id)
);
