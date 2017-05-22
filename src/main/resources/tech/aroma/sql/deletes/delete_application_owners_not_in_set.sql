------------------------------------------------------------------------------
-- REMOVES ALL AN APPLICATION'S OWNERS THAT ARE NOT
-- IN THE SPECIFIED ARRAY
------------------------------------------------------------------------------

DELETE
FROM application_owners
WHERE owner_id != ALL (string_to_array(?, ',')::UUID[])
