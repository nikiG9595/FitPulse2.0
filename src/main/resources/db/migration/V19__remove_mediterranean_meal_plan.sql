-- V17 is already applied; never change its checksum.
-- Personal menus, ingredient snapshots, shopping checks and recipes are retained.
-- V4's ON DELETE SET NULL detaches personal copies without deleting their content.
-- Deleting the template also removes its weekly_menu/original_weekly_menu recipe links.
DELETE FROM meal_plan_templates WHERE demo_code='mediterranean';
