//Assumes the existence of a 'Banana' Keyspace

CREATE TABLE IF NOT EXISTS Banana.Messages
(
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

	PRIMARY KEY ((app_id), message_id)
)
WITH gc_grace_seconds=84000
AND compaction = {'class' : 'DateTieredCompactionStrategy' }
;

CREATE INDEX IF NOT EXISTS Messages_By_Title ON Banana.Messages (title);

CREATE INDEX IF NOT EXISTS Messages_By_Hostname  ON Banana.Messages (hostname);

CREATE TABLE IF NOT EXISTS Banana.Messages_Rate_Limit
(
	app_id uuid,
	request_time timeuuid,

	PRIMARY KEY ((app_id), request_time)
);

CREATE TABLE IF NOT EXISTS Banana.Messages_Totals_By_App
(
	app_id uuid,
	total_messages counter,

	PRIMARY KEY (app_id)
);

CREATE TABLE IF NOT EXISTS Banana.Messages_Totals_By_Title
(
	app_id uuid,
	title text,
	total_messages counter,

	PRIMARY KEY ((app_id), title)
);
