//Assumes the existence of a 'Banana' Keyspace

CREATE TABLE IF NOT EXISTS Banana.Inbox
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

	PRIMARY KEY ((user_id), message_id)
);
