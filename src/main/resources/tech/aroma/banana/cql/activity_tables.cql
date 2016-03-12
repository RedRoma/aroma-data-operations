//Assumes the existence of a 'Aroma' Keyspace

CREATE TABLE IF NOT EXISTS Aroma.Activity
(
	user_id uuid,
	event_id timeuuid,
	app_id uuid,
	time_of_event timestamp,
	serialized_event text,

	PRIMARY KEY ((user_id), event_id)
)
WITH gc_grace_seconds=600
AND compaction = {'class' : 'DateTieredCompactionStrategy' }
;

CREATE INDEX IF NOT EXISTS Activity_By_Application ON Aroma.Activity (app_id);
