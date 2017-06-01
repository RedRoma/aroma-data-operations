------------------------------------------------------------------------------
-- SELECTS ALL OF AN APP'S FOLLOWERS
------------------------------------------------------------------------------

SELECT *
FROM Followings
    LEFT JOIN Users USING (user_id)
WHERE app_id = ?