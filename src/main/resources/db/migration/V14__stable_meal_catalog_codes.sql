-- Bind existing catalog records to stable keys. Names, IDs, goals and menus remain unchanged.
-- The pre-existing UNIQUE(demo_code) rejects ambiguous duplicate records instead of choosing one silently.
UPDATE meal_plan_templates SET demo_code='vegetarian'
 WHERE demo_code IS NULL AND name IN ('Вегетариански пример','Вегетариански режим');
UPDATE meal_plan_templates SET demo_code='mediterranean'
 WHERE demo_code IS NULL AND name IN ('Средиземноморски пример','Средиземноморски режим');
UPDATE meal_plan_templates SET demo_code='active'
 WHERE demo_code IS NULL AND name IN ('Пример за активен ден','Хранителен режим за активно спортуващи','Режим за активно спортуващи');
UPDATE meal_plan_templates SET demo_code='fat-loss'
 WHERE demo_code IS NULL AND name IN ('Хранителен режим за отслабване','Режим за отслабване');
UPDATE meal_plan_templates SET demo_code='maintenance'
 WHERE demo_code IS NULL AND name IN ('Хранителен режим за поддържане на теглото','Режим за поддържане на теглото');
UPDATE meal_plan_templates SET demo_code='muscle'
 WHERE demo_code IS NULL AND name IN ('Хранителен режим за покачване на мускулна маса','Режим за покачване на мускулна маса');
