//Assumes the existence of a 'Aroma' Keyspace

// Used to store binary types like Images and Videos.
CREATE TABLE IF NOT EXISTS Aroma.Media
(
	media_id uuid,
	media_type text,
	width int,
	height int,
	creation_time timestamp,
	expiration_time timestamp,
	binary blob,

	PRIMARY KEY (media_id)
)
WITH gc_grace_seconds=43200
;

//Used to store thumbnails for images, allowing for quick retrieval
CREATE TABLE IF NOT EXISTS Aroma.Media_Thumbnails
(
	media_id uuid,
	dimension text,
	media_type text,
	width int,
	height int,
	creation_time timestamp,
	expiration_time timestamp,
	binary blob,

	PRIMARY KEY ((media_id), dimension)
)
WITH gc_grace_seconds = 43200
;
