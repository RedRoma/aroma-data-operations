---------------------------------------
-- FIND AN ORGANIZATION'S MEMBERS
---------------------------------------

SELECT user_id
FROM organization_members
WHERE org_id = ?