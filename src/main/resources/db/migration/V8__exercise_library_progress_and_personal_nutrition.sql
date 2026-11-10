CREATE TABLE exercises (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, code VARCHAR(80) NOT NULL UNIQUE,
 name VARCHAR(150) NOT NULL, muscle VARCHAR(50) NOT NULL, secondary_muscles VARCHAR(200) NOT NULL,
 difficulty VARCHAR(40) NOT NULL, equipment VARCHAR(100) NOT NULL, location VARCHAR(30) NOT NULL,
 start_position VARCHAR(1000) NOT NULL, steps VARCHAR(2000) NOT NULL, breathing VARCHAR(500) NOT NULL,
 posture VARCHAR(800) NOT NULL, mistakes VARCHAR(800) NOT NULL, safety VARCHAR(800) NOT NULL,
 easier VARCHAR(500) NOT NULL, harder VARCHAR(500) NOT NULL, aliases VARCHAR(800) NOT NULL, alternatives VARCHAR(500) NOT NULL
);
CREATE TABLE foods (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, code VARCHAR(80) NOT NULL UNIQUE, name VARCHAR(150) NOT NULL,
 state VARCHAR(120) NOT NULL, category VARCHAR(80) NOT NULL,
 protein DECIMAL(8,2) NOT NULL, fat DECIMAL(8,2) NOT NULL, carbs DECIMAL(8,2) NOT NULL
);
ALTER TABLE program_template_items ADD COLUMN exercise_id BIGINT NULL;
ALTER TABLE program_template_items ADD CONSTRAINT fk_template_exercise FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON DELETE SET NULL;
ALTER TABLE user_program_items ADD COLUMN exercise_id BIGINT NULL;
ALTER TABLE user_program_items ADD CONSTRAINT fk_user_program_exercise FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON DELETE SET NULL;
ALTER TABLE workout_sets ADD COLUMN exercise_id BIGINT NULL;
ALTER TABLE workout_sets ADD CONSTRAINT fk_workout_set_exercise FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON DELETE SET NULL;
ALTER TABLE user_meal_plans ADD COLUMN weekly_menu LONGTEXT NULL;
ALTER TABLE user_meal_plans ADD COLUMN goal VARCHAR(30) NULL;
CREATE TABLE body_measurements (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, user_id BINARY(16) NOT NULL, type VARCHAR(20) NOT NULL,
 recorded_at DATE NOT NULL, measurement_value DECIMAL(6,2) NOT NULL,
 CONSTRAINT fk_measurement_user FOREIGN KEY(user_id) REFERENCES users(id),
 CONSTRAINT uk_measurement_date UNIQUE(user_id,type,recorded_at)
);
CREATE TABLE shopping_checks (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, plan_id BIGINT NOT NULL, food_code VARCHAR(80) NOT NULL,
 scope VARCHAR(64) NOT NULL, checked BOOLEAN NOT NULL DEFAULT FALSE, grams DECIMAL(10,2) NOT NULL DEFAULT 0,
 CONSTRAINT fk_shopping_plan FOREIGN KEY(plan_id) REFERENCES user_meal_plans(id) ON DELETE CASCADE,
 CONSTRAINT uk_shopping_scope UNIQUE(plan_id,food_code,scope)
);
