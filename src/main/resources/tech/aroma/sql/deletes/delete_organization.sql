---------------------------------------
-- DELETES AN ORGANIZATION
---------------------------------------
DELETE
FROM Organizations
WHERE org_id = :org_id
;

DELETE FROM Organization_Owners
WHERE org_id = :org_id
;

DELETE FROM Organization_Members
WHERE org_id = :org_id
