//Assumes the existence of a 'Banana' Keyspace

CREATE TABLE IF NOT EXISTS Banana.Users
(
	user_id UUID,
	first_name text,
	middle_name text,
	last_name text,
	emails set<text>,
	organizations set<UUID>,
	roles set<text>,
	gender text,
	birthdate timestamp,
	profile_image_id UUID,
	github_profile text,

	PRIMARY KEY (user_id)
);

CREATE INDEX IF NOT EXISTS Banana.Users_By_Last_Name_Index ON Banana.Users (last_name);
CREATE INDEX IF NOT EXISTS Banana.Users_By_First_Name_Index ON Banana.Users (first_name);
CREATE INDEX IF NOT EXISTS Banana.Users_By_Roles_Index ON Banana.Users (roles);

CREATE TABLE IF NOT EXISTS  Banana.Users_By_Github_Profile
(
	github_profile text,
	user_id UUID,
	first_name text,
	middle_name text,
	last_name text,
	emails set<text>,

	PRIMARY KEY (github_profile)
);

CREATE TABLE IF NOT EXISTS Banana.Users_By_Email
(
	email_address text,
	user_id UUID,
	first_name text,
	middle_name text,
	last_name text,
	github_profile text,
	profile_image_id UUID,

	PRIMARY KEY (email_address)
);
