---------------------------------------
-- REMOVES A MEMBER FROM AN ORGANIZATION
---------------------------------------
DELETE
FROM Organization_Members
WHERE organization_id = ?
AND user_id = ?
