------------------------------------------------------------------------------
-- SELECTS ALL OF THE APPS A USER FOLLOWS
------------------------------------------------------------------------------

SELECT *
FROM Followings
    LEFT JOIN Applications USING (app_id)
WHERE user_id = ?