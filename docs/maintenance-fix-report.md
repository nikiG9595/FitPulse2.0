# Maintenance corrections

All 14 supplied maintenance-fix JPG files are linked according to manifest.csv. The yogurt/fruit meal uses maintenance-fix-002.jpg on both day 1 and day 7. Existing correct photos are preserved.

Six separate catalog identities (452-457) isolate shared recipes and the renamed bread/egg/tomato meal from the other five menus. No nutritional fields were changed.

V24 was applied directly to fitpulse2 in a transaction, without changing Flyway history. It is idempotent and may run again through Flyway. Database assertions confirmed that menu content apart from name/recipeCode was unchanged and other menu records were unchanged. Saved templates and linked personal copies are included. The application needs a restart/resource reload to load the updated catalog.

## Mapping

| Day | Meal position | Recipe code | Name | Image |
|---|---|---|---|---|
| 1 | 3 | recipe-317 | Пилешко с ориз и зеленчуци | /images/meals/recipes/maintenance-fix-001.jpg |
| 1 | 4 | recipe-452 | Кисело мляко с плод | /images/meals/recipes/maintenance-fix-002.jpg |
| 7 | 4 | recipe-452 | Кисело мляко с плод | /images/meals/recipes/maintenance-fix-002.jpg |
| 3 | 4 | recipe-453 | Кисело мляко с овес | /images/meals/recipes/maintenance-fix-003.jpg |
| 4 | 2 | recipe-325 | Варено яйце и плод | /images/meals/recipes/maintenance-fix-004.jpg |
| 5 | 4 | recipe-454 | Скир с плод | /images/meals/recipes/maintenance-fix-005.jpg |
| 5 | 5 | recipe-333 | Хек с печени зеленчуци и картоф | /images/meals/recipes/maintenance-fix-006.jpg |
| 6 | 1 | recipe-334 | Овесени палачинки с ябълка | /images/meals/recipes/maintenance-fix-007.jpg |
| 6 | 3 | recipe-336 | Пуешко с кус-кус и салата | /images/meals/recipes/maintenance-fix-008.jpg |
| 6 | 4 | recipe-455 | Плод и ядки | /images/meals/recipes/maintenance-fix-009.jpg |
| 6 | 5 | recipe-338 | Омлет с гъби и сирене, доматена салата | /images/meals/recipes/maintenance-fix-010.jpg |
| 7 | 1 | recipe-339 | Тост с извара и краставица | /images/meals/recipes/maintenance-fix-011.jpg |
| 7 | 2 | recipe-456 | Ябълка и бадеми | /images/meals/recipes/maintenance-fix-012.jpg |
| 7 | 3 | recipe-340 | Пилешки шишчета с булгур | /images/meals/recipes/maintenance-fix-013.jpg |
| 7 | 5 | recipe-341 | Скумрия на фурна с картофи и зелена салата | /images/meals/recipes/maintenance-fix-014.jpg |
| 3 | 1 | recipe-457 | Пълнозърнест хляб с яйце и домат | /images/meals/recipes/recipe-278.jpg |
| 4 | 5 | recipe-328 | Салата с нахут, сирене и пълнозърнест хляб | /images/meals/recipes/library-recipe-263.jpg |

## Visual differences in supplied illustrations

The requested manifest mappings were used. These images are illustrations; the following visible differences were not copied into ingredients or nutrition:

- 001: broccoli/zucchini appear in addition to or instead of the listed carrot/pepper combination.
- 002: mixed fruit/berries are pictured; the stored fruit ingredient is apple.
- 005: kiwi/berries/seeds are pictured; the stored fruit ingredient is apple.
- 006: carrot is visible; the stored vegetables are zucchini and red pepper.
- 009: walnuts are visible as well as almonds; the stored nuts are almonds.
- 013: peppers are visible alongside skewers/bulgur; peppers are not listed ingredients.

No JPG files were modified or deleted. No build, tests, commit or push were run.
