------------------------------------------------------------------------------
-- CREATES OR UPDATES A USER
------------------------------------------------------------------------------
INSERT INTO users (user_id, first_name, middle_name, last_name, emails, roles, gender, birthdate, profile_image_id, github_profile)
VALUES (?, ?, ?, ?, string_to_array(?, ','), string_to_array(?, ','), ?, ?, ?, ?)
ON CONFLICT (user_id)
    DO UPDATE
        SET
            first_name        = EXCLUDED.first_name,
            middle_name       = EXCLUDED.middle_name,
            last_name         = EXCLUDED.last_name,
            emails            = EXCLUDED.emails,
            roles             = Excluded.roles,
            gender            = EXCLUDED.gender,
            birthdate         = EXCLUDED.birthdate,
            profile_image_id  = EXCLUDED.profile_image_id,
            github_profile    = EXCLUDED.github_profile,
            time_last_updated = EXCLUDED.time_last_updated