//Assumes the existence of a 'Banana' Keyspace

/* Stores Organization Information */
CREATE TABLE IF NOT EXISTS Banana.Organizations
(
	org_id uuid,
	org_name text,
	owners set<uuid>,
	icon_link text,
	industry text,
	contact_email text,
	github_profile text,
	stock_name text,
	tier text,
	description text,
	website text,

	PRIMARY KEY (org_id)
);

CREATE INDEX IF NOT EXISTS Organizations_By_Tier ON Banana.Organizations (tier);

CREATE INDEX IF NOT EXISTS Organizations_By_Industry ON Banana.Organizations (industry);

/* Stores information relating to an Organization's members */
CREATE TABLE IF NOT EXISTS Banana.Organizations_Members
(
	org_id uuid,
	user_id uuid,
	org_name text,
	user_first_name text,
	user_middle_name text,
	user_last_name text,
	user_roles set<text>,
	user_email text,

	PRIMARY KEY ((org_id), user_id)
);

CREATE INDEX IF NOT EXISTS Organizations_Members_By_Email ON Banana.Organizations_Members (user_email);

CREATE INDEX IF NOT EXISTS Organizations_Members_By_Role ON Banana.Organizations_Members (user_roles);

CREATE INDEX IF NOT EXISTS Organizations_Members_By_User ON Banana.Organizations_Members (user_id);
