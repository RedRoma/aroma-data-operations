---------------------------------------
-- REMOVES A MEMBER FROM AN ORGANIZATION
---------------------------------------
DELETE FROM Organization_Members
WHERE org_id = ?
AND user_id = ?
