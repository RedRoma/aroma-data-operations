//Assumes the existence of a 'Banana' Keyspace

/*
In these tables, an owner can be either a User or an Application,
depending on the Token Type.
*/
CREATE TABLE IF NOT EXISTS Banana.Tokens
(
	token_id uuid,
	owner_id uuid,
	owner_name text,
	features list<text>,
	time_of_expiration timestamp,
	time_of_creation timestamp,
	organization_id uuid,
	token_type text,

	PRIMARY KEY (token_id)
);

CREATE TABLE IF NOT EXISTS Banana.Tokens_By_Owner
(
	owner_id uuid,
	token_id uuid,
	owner_name text,
	time_of_creation timestamp,
	time_of_expiration timestamp,
	token_type text,

	PRIMARY KEY ((owner_id), token_id)
);
