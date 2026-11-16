# Muscle plan 5: days 4-7 corrections

10 new JPG paths were linked by exact manifest day, meal label and recipe name. Existing muscle recipe codes are already isolated from other menus and were retained. The two requested titles are updated in source and the existing database template. Saved original menus and linked personal copies are included by V25 where the expected title/code match.

V25 was applied directly to fitpulse2 in a transaction, without modifying Flyway history. It is idempotent for later application by Flyway. Transaction assertions and before/after comparison confirmed unchanged nutritional snapshots and no changes to other modes. An application restart/resource reload is needed for the static recipe catalog.

| Day | Meal position | Recipe code | Title | New file | Effective imagePath |
|---|---|---|---|---|---|
| 4 | 1 | recipe-441 | Пълнозърнест хляб с яйце и кашкавал | muscle-fix-001.jpg | /images/meals/recipes/muscle-fix-001.jpg |
| 4 | 5 | recipe-302 | Риба тон с паста и царевица | muscle-fix-002.jpg | /images/meals/recipes/muscle-fix-002.jpg |
| 5 | 2 | recipe-444 | Банан и фъстъчено масло | muscle-fix-003.jpg | /images/meals/recipes/muscle-recipe-304.jpg |
| 5 | 3 | recipe-305 | Телешко филе с картофи и салата | muscle-fix-004.jpg | /images/meals/recipes/muscle-fix-004.jpg |
| 6 | 2 | recipe-446 | Извара с круша | muscle-fix-005.jpg | /images/meals/recipes/muscle-fix-005.jpg |
| 6 | 3 | recipe-310 | Печена скумрия с картофи | muscle-fix-006.jpg | /images/meals/recipes/muscle-fix-006.jpg |
| 6 | 4 | recipe-311 | Скир с банан | muscle-fix-007.jpg | /images/meals/recipes/muscle-fix-007.jpg |
| 6 | 5 | recipe-312 | Тортила с пуешко, ориз и зеленчуци | muscle-fix-008.jpg | /images/meals/recipes/muscle-fix-008.jpg |
| 7 | 1 | recipe-447 | Палачинки с извара и ягоди | muscle-fix-009.jpg | /images/meals/recipes/muscle-fix-009.jpg |
| 7 | 2 | recipe-448 | Ябълка и орехи | muscle-fix-010.jpg | /images/meals/recipes/muscle-fix-010.jpg |
| 7 | 3 | recipe-313 | Пилешко с булгур и моркови | muscle-fix-011.jpg | /images/meals/recipes/muscle-fix-011.jpg |

## Supplied image requiring correction

muscle-fix-003.jpg is not linked: it depicts bread and a nut topping, while recipe-444 contains banana and peanut butter only. The previous muscle-recipe-304.jpg path is retained; no ingredients were added.

## Other visible illustration differences

muscle-fix-005.jpg includes a nut topping in addition to cottage cheese and pear. muscle-fix-007.jpg includes oats/seeds in addition to skyr and banana. These were mapped to the requested meals without copying depicted extras into ingredients, quantities or nutrition.

All other existing images, all original JPG files, recipe codes, ingredients, quantities, calories and macros are unchanged. No build, tests, commit or push were run.
