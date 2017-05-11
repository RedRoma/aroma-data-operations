------------------------------------------------------------------------------
-- CREATES OR UPDATES AN APPLICATION
------------------------------------------------------------------------------

INSERT INTO applications (app_id, name, app_description, organization_id, programming_language, tier, time_of_token_expiration, app_icon_media_id)
VALUES (?, ?, ?, ?, ?, ?, ?, ?)
ON CONFLICT (app_id)
    DO UPDATE
        SET name                     = EXCLUDED.name,
            app_description          = EXCLUDED.app_description,
            organization_id          = EXCLUDED.organization_id,
            programming_language     = EXCLUDED.programming_language,
            time_last_updated        = now(),
            tier                     = EXCLUDED.tier,
            time_of_token_expiration = EXCLUDED.time_of_token_expiration,
            app_icon_media_id        = EXCLUDED.app_icon_media_id