CREATE TABLE support_messages (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 category VARCHAR(30) NOT NULL,
 subject VARCHAR(150) NOT NULL,
 description TEXT NOT NULL,
 reply_email VARCHAR(254) NOT NULL,
 status VARCHAR(30) NOT NULL DEFAULT 'NEW',
 attachment_key VARCHAR(40) NULL,
 created_at DATETIME(6) NOT NULL,
 updated_at DATETIME(6) NOT NULL,
 INDEX ix_support_filter (category, status, created_at)
);

-- Persisted, shared across instances/restarts; no raw IP addresses are stored.
CREATE TABLE support_rate_limits (
 client_key CHAR(64) NOT NULL PRIMARY KEY,
 window_start DATETIME(6) NOT NULL,
 attempts INT NOT NULL
);
