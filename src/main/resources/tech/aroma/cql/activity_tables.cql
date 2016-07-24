//Assumes the existence of a 'Aroma' Keyspace

// This Table Stores User Activity in Aroma
CREATE TABLE IF NOT EXISTS Aroma.Activity
(
	// User ID of the intended recipient
	user_id uuid,
	//The ID of the event
	event_id uuid,
	//The Application in Question
	app_id uuid,
	//The User ID of the person performing the action
	actor_id uuid,
	time_of_event timestamp,
	serialized_event text,

	PRIMARY KEY ((user_id), event_id)
)
WITH gc_grace_seconds=600
AND compaction = {'class' : 'DateTieredCompactionStrategy' }
;

CREATE INDEX IF NOT EXISTS Activity_By_Application ON Aroma.Activity (app_id);
