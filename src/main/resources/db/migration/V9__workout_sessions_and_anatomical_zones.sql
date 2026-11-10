ALTER TABLE meal_plan_templates ADD COLUMN original_weekly_menu LONGTEXT NULL;
ALTER TABLE exercises ADD COLUMN primary_zones VARCHAR(500) NULL;
ALTER TABLE exercises ADD COLUMN secondary_zones VARCHAR(500) NULL;
CREATE TABLE workout_sessions (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,user_id BINARY(16) NOT NULL,program_id BIGINT NULL,
 day_number INT NOT NULL,title VARCHAR(150) NOT NULL,status VARCHAR(20) NOT NULL,
 active_key VARCHAR(120) NULL UNIQUE,created_at DATETIME(6) NOT NULL,updated_at DATETIME(6) NOT NULL,
 finished_log_id BIGINT NULL UNIQUE,timer_remaining INT NOT NULL DEFAULT 90,timer_deadline BIGINT NULL,
 CONSTRAINT fk_workout_session_user FOREIGN KEY(user_id) REFERENCES users(id),
 CONSTRAINT fk_workout_session_program FOREIGN KEY(program_id) REFERENCES user_programs(id) ON DELETE SET NULL,
 CONSTRAINT fk_workout_session_log FOREIGN KEY(finished_log_id) REFERENCES workout_logs(id) ON DELETE SET NULL
);
CREATE INDEX ix_workout_session_user ON workout_sessions(user_id,created_at);
CREATE TABLE workout_session_sets (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,session_id BIGINT NOT NULL,exercise_id BIGINT NULL,
 exercise_name VARCHAR(150) NOT NULL,exercise_order INT NOT NULL,set_number INT NOT NULL,
 planned_repetitions VARCHAR(50) NOT NULL,rest_seconds INT NOT NULL,repetitions INT NULL,
 weight DECIMAL(7,2) NULL,completed BOOLEAN NOT NULL DEFAULT FALSE,
 CONSTRAINT fk_session_set_session FOREIGN KEY(session_id) REFERENCES workout_sessions(id),
 CONSTRAINT fk_session_set_exercise FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON DELETE SET NULL,
 CONSTRAINT uk_session_set_order UNIQUE(session_id,exercise_order,set_number)
);
