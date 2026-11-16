# Active plan 2: days 1-7 corrections

21 new images are linked using exact manifest day, meal label and recipe name. Five manifest/image mismatches are not linked; their previous image paths are retained. All other correct existing images are retained.

Day 4 breakfast has the requested new bread/egg/tomato title (recipe-464), reusing the existing recipe-278.jpg photo. Other menus keep recipe-278 and their existing names and images.

Seven isolated catalog copies (458-464) preserve shared recipes and allow separate manifest images for repeated skyr/fruit and egg-sandwich meals. Recipe codes for other modes and all nutritional data are unchanged.

V26 was applied directly to fitpulse2 in a transaction without modifying Flyway history. The migration is idempotent for subsequent Flyway application. Assertions checked unchanged menu content except name/recipeCode and unchanged records for other modes. A before/after comparison also confirmed that only active records changed. Plan 2 matches all 35 source names/codes. The catalog needs application restart/resource reload.

## Manifest/image mismatches requiring correction

| File | Manifest target | Actual visible dish | Decision |
|---|---|---|---|
| active-fix-017.jpg | Day 5 dinner: turkey meatballs with pasta and tomato sauce | Yogurt, berries and nuts | Not linked |
| active-fix-018.jpg | Day 6 morning snack: cottage cheese with pear | Meatballs with pasta and tomato sauce | Not linked |
| active-fix-019.jpg | Day 6 lunch: chicken with bulgur and peas | Cottage cheese with pear and walnuts | Not linked |
| active-fix-020.jpg | Day 6 afternoon snack: yogurt with nuts | Chicken with grains and peas | Existing suitable yogurt photo retained |
| active-fix-021.jpg | Day 6 dinner: pork with rice and mushrooms | Yogurt with mixed nuts | Not linked |

Files 018-021 appear shifted relative to the manifest: 018 depicts the 017 target, 019 the 018 target, 020 the 019 target, and 021 the 020 target. No remapping was applied without a corrected manifest. A matching pork/rice/mushroom photo is still needed.

## Full mapping

| Day | Meal position | Name | Original code | Effective code | New file | Resulting imagePath |
|---|---|---|---|---|---|---|
| 1 | 2 | Скир с плод | recipe-332 | recipe-458 | active-fix-001.jpg | /images/meals/recipes/active-fix-001.jpg |
| 1 | 3 | Пилешко с ориз и моркови | recipe-388 | recipe-388 | active-fix-002.jpg | /images/meals/recipes/active-fix-002.jpg |
| 1 | 5 | Свинско бонфиле с печени картофи | recipe-389 | recipe-389 | active-fix-003.jpg | /images/meals/recipes/active-fix-003.jpg |
| 2 | 1 | Омлет с яйца, сирене и хляб | recipe-293 | recipe-293 | active-fix-004.jpg | /images/meals/recipes/active-fix-004.jpg |
| 2 | 3 | Паста с пуешко и доматен сос | recipe-390 | recipe-390 | active-fix-005.jpg | /images/meals/recipes/active-fix-005.jpg |
| 2 | 4 | Кисело мляко с овес | recipe-314 | recipe-459 | active-fix-006.jpg | /images/meals/recipes/active-fix-006.jpg |
| 2 | 5 | Пъстърва с булгур и салата | recipe-391 | recipe-391 | active-fix-007.jpg | /images/meals/recipes/active-fix-007.jpg |
| 3 | 1 | Овесени палачинки с мед и плод | recipe-392 | recipe-392 | active-fix-008.jpg | /images/meals/recipes/active-fix-008.jpg |
| 3 | 3 | Пилешки шишчета с картофи и зеленчуци | recipe-393 | recipe-393 | active-fix-009.jpg | /images/meals/recipes/active-fix-009.jpg |
| 3 | 4 | Пълнозърнест сандвич с яйце | recipe-306 | recipe-460 | active-fix-010.jpg | /images/meals/recipes/active-fix-010.jpg |
| 3 | 5 | Риба тон с ориз и царевица | recipe-394 | recipe-394 | active-fix-011.jpg | /images/meals/recipes/active-fix-011.jpg |
| 4 | 3 | Свинско филе с кус-кус и печени чушки | recipe-395 | recipe-395 | active-fix-012.jpg | /images/meals/recipes/active-fix-012.jpg |
| 4 | 5 | Пилешка тортила с ориз и зеленчуци | recipe-396 | recipe-396 | active-fix-013.jpg | /images/meals/recipes/active-fix-013.jpg |
| 5 | 2 | Банан с фъстъчено масло | recipe-304 | recipe-304 | active-fix-014.jpg | /images/meals/recipes/active-fix-014.jpg |
| 5 | 3 | Скумрия с картофи и салата | recipe-397 | recipe-397 | active-fix-015.jpg | /images/meals/recipes/active-fix-015.jpg |
| 5 | 4 | Скир с плод | recipe-332 | recipe-461 | active-fix-016.jpg | /images/meals/recipes/active-fix-016.jpg |
| 5 | 5 | Пуешки кюфтета с паста и доматен сос | recipe-398 | recipe-398 | active-fix-017.jpg | /images/meals/meal-placeholder.svg |
| 6 | 2 | Извара с круша | recipe-309 | recipe-309 | active-fix-018.jpg | /images/meals/meal-placeholder.svg |
| 6 | 3 | Пилешко с булгур и грах | recipe-400 | recipe-400 | active-fix-019.jpg | /images/meals/meal-placeholder.svg |
| 6 | 4 | Кисело мляко с ядки | recipe-019 | recipe-019 | active-fix-020.jpg | /images/meals/recipes/recipe-019.jpg |
| 6 | 5 | Свинско с ориз и гъби на тиган | recipe-401 | recipe-401 | active-fix-021.jpg | /images/meals/meal-placeholder.svg |
| 7 | 1 | Палачинки с извара и банан | recipe-402 | recipe-402 | active-fix-022.jpg | /images/meals/recipes/active-fix-022.jpg |
| 7 | 2 | Ябълка и бадеми | recipe-337 | recipe-462 | active-fix-023.jpg | /images/meals/recipes/active-fix-023.jpg |
| 7 | 3 | Телешко с ориз и зеленчуци на тиган | recipe-403 | recipe-403 | active-fix-024.jpg | /images/meals/recipes/active-fix-024.jpg |
| 7 | 4 | Сандвич с яйце | recipe-306 | recipe-463 | active-fix-025.jpg | /images/meals/recipes/active-fix-025.jpg |
| 7 | 5 | Хек с картофи и салата | recipe-404 | recipe-404 | active-fix-026.jpg | /images/meals/recipes/active-fix-026.jpg |

## Illustration limitations

The supplied images are illustrations; depicted extras were not added to ingredients or nutritional calculations. Examples include mixed fruits/toppings in 001 and 016 (the recipe specifies skyr/apple), berry toppings in 008 (the recipe specifies apple/honey), extra vegetables in 010/025 egg sandwiches, and mixed nuts in the unlinked 021 (the intended yogurt recipe contains walnuts). File 026 contains visible collage/cropping seams and parts of adjacent images, although its central fish/potato/salad dish matches the manifest target; the crop should be corrected.

No original JPG files were changed, overwritten or deleted. No build, tests, commit or push were run.
