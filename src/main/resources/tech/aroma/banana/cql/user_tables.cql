//Assumes the existence of a 'Aroma' Keyspace

CREATE TABLE IF NOT EXISTS Aroma.Users
(
	user_id uuid,
	first_name text,
	middle_name text,
	last_name text,
	emails set<text>,
	organizations set<uuid>,
	roles set<text>,
	gender text,
	birthdate timestamp,
	profile_image_id text,
	github_profile text,
	time_account_created timestamp,

	PRIMARY KEY (user_id)
);

CREATE INDEX IF NOT EXISTS Users_By_Last_Name_Index ON Aroma.Users (last_name);
CREATE INDEX IF NOT EXISTS Users_By_First_Name_Index ON Aroma.Users (first_name);
CREATE INDEX IF NOT EXISTS Users_By_Roles_Index ON Aroma.Users (roles);

CREATE TABLE IF NOT EXISTS  Aroma.Users_By_Github_Profile
(
	github_profile text,
	user_id uuid,
	first_name text,
	middle_name text,
	last_name text,
	//This is the email associated with the GitHub account
	email text,
	time_account_created timestamp,

	PRIMARY KEY (github_profile)
);

CREATE TABLE IF NOT EXISTS Aroma.Users_By_Email
(
	email text,
	user_id uuid,
	first_name text,
	middle_name text,
	last_name text,
	github_profile text,
	profile_image_id text,
	time_account_created timestamp,

	PRIMARY KEY (email)
);

CREATE TABLE IF NOT EXISTS Aroma.Users_Recent
(
	user_id uuid,
	first_name text,
	middle_name text,
	last_name text,
	emails set<text>,
	organizations set<uuid>,
	roles set<text>,
	gender text,
	birthdate timestamp,
	profile_image_id text,
	github_profile text,
	time_account_created timestamp,

	PRIMARY KEY (user_id)
);
