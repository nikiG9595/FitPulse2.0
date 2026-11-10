-- Membership visibility only. Existing copies, duplicates, histories and dependent rows remain intact.
ALTER TABLE user_programs ADD COLUMN listed BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE user_meal_plans ADD COLUMN listed BOOLEAN NOT NULL DEFAULT TRUE;
