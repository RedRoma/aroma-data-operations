---------------------------------------
-- REMOVES ALL OF THE OWNERS BELONGING TO AN ORGANIZATION
---------------------------------------

DELETE FROM Organization_Owners
WHERE org_id = ?