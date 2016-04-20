//Assumes the existence of a 'Aroma' Keyspace

/*
In these tables, an owner can be either a User or an Application.
Both are expected to be unique.
*/
CREATE TABLE IF NOT EXISTS Aroma.Reactions
(
	owner_id uuid,
	list<string> serializedReactions,

	PRIMARY KEY (token_id)
);
