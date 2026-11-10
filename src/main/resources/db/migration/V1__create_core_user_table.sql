CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) NOT NULL,
    username VARCHAR(30) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- Legacy membership_id and legacy tables are intentionally not removed here.
-- Existing installations keep all data until a separately approved archival migration.
