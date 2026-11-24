ALTER TABLE user_meal_plans ADD COLUMN intake_selected BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE daily_meal_intake (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BINARY(16) NOT NULL,
    plan_id BIGINT NOT NULL,
    intake_date DATE NOT NULL,
    meal_index INT NOT NULL,
    meal_revision VARCHAR(64) NOT NULL,
    calories DOUBLE NOT NULL,
    protein DOUBLE NOT NULL,
    carbs DOUBLE NOT NULL,
    fat DOUBLE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_daily_meal_intake UNIQUE (user_id, plan_id, intake_date, meal_index, meal_revision),
    CONSTRAINT fk_daily_meal_intake_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
