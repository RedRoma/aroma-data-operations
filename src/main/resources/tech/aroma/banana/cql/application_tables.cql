//Assumes the existence of a 'Banana' Keyspace

CREATE TABLE IF NOT EXISTS Banana.Applications
(
	app_id uuid,
	name text,
	app_description text,
	organization_id uuid,
	organization_name text,
	owners set<uuid>,
	programming_language text,
	time_provisioned timestamp,
	tier text,
	time_of_token_expiration timestamp,

	PRIMARY KEY (app_id)
);

CREATE INDEX IF NOT EXISTS Applications_By_Owners ON Banana.Applications (owners);

CREATE INDEX IF NOT EXISTS Applications_By_Organization ON Banana.Applications (organization_id);

CREATE TABLE IF NOT EXISTS Banana.Applications_Recently_Created
(
	app_id uuid,
	name text,
	app_description text,
	organization_id uuid,
	organization_name text,
	owners set<uuid>,
	programming_language text,
	time_provisioned timestamp,
	tier text,
	time_of_token_expiration timestamp,

	PRIMARY KEY (app_id)
);
