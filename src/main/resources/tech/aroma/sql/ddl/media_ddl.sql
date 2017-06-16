---------------------------------------
-- DDL FOR AROMA MEDIA
--
-- Media includes images, videos, and other binary information
-- used by Aroma.
---------------------------------------

CREATE TABLE IF NOT EXISTS Media
(
    media_id        UUID,
    media_type      TEXT,
    width           INT,
    height          INT,
    creation_time   TIMESTAMPTZ DEFAULT now(),
    data            BYTEA,

    PRIMARY KEY (media_id)
)
;

-- Used to store thumbnails for images,
-- allowing for quick retrievals
CREATE TABLE IF NOT EXISTS Media_Thumbnails
(
    media_id        UUID,
    width           INT,
    height          INT,
    media_type      TEXT,
    creation_time   TIMESTAMPTZ DEFAULT now(),
    data            BYTEA,

    PRIMARY KEY (media_id, width, height)
)
;