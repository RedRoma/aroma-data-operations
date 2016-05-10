//Assumes the existence of a 'Aroma' Keyspace

CREATE TABLE IF NOT EXISTS Aroma.Inbox
(
	user_id uuid,
	message_id timeuuid,
	title text,
	body text,
	urgency text,
	time_created timestamp,
	time_received timestamp,
	hostname text,
	mac_address text,
	ip_address text,
	app_id uuid,
	app_name text,
	device_name text,

	PRIMARY KEY ((user_id), message_id)
)
WITH gc_grace_seconds=600
AND compaction = {'class' : 'DateTieredCompactionStrategy' }
;

CREATE TABLE IF NOT EXISTS Aroma.Inbox_By_Application
(
	user_id uuid,
	message_id timeuuid,
	title text,
	body text,
	urgency text,
	time_created timestamp,
	time_received timestamp,
	hostname text,
	device_name,
	mac_address text,
	ip_address text,
	app_id uuid,
	app_name text

	PRIMARY KEY ((user_id), app_id, message_id)
)
WITH gc_grace_seconds=600
AND compaction = {'class' : 'DateTieredCompactionStrategy' }
;


CREATE TABLE IF NOT EXISTS Aroma.Inbox_Count_By_Application
(
	user_id uuid,
	app_id uuid,
	counter static int,

	PRIMARY KEY ((user_id), app_id)
);
