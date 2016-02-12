//Assumes the existence of a 'Aroma' Keyspace

CREATE TABLE IF NOT EXISTS Aroma.Applications
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

CREATE INDEX IF NOT EXISTS Applications_By_Owners ON Aroma.Applications (owners);

CREATE INDEX IF NOT EXISTS Applications_By_Organization ON Aroma.Applications (organization_id);

CREATE TABLE IF NOT EXISTS Aroma.Applications_Recently_Created
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
