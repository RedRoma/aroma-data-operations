//Assumes the existence of a 'Banana' Keyspace

/* Defines an App's Followers */
CREATE TABLE IF NOT EXISTS Banana.Follow_Application_Followers
(
	app_id uuid,
	user_id uuid,
	app_name text,
	user_first_name text,
	time_of_follow timestamp,

	PRIMARY KEY ((app_id), user_id)
);

//Defines the Apps a User is following
CREATE TABLE IF NOT EXISTS Banana.Follow_User_Followings
(
	user_id uuid,
	app_id uuid,
	user_first_name text,
	app_name text,
	time_of_follow timestamp,

	PRIMARY KEY ((user_id), app_id)
);
