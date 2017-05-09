---------------------------------------
-- REMOVES AN OWNER FROM AN ORGANIZATION
---------------------------------------

DELETE FROM Organization_Owners
WHERE org_id = ?
AND user_id = ?