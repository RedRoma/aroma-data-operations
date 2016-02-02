//Assumes the existence of a 'Banana' Keyspace

/*
	Used to store encrypted User Passwords.
*/
CREATE TABLE IF NOT EXISTS Banana.Credentials
(
	user_id uuid,
	encrypted_password text,
	time_created timestamp,

	PRIMARY KEY (user_id)
);
