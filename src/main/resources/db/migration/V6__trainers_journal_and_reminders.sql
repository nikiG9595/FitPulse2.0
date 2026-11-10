CREATE TABLE trainers (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(150) NOT NULL, age INT NOT NULL, country VARCHAR(100) NOT NULL,
 experience_years INT NOT NULL, bio VARCHAR(2000) NOT NULL,
 photo_url VARCHAR(1000) NOT NULL, published BOOLEAN NOT NULL DEFAULT FALSE
);
INSERT INTO trainers(name,age,country,experience_years,bio,photo_url,published)
VALUES ('Charles Walker',36,'САЩ',12,'Треньор с 12 години опит.','/css/images/CharlesTrainer.png',TRUE);
ALTER TABLE workout_events ADD COLUMN program_id BIGINT NULL;
ALTER TABLE workout_events ADD COLUMN meal_plan_id BIGINT NULL;
ALTER TABLE workout_events ADD COLUMN reminder_minutes INT NOT NULL DEFAULT 30;
ALTER TABLE workout_events ADD COLUMN reminder_dismissed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE workout_events ADD CONSTRAINT fk_events_program FOREIGN KEY(program_id) REFERENCES user_programs(id) ON DELETE SET NULL;
ALTER TABLE workout_events ADD CONSTRAINT fk_events_meal FOREIGN KEY(meal_plan_id) REFERENCES user_meal_plans(id) ON DELETE SET NULL;
CREATE TABLE workout_logs (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, user_id BINARY(16) NOT NULL,
 event_id BIGINT NULL, title VARCHAR(150) NOT NULL, performed_at DATETIME(6) NOT NULL, notes VARCHAR(1500),
 CONSTRAINT fk_logs_user FOREIGN KEY(user_id) REFERENCES users(id),
 CONSTRAINT fk_logs_event FOREIGN KEY(event_id) REFERENCES workout_events(id) ON DELETE SET NULL
);
CREATE INDEX ix_logs_user_date ON workout_logs(user_id,performed_at);
CREATE INDEX ix_events_user_date ON workout_events(user_id,starts_at);
CREATE TABLE workout_sets (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, workout_log_id BIGINT NOT NULL,
 exercise_name VARCHAR(150) NOT NULL, set_number INT NOT NULL, repetitions INT NOT NULL,
 weight DECIMAL(7,2) NOT NULL,
 CONSTRAINT fk_sets_log FOREIGN KEY(workout_log_id) REFERENCES workout_logs(id) ON DELETE CASCADE
);
