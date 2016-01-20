//Assumes the existence of a 'Banana' Keyspace

CREATE TABLE IF NOT EXISTS Banana.Tokens_Applications
(
	token_id text,
	app_id uuid,
	features list<text>,
	time_of_expiration timestamp,
	organization_id uuid,

	PRIMARY KEY (token_id)
);

CREATE TABLE IF NOT EXISTS Banana.Tokens_Users
(
	user_id uuid,
	token_id text,
	time_of_creation timestamp,
	time_of_expiration timestamp,

	PRIMARY KEY (token_id)
);

CREATE TABLE IF NOT EXISTS Banana.Tokens_Users_By_User
(
	user_id uuid,
	token_id text,
	time_of_creation timestamp,
	time_of_expiration timestamp,

	PRIMARY KEY ((user_id), token_id)
);
