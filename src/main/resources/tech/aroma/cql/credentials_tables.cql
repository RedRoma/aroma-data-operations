//Assumes the existence of a 'Aroma' Keyspace

/*
	Used to store encrypted User Passwords.
*/
CREATE TABLE IF NOT EXISTS Aroma.Credentials
(
	user_id uuid,
	encrypted_password text,
	time_created timestamp,

	PRIMARY KEY (user_id)
);
