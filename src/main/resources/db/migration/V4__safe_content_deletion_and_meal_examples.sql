ALTER TABLE user_programs DROP FOREIGN KEY fk_user_programs_template;
ALTER TABLE user_programs MODIFY source_template_id BIGINT NULL;
ALTER TABLE user_programs ADD CONSTRAINT fk_user_programs_template FOREIGN KEY (source_template_id) REFERENCES program_templates(id) ON DELETE SET NULL;
ALTER TABLE user_meal_plans DROP FOREIGN KEY fk_user_meals_template;
ALTER TABLE user_meal_plans MODIFY source_template_id BIGINT NULL;
ALTER TABLE user_meal_plans ADD CONSTRAINT fk_user_meals_template FOREIGN KEY (source_template_id) REFERENCES meal_plan_templates(id) ON DELETE SET NULL;
ALTER TABLE gym_photos DROP FOREIGN KEY fk_gym_photos_gym;
ALTER TABLE gym_photos ADD CONSTRAINT fk_gym_photos_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;
ALTER TABLE review_reports DROP FOREIGN KEY fk_reports_review;
ALTER TABLE review_reports ADD CONSTRAINT fk_reports_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE;
ALTER TABLE reviews DROP FOREIGN KEY fk_reviews_gym;
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;
ALTER TABLE favorite_gyms DROP FOREIGN KEY fk_favorites_gym;
ALTER TABLE favorite_gyms ADD CONSTRAINT fk_favorites_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;
ALTER TABLE workout_events DROP FOREIGN KEY fk_events_gym;
ALTER TABLE workout_events ADD CONSTRAINT fk_events_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE SET NULL;
ALTER TABLE meal_plan_templates ADD COLUMN example_foods VARCHAR(3000) NOT NULL DEFAULT '';
ALTER TABLE user_meal_plans ADD COLUMN example_foods VARCHAR(3000) NOT NULL DEFAULT '';
UPDATE meal_plan_templates SET example_foods = 'Овесени ядки, плодове, кисело мляко, яйца, ориз, картофи, бобови храни, зеленчуци, ядки и източник на протеин.' WHERE example_foods = '';
INSERT INTO meal_plan_templates(name, goal, description, meals, example_foods, disclaimer, active) VALUES
('Средиземноморски пример', 'GENERAL', 'Разнообразен пример с растителни храни, риба и пълнозърнести източници.', 'Закуска: кисело мляко с овес и плод; Обяд: салата, пълнозърнест хляб и риба; Вечеря: зеленчуци и бобови; Междинно: плод и ядки.', 'Домати, краставици, маслини, боб, леща, пълнозърнест хляб, риба, кисело мляко, плодове и ядки.', 'Образователен пример, а не индивидуална медицинска или хранителна препоръка.', TRUE),
('Пример с повече протеин', 'MUSCLE', 'Примерно разпределение на протеин между основните хранения.', 'Закуска: яйца и зеленчуци; Обяд: ориз и избран протеин; Следобед: кисело мляко; Вечеря: картофи, зеленчуци и избран протеин.', 'Яйца, кисело мляко, извара, пилешко месо, риба, тофу, бобови храни, ориз, картофи и зеленчуци.', 'Количествата зависят от индивидуалните нужди. При медицинско състояние се обърнете към квалифициран специалист.', TRUE);
