---------------------------------------
-- FIND AN ORGANIZATION'S MEMBERS
---------------------------------------

SELECT user_id
FROM organization_members
WHERE organization_id = ?