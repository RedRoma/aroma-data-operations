//Assumes the existence of a 'Aroma' Keyspace

/*
	Used to store User Device information for Push Notification events.
*/
CREATE TABLE IF NOT EXISTS Aroma.User_Devices
(
	user_id uuid,
	//The Thrift Structures representing each device, serialized as a JSON String.
	serialized_devices list<text>,
	time_registered timestamp,

	PRIMARY KEY (user_id)
);
