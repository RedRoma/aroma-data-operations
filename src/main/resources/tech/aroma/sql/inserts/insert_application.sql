------------------------------------------------------------------------------
-- CREATES OR UPDATES AN APPLICATION
------------------------------------------------------------------------------

INSERT INTO applications (app_id, app_name, app_description, organization_id, programming_language, tier, time_of_token_expiration, app_icon_media_id, owners)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, string_to_array(?, ',')::UUID[])
ON CONFLICT (app_id)
    DO UPDATE
        SET app_name                 = EXCLUDED.app_name,
            app_description          = EXCLUDED.app_description,
            organization_id          = EXCLUDED.organization_id,
            owners                   = EXCLUDED.owners,
            programming_language     = EXCLUDED.programming_language,
            time_last_updated        = now(),
            tier                     = EXCLUDED.tier,
            time_of_token_expiration = EXCLUDED.time_of_token_expiration,
            app_icon_media_id        = EXCLUDED.app_icon_media_id