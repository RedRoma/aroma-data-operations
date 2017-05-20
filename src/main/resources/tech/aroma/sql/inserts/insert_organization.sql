------------------------------------------------------------------------------
-- INSERTS A NEW ORGANIZATION INTO THE TABLE
------------------------------------------------------------------------------

INSERT INTO aroma.public.organizations (organization_id, organization_name, owners, icon_link, industry, contact_email, github_profile, stock_name, tier, description, website)
VALUES (?, ?, string_to_array(?, ','), ?, ?, ?, ?, ?, ?, ?, ?)