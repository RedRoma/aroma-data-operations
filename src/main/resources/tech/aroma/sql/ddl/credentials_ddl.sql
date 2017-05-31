---------------------------------------
-- DDL FOR AROMA CREDENTIALS
--
-- Used to store encrypted user passwords.
---------------------------------------

CREATE TABLE IF NOT EXISTS Credentials
(
    user_id            UUID,
    encrypted_password TEXT,
    time_created       TIMESTAMP DEFAULT now(),

    PRIMARY KEY (user_id)
)
;