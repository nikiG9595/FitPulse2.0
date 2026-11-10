CREATE TABLE IF NOT EXISTS progress_records (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    weight DECIMAL(6,2) NOT NULL,
    body_fat_percentage DECIMAL(5,2) NULL,
    recorded_at DATE NOT NULL,
    note VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_progress_records_user_recorded_at UNIQUE (user_id, recorded_at),
    CONSTRAINT fk_progress_records_user FOREIGN KEY (user_id) REFERENCES users (id)
);
