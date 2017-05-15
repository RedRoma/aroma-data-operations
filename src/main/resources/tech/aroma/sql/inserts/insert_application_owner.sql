------------------------------------------------------------------------------
-- ADDS AN OWNER TO TO AN APPLICATION
------------------------------------------------------------------------------

INSERT INTO application_owners (app_id, owner_id)
VALUES (?, ?)
ON CONFLICT DO NOTHING 