//Assumes the existence of a 'Banana' Keyspace

CREATE TABLE IF NOT EXISTS Banana.Applications
(
	app_id UUID,
	name text,
	organization_id UUID,
	organization_name text,
	owners list<UUID>,
	programming_language text,
	time_provisioned timestamp,
	tier text,

	PRIMARY KEY (app_id)
);

CREATE INDEX IF NOT EXISTS Applications_By_Owners ON Banana.Applications (owners);

CREATE INDEX IF NOT EXISTS Applications_By_Organization ON Banana.Applications (organization_id);

CREATE TABLE IF NOT EXISTS Banana.Applications_Recently_Created
(
	app_id UUID,
	name text,
	organization_id UUID,
	organization_name text,
	owners list<UUID>,
	programming_language text,
	time_provisioned timestamp,
	tier text,

	PRIMARY KEY (app_id)
);
