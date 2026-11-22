ALTER TABLE users
    ADD COLUMN accepted_terms_version VARCHAR(32) NULL,
    ADD COLUMN terms_accepted_at DATETIME(6) NULL;
