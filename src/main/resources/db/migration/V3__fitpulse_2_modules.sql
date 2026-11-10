CREATE TABLE IF NOT EXISTS cities (
    id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(100) NOT NULL, slug VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE, PRIMARY KEY (id), UNIQUE (name), UNIQUE (slug)
);
CREATE TABLE IF NOT EXISTS gyms (
    id BIGINT NOT NULL AUTO_INCREMENT, city_id BIGINT NOT NULL, name VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL, description VARCHAR(3000) NOT NULL, address VARCHAR(300) NOT NULL,
    latitude DECIMAL(9,6), longitude DECIMAL(9,6), phone VARCHAR(50), website_url VARCHAR(500),
    opening_hours VARCHAR(1000), services VARCHAR(1500), price_info VARCHAR(1000), status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL, PRIMARY KEY (id), UNIQUE (slug),
    CONSTRAINT fk_gyms_city FOREIGN KEY (city_id) REFERENCES cities(id)
);
CREATE TABLE IF NOT EXISTS gym_photos (
    id BIGINT NOT NULL AUTO_INCREMENT, gym_id BIGINT NOT NULL, image_url VARCHAR(1000) NOT NULL,
    alt_text VARCHAR(250) NOT NULL, sort_order INT NOT NULL DEFAULT 0, cover_photo BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id), CONSTRAINT fk_gym_photos_gym FOREIGN KEY (gym_id) REFERENCES gyms(id)
);
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT NOT NULL AUTO_INCREMENT, gym_id BIGINT NOT NULL, user_id BINARY(16) NOT NULL,
    rating INT NOT NULL, comment VARCHAR(2000), status VARCHAR(30) NOT NULL, created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL, PRIMARY KEY (id), UNIQUE (gym_id,user_id),
    CONSTRAINT fk_reviews_gym FOREIGN KEY (gym_id) REFERENCES gyms(id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS review_reports (
    id BIGINT NOT NULL AUTO_INCREMENT, review_id BIGINT NOT NULL, reporter_id BINARY(16) NOT NULL,
    reason VARCHAR(500) NOT NULL, status VARCHAR(30) NOT NULL, created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id), UNIQUE (review_id,reporter_id),
    CONSTRAINT fk_reports_review FOREIGN KEY (review_id) REFERENCES reviews(id),
    CONSTRAINT fk_reports_user FOREIGN KEY (reporter_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS favorite_gyms (
    id BIGINT NOT NULL AUTO_INCREMENT, user_id BINARY(16) NOT NULL, gym_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL, PRIMARY KEY (id), UNIQUE (user_id,gym_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_favorites_gym FOREIGN KEY (gym_id) REFERENCES gyms(id)
);
CREATE TABLE IF NOT EXISTS gym_suggestions (
    id BIGINT NOT NULL AUTO_INCREMENT, submitted_by BINARY(16), name VARCHAR(150) NOT NULL,
    city_name VARCHAR(100) NOT NULL, address VARCHAR(300) NOT NULL, website_url VARCHAR(500),
    details VARCHAR(2000) NOT NULL, status VARCHAR(30) NOT NULL, admin_note VARCHAR(1000),
    created_at DATETIME(6) NOT NULL, PRIMARY KEY (id),
    CONSTRAINT fk_suggestions_user FOREIGN KEY (submitted_by) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS workout_events (
    id BIGINT NOT NULL AUTO_INCREMENT, user_id BINARY(16) NOT NULL, gym_id BIGINT,
    title VARCHAR(150) NOT NULL, notes VARCHAR(1500), starts_at DATETIME(6) NOT NULL,
    duration_minutes INT NOT NULL, status VARCHAR(30) NOT NULL, created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id), CONSTRAINT fk_events_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_events_gym FOREIGN KEY (gym_id) REFERENCES gyms(id)
);
CREATE TABLE IF NOT EXISTS program_templates (
    id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(150) NOT NULL, goal VARCHAR(30) NOT NULL,
    experience VARCHAR(30) NOT NULL, training_days INT NOT NULL, location VARCHAR(30) NOT NULL,
    description VARCHAR(1500) NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE, PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS program_template_items (
    id BIGINT NOT NULL AUTO_INCREMENT, template_id BIGINT NOT NULL, day_number INT NOT NULL,
    exercise_name VARCHAR(150) NOT NULL, sets_count INT NOT NULL, repetitions VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL, PRIMARY KEY (id), CONSTRAINT fk_template_items_template FOREIGN KEY (template_id) REFERENCES program_templates(id)
);
CREATE TABLE IF NOT EXISTS user_programs (
    id BIGINT NOT NULL AUTO_INCREMENT, user_id BINARY(16) NOT NULL, source_template_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE, created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id), CONSTRAINT fk_user_programs_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_programs_template FOREIGN KEY (source_template_id) REFERENCES program_templates(id)
);
CREATE TABLE IF NOT EXISTS user_program_items (
    id BIGINT NOT NULL AUTO_INCREMENT, user_program_id BIGINT NOT NULL, day_number INT NOT NULL,
    exercise_name VARCHAR(150) NOT NULL, sets_count INT NOT NULL, repetitions VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL, PRIMARY KEY (id), CONSTRAINT fk_user_items_program FOREIGN KEY (user_program_id) REFERENCES user_programs(id)
);
CREATE TABLE IF NOT EXISTS fitness_questionnaires (
    id BIGINT NOT NULL AUTO_INCREMENT, user_id BINARY(16) NOT NULL, goal VARCHAR(30) NOT NULL,
    experience VARCHAR(30) NOT NULL, training_days INT NOT NULL, location VARCHAR(30) NOT NULL,
    updated_at DATETIME(6) NOT NULL, PRIMARY KEY (id), UNIQUE(user_id),
    CONSTRAINT fk_questionnaires_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE IF NOT EXISTS meal_plan_templates (
    id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(150) NOT NULL, goal VARCHAR(30) NOT NULL,
    description VARCHAR(2000) NOT NULL, meals VARCHAR(4000) NOT NULL, disclaimer VARCHAR(1000) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE, PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS user_meal_plans (
    id BIGINT NOT NULL AUTO_INCREMENT, user_id BINARY(16) NOT NULL, source_template_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL, meals VARCHAR(4000) NOT NULL, created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id), CONSTRAINT fk_user_meals_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_meals_template FOREIGN KEY (source_template_id) REFERENCES meal_plan_templates(id)
);
ALTER TABLE users ADD COLUMN display_name VARCHAR(80);
ALTER TABLE users ADD COLUMN bio VARCHAR(500);
ALTER TABLE users ADD COLUMN home_city VARCHAR(100);
ALTER TABLE users ADD COLUMN public_profile BOOLEAN NOT NULL DEFAULT FALSE;

INSERT INTO program_templates(name,goal,experience,training_days,location,description,active) VALUES
('Старт у дома 3 дни','GENERAL','BEGINNER',3,'HOME','Основни движения за изграждане на навик.',TRUE),
('Старт във фитнес 3 дни','GENERAL','BEGINNER',3,'GYM','Цялостна програма с машини и свободни тежести.',TRUE),
('Отслабване у дома 4 дни','FAT_LOSS','BEGINNER',4,'HOME','Умерени силови и кондиционни сесии.',TRUE),
('Отслабване във фитнес 4 дни','FAT_LOSS','BEGINNER',4,'GYM','Силови тренировки с постепенно натоварване.',TRUE),
('Сила 3 дни','STRENGTH','INTERMEDIATE',3,'GYM','Фокус върху основните многоставни движения.',TRUE),
('Сила 4 дни','STRENGTH','INTERMEDIATE',4,'GYM','Upper/lower разпределение за сила.',TRUE),
('Мускулна маса 4 дни','MUSCLE','INTERMEDIATE',4,'GYM','Хипертрофия с балансиран обем.',TRUE),
('Мускулна маса 5 дни','MUSCLE','ADVANCED',5,'GYM','По-висок обем за напреднали.',TRUE),
('Функционална форма 3 дни','GENERAL','INTERMEDIATE',3,'HOME','Мобилност, сила и кондиция без специална техника.',TRUE),
('Функционална форма 4 дни','GENERAL','INTERMEDIATE',4,'GYM','Комбинация от тежести и кондиция.',TRUE),
('Начинаещ 2 дни','GENERAL','BEGINNER',2,'GYM','Кратка програма за натоварен график.',TRUE),
('Домашна сила 3 дни','STRENGTH','INTERMEDIATE',3,'HOME','Собствено тегло и базово оборудване.',TRUE),
('Активно възстановяване 3 дни','MOBILITY','BEGINNER',3,'HOME','Леки тренировки за мобилност и обща активност.',TRUE),
('Атлетична подготовка 4 дни','PERFORMANCE','ADVANCED',4,'GYM','Сила, експлозивност и кондиция.',TRUE),
('Издръжливост 5 дни','ENDURANCE','INTERMEDIATE',5,'HOME','Постепенно развитие на общата издръжливост.',TRUE);

INSERT INTO meal_plan_templates(name,goal,description,meals,disclaimer,active) VALUES
('Балансиран примерен ден','GENERAL','Пример за разнообразно ежедневно меню.','Закуска: овес и плод; Обяд: ориз, зеленчуци и протеин; Вечеря: салата и протеин; Междинно: кисело мляко.','Общ образователен пример, не медицински или индивидуален хранителен съвет. При заболяване, алергии, бременност или хранително разстройство се консултирайте с квалифициран специалист.',TRUE),
('Пример за активен ден','PERFORMANCE','Пример с въглехидрати около тренировка.','Закуска: яйца и пълнозърнест хляб; Преди тренировка: банан; Обяд: картофи и протеин; Вечеря: зеленчуци и бобови.','Общ образователен пример, не медицински или индивидуален хранителен съвет.',TRUE),
('Вегетариански пример','GENERAL','Примерно меню без месо.','Закуска: овес; Обяд: леща и салата; Вечеря: тофу със зеленчуци; Междинно: плод и ядки.','Проверявайте алергени и се консултирайте със специалист при специфични потребности.',TRUE);
