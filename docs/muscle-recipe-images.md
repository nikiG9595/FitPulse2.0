# Muscle recipe photos

All 35 manifest rows match the exact menu names and positions. Rows 2, 12, 13, 14 and 32 omit recipe_code; their identities are resolved from muscle.json, without fuzzy name matching.

15 shared entries now have separate muscle recipe codes (435-449). Original shared recipes and their photos are retained for other menus. No JPG files were changed.

| Position | Manifest/source code | Muscle code | JPG |
|---|---|---|---|
| 1 | recipe-289 | recipe-289 | muscle-recipe-289.jpg |
| 2 |  | recipe-435 | muscle-meal-02.jpg |
| 3 | recipe-290 | recipe-290 | muscle-recipe-290.jpg |
| 4 | recipe-291 | recipe-291 | muscle-recipe-291.jpg |
| 5 | recipe-292 | recipe-292 | muscle-recipe-292.jpg |
| 6 | recipe-293 | recipe-436 | muscle-recipe-293.jpg |
| 7 | recipe-012 | recipe-437 | muscle-recipe-012.jpg |
| 8 | recipe-294 | recipe-294 | muscle-recipe-294.jpg |
| 9 | recipe-295 | recipe-295 | muscle-recipe-295.jpg |
| 10 | recipe-296 | recipe-296 | muscle-recipe-296.jpg |
| 11 | recipe-282 | recipe-438 | muscle-recipe-282.jpg |
| 12 |  | recipe-439 | muscle-meal-12.jpg |
| 13 |  | recipe-008 | muscle-meal-13.jpg |
| 14 |  | recipe-440 | muscle-meal-14.jpg |
| 15 | recipe-297 | recipe-297 | muscle-recipe-297.jpg |
| 16 | recipe-298 | recipe-441 | muscle-recipe-298.jpg |
| 17 | recipe-299 | recipe-442 | muscle-recipe-299.jpg |
| 18 | recipe-300 | recipe-300 | muscle-recipe-300.jpg |
| 19 | recipe-301 | recipe-443 | muscle-recipe-301.jpg |
| 20 | recipe-302 | recipe-302 | muscle-recipe-302.jpg |
| 21 | recipe-303 | recipe-303 | muscle-recipe-303.jpg |
| 22 | recipe-304 | recipe-444 | muscle-recipe-304.jpg |
| 23 | recipe-305 | recipe-305 | muscle-recipe-305.jpg |
| 24 | recipe-306 | recipe-445 | muscle-recipe-306.jpg |
| 25 | recipe-307 | recipe-307 | muscle-recipe-307.jpg |
| 26 | recipe-308 | recipe-308 | muscle-recipe-308.jpg |
| 27 | recipe-309 | recipe-446 | muscle-recipe-309.jpg |
| 28 | recipe-310 | recipe-310 | muscle-recipe-310.jpg |
| 29 | recipe-311 | recipe-311 | muscle-recipe-311.jpg |
| 30 | recipe-312 | recipe-312 | muscle-recipe-312.jpg |
| 31 | recipe-051 | recipe-447 | muscle-recipe-051.jpg |
| 32 |  | recipe-448 | muscle-meal-32.jpg |
| 33 | recipe-313 | recipe-313 | muscle-recipe-313.jpg |
| 34 | recipe-314 | recipe-449 | muscle-recipe-314.jpg |
| 35 | recipe-315 | recipe-315 | muscle-recipe-315.jpg |

V21 updates only recipeCode in existing muscle templates (weekly_menu and original_weekly_menu) and linked personal copies. Position, old code and meal name must match; the standard personal-name prefix is supported. Ingredients, quantities and nutritional values are retained. Other menus are excluded.

Detached personal copies and meals whose position/name/recipe was changed are deliberately left unchanged: their muscle origin or mapping cannot be established reliably.

The migration has not been executed in this task. Flyway will apply it on the next application startup. No build, tests, commit or push were run.
