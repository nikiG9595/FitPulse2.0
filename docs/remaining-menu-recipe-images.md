# Maintenance, vegetarian and active recipe photos

82 manifest rows were resolved using exact meal names within the listed menu JSON files, then the original recipeCode was used to locate recipes.json. Image numbers 248-329 are never treated as recipe codes.

24 imagePath values updated after visual review. 7 manifest rows retain suitable existing images (6 distinct recipe codes). All repeated occurrences of a recipe continue to resolve through the same original code. No recipe codes, ingredients, quantities, nutrition, menu files or JPG files were changed.

## Existing database records

WeeklyMenu.Meal.getImage() calls RecipeAssets.image(meal), resolving its recipeCode against recipes.json and checking ingredient names. Thus existing database templates and personal snapshots with these original codes use the new paths after application restart. No database migration or database write is needed or performed. The existing placeholder remains for personalized ingredient sets that fail the guard.

No image paths used by fat-loss, muscle or low-carb were changed. Existing images for shared recipes 022, 032 and 278 are retained.

## New images requiring correction or review

All 46 images 284-329 have quality_status=approximate_not_verified_do_not_auto_link and were not linked. Their existing recipe photos, where available, are retained. Eight additional candidates were deferred after visual ingredient review.

| File | Original code | Meal | Menus | Reason |
|---|---|---|---|---|
| library-recipe-249.jpg | recipe-317 | Пилешко с ориз и зеленчуци | maintenance | Additional broccoli not present in the recipe. |
| library-recipe-250.jpg | recipe-299 | Кисело мляко с плод | maintenance | Mixed berries, kiwi and banana shown; the recipe specifies apple. |
| library-recipe-260.jpg | recipe-325 | Варено яйце и плод | maintenance | Additional grapes and blueberries; the recipe specifies egg and apple. |
| library-recipe-267.jpg | recipe-332 | Скир с плод | maintenance, active | Berries shown; the recipe specifies skyr and apple. |
| library-recipe-268.jpg | recipe-304 | Банан с фъстъчено масло | active | Whole/chopped nut topping beyond the listed banana and peanut butter; ingredient review needed. |
| library-recipe-275.jpg | recipe-299 | Кисело мляко и плод | vegetarian | Mixed fruit shown; the shared original recipe specifies yogurt and apple. No alternate photo selected for recipe-299. |
| library-recipe-279.jpg | recipe-367 | Нахут с ориз и зеленчуци на тиган | vegetarian | Zucchini/tomato mixture shown; the listed carrot/pepper combination is not a confident match. |
| library-recipe-283.jpg | recipe-392 | Овесени палачинки с мед и плод | active | Berry toppings shown; the recipe specifies apple and honey. |
| library-recipe-284.jpg | recipe-334 | Овесени палачинки с ябълка | maintenance | Manifest explicitly prohibits automatic linking. |
| library-recipe-285.jpg | recipe-374 | Овесени палачинки с ягоди | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-286.jpg | recipe-371 | Омлет с гъби и картофи | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-287.jpg | recipe-338 | Омлет с гъби и сирене, доматена салата | maintenance | Manifest explicitly prohibits automatic linking. |
| library-recipe-288.jpg | recipe-293 | Омлет с яйца, сирене и хляб | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-289.jpg | recipe-386 | Омлет със спанак и салата с картофи | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-290.jpg | recipe-366 | Омлет със спанак и сирене | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-291.jpg | recipe-383 | Ориз с гъби, грах и настърган кашкавал | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-292.jpg | recipe-402 | Палачинки с извара и банан | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-293.jpg | recipe-390 | Паста с пуешко и доматен сос | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-294.jpg | recipe-396 | Пилешка тортила с ориз и зеленчуци | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-295.jpg | recipe-340 | Пилешки шишчета с булгур | maintenance | Manifest explicitly prohibits automatic linking. |
| library-recipe-296.jpg | recipe-393 | Пилешки шишчета с картофи и зеленчуци | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-297.jpg | recipe-400 | Пилешко с булгур и грах | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-298.jpg | recipe-388 | Пилешко с ориз и моркови | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-299.jpg | recipe-337 | Плод и бадеми | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-300.jpg | recipe-337 | Плод и ядки | maintenance | Manifest explicitly prohibits automatic linking. |
| library-recipe-301.jpg | recipe-398 | Пуешки кюфтета с паста и доматен сос | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-302.jpg | recipe-336 | Пуешко с кус-кус и салата | maintenance | Manifest explicitly prohibits automatic linking. |
| library-recipe-303.jpg | recipe-381 | Пълнена тортила с яйца, сирене и зеленчуци | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-304.jpg | recipe-306 | Пълнозърнест сандвич с яйце | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-305.jpg | recipe-372 | Пълнозърнеста паста с домати, моцарела и спанак | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-306.jpg | recipe-391 | Пъстърва с булгур и салата | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-307.jpg | recipe-394 | Риба тон с ориз и царевица | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-308.jpg | recipe-373 | Салата с леща, яйце и зеленчуци | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-309.jpg | recipe-384 | Салата с нахут, фета, домати и маслини | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-310.jpg | recipe-306 | Сандвич с яйце | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-311.jpg | recipe-389 | Свинско бонфиле с печени картофи | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-312.jpg | recipe-401 | Свинско с ориз и гъби на тиган | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-313.jpg | recipe-395 | Свинско филе с кус-кус и печени чушки | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-314.jpg | recipe-376 | Скир с канела | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-315.jpg | recipe-341 | Скумрия на фурна с картофи и зелена салата | maintenance | Manifest explicitly prohibits automatic linking. |
| library-recipe-316.jpg | recipe-397 | Скумрия с картофи и салата | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-317.jpg | recipe-403 | Телешко с ориз и зеленчуци на тиган | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-318.jpg | recipe-368 | Тортила с боб, царевица и зеленчуци | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-319.jpg | recipe-301 | Тост с извара | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-320.jpg | recipe-032 | Тост с извара и домат | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-321.jpg | recipe-339 | Тост с извара и краставица | maintenance | Manifest explicitly prohibits automatic linking. |
| library-recipe-322.jpg | recipe-298 | Тост с яйце и кашкавал | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-323.jpg | recipe-278 | Тостове с яйца и домат | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-324.jpg | recipe-370 | Тофу с ориз и броколи | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-325.jpg | recipe-377 | Тофу с тиквички и булгур | vegetarian | Manifest explicitly prohibits automatic linking. |
| library-recipe-326.jpg | recipe-404 | Хек с картофи и салата | active | Manifest explicitly prohibits automatic linking. |
| library-recipe-327.jpg | recipe-333 | Хек с печени зеленчуци и картоф | maintenance | Manifest explicitly prohibits automatic linking. |
| library-recipe-328.jpg | recipe-337 | Ябълка и бадеми | maintenance, active | Manifest explicitly prohibits automatic linking. |
| library-recipe-329.jpg | recipe-379 | Ябълка и фъстъчено масло | vegetarian | Manifest explicitly prohibits automatic linking. |

## All mapping decisions

| New file | Original code | Meal | Menus | Manifest status | Decision | Resulting existing/new imagePath |
|---|---|---|---|---|---|---|
| library-recipe-248.jpg | recipe-316 | Омлет със сирене и домат | maintenance | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-248.jpg |
| library-recipe-249.jpg | recipe-317 | Пилешко с ориз и зеленчуци | maintenance | generated_visual_review_needed | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-250.jpg | recipe-299 | Кисело мляко с плод | maintenance | generated_visual_review_needed | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-251.jpg | recipe-318 | Пъстърва с картофи и салата | maintenance | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-251.jpg |
| library-recipe-252.jpg | recipe-319 | Овес с мляко и банан | maintenance | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-252.jpg |
| library-recipe-253.jpg | recipe-320 | Свинско филе с булгур | maintenance | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-253.jpg |
| library-recipe-254.jpg | recipe-068 | Моркови с хумус | maintenance, vegetarian | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-254.jpg |
| library-recipe-255.jpg | recipe-278 | Тост с яйце и домат | maintenance | generated_visual_review_needed | Reuse existing | /images/meals/recipes/recipe-278.jpg |
| library-recipe-256.jpg | recipe-321 | Круша и бадеми | maintenance, vegetarian | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-256.jpg |
| library-recipe-257.jpg | recipe-322 | Паста с риба тон и домати | maintenance | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-257.jpg |
| library-recipe-258.jpg | recipe-323 | Пуешки кюфтета с печени тиквички и картофи | maintenance | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-258.jpg |
| library-recipe-259.jpg | recipe-324 | Кисело мляко с овес и ягоди | maintenance, active | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-259.jpg |
| library-recipe-260.jpg | recipe-325 | Варено яйце и плод | maintenance | generated_visual_review_needed | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-261.jpg | recipe-326 | Пилешко с картофи и броколи | maintenance | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-261.jpg |
| library-recipe-262.jpg | recipe-327 | Извара с канела | maintenance | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-262.jpg |
| library-recipe-263.jpg | recipe-328 | Салата с нахут, сирене и пълнозърнест тост | maintenance | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-263.jpg |
| library-recipe-264.jpg | recipe-329 | Бъркани яйца и пълнозърнест хляб | maintenance | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-264.jpg |
| library-recipe-265.jpg | recipe-330 | Банан и орехи | maintenance, vegetarian | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-265.jpg |
| library-recipe-266.jpg | recipe-331 | Свинско бонфиле с ориз и моркови | maintenance | generated_visual_review_needed | Linked | /images/meals/recipes/library-recipe-266.jpg |
| library-recipe-267.jpg | recipe-332 | Скир с плод | maintenance, active | generated_visual_review_needed | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-268.jpg | recipe-304 | Банан с фъстъчено масло | active | visual_match_plausible_check_ingredients | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-269.jpg | recipe-375 | Боб с печени чушки и пълнозърнест хляб | vegetarian | visual_match_plausible_check_ingredients | Linked | /images/meals/recipes/library-recipe-269.jpg |
| library-recipe-270.jpg | recipe-399 | Бъркани яйца с кашкавал и хляб | active | visual_match_plausible_check_ingredients | Linked | /images/meals/recipes/library-recipe-270.jpg |
| library-recipe-271.jpg | recipe-378 | Бъркани яйца със сирене и домат | vegetarian | visual_match_plausible_check_ingredients | Linked | /images/meals/recipes/library-recipe-271.jpg |
| library-recipe-272.jpg | recipe-002 | Извара с плод | vegetarian | visual_match_plausible_check_ingredients | Reuse existing | /images/meals/recipes/recipe-002.jpg |
| library-recipe-273.jpg | recipe-335 | Кисело мляко | maintenance | visual_match_plausible_check_ingredients | Linked | /images/meals/recipes/library-recipe-273.jpg |
| library-recipe-274.jpg | recipe-022 | Кисело мляко и банан | active | visual_match_plausible_check_ingredients | Reuse existing | /images/meals/recipes/recipe-022.jpg |
| library-recipe-275.jpg | recipe-299 | Кисело мляко и плод | vegetarian | visual_match_plausible_check_ingredients | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-276.jpg | recipe-382 | Кисело мляко с овес и ябълка | vegetarian | visual_match_plausible_check_ingredients | Linked | /images/meals/recipes/library-recipe-276.jpg |
| library-recipe-277.jpg | recipe-019 | Кисело мляко с ядки | active | visual_match_plausible_check_ingredients | Reuse existing | /images/meals/recipes/recipe-019.jpg |
| library-recipe-278.jpg | recipe-385 | Леща с ориз и печени моркови | vegetarian | visual_match_plausible_check_ingredients | Linked | /images/meals/recipes/library-recipe-278.jpg |
| library-recipe-279.jpg | recipe-367 | Нахут с ориз и зеленчуци на тиган | vegetarian | visual_match_plausible_check_ingredients | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-280.jpg | recipe-380 | Нахутени кюфтенца на тиган с млечен сос и салата | vegetarian | visual_match_plausible_check_ingredients | Linked | /images/meals/recipes/library-recipe-280.jpg |
| library-recipe-281.jpg | recipe-387 | Овес с банан, мляко и ядки | active | visual_match_plausible_check_ingredients | Linked | /images/meals/recipes/library-recipe-281.jpg |
| library-recipe-282.jpg | recipe-369 | Овес с мляко, банан и канела | vegetarian | visual_match_plausible_check_ingredients | Linked | /images/meals/recipes/library-recipe-282.jpg |
| library-recipe-283.jpg | recipe-392 | Овесени палачинки с мед и плод | active | visual_match_plausible_check_ingredients | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-284.jpg | recipe-334 | Овесени палачинки с ябълка | maintenance | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-285.jpg | recipe-374 | Овесени палачинки с ягоди | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-286.jpg | recipe-371 | Омлет с гъби и картофи | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-287.jpg | recipe-338 | Омлет с гъби и сирене, доматена салата | maintenance | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-288.jpg | recipe-293 | Омлет с яйца, сирене и хляб | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-289.jpg | recipe-386 | Омлет със спанак и салата с картофи | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-290.jpg | recipe-366 | Омлет със спанак и сирене | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-291.jpg | recipe-383 | Ориз с гъби, грах и настърган кашкавал | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-292.jpg | recipe-402 | Палачинки с извара и банан | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-293.jpg | recipe-390 | Паста с пуешко и доматен сос | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-294.jpg | recipe-396 | Пилешка тортила с ориз и зеленчуци | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-295.jpg | recipe-340 | Пилешки шишчета с булгур | maintenance | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-296.jpg | recipe-393 | Пилешки шишчета с картофи и зеленчуци | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-297.jpg | recipe-400 | Пилешко с булгур и грах | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-298.jpg | recipe-388 | Пилешко с ориз и моркови | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-299.jpg | recipe-337 | Плод и бадеми | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-300.jpg | recipe-337 | Плод и ядки | maintenance | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-301.jpg | recipe-398 | Пуешки кюфтета с паста и доматен сос | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-302.jpg | recipe-336 | Пуешко с кус-кус и салата | maintenance | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-303.jpg | recipe-381 | Пълнена тортила с яйца, сирене и зеленчуци | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-304.jpg | recipe-306 | Пълнозърнест сандвич с яйце | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-305.jpg | recipe-372 | Пълнозърнеста паста с домати, моцарела и спанак | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-306.jpg | recipe-391 | Пъстърва с булгур и салата | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-307.jpg | recipe-394 | Риба тон с ориз и царевица | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-308.jpg | recipe-373 | Салата с леща, яйце и зеленчуци | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-309.jpg | recipe-384 | Салата с нахут, фета, домати и маслини | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-310.jpg | recipe-306 | Сандвич с яйце | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-311.jpg | recipe-389 | Свинско бонфиле с печени картофи | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-312.jpg | recipe-401 | Свинско с ориз и гъби на тиган | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-313.jpg | recipe-395 | Свинско филе с кус-кус и печени чушки | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-314.jpg | recipe-376 | Скир с канела | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-315.jpg | recipe-341 | Скумрия на фурна с картофи и зелена салата | maintenance | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-316.jpg | recipe-397 | Скумрия с картофи и салата | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-317.jpg | recipe-403 | Телешко с ориз и зеленчуци на тиган | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-318.jpg | recipe-368 | Тортила с боб, царевица и зеленчуци | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-319.jpg | recipe-301 | Тост с извара | active | approximate_not_verified_do_not_auto_link | Reuse existing | /images/meals/recipes/toast-cottage-cheese.jpg |
| library-recipe-320.jpg | recipe-032 | Тост с извара и домат | vegetarian | approximate_not_verified_do_not_auto_link | Reuse existing | /images/meals/recipes/recipe-032.jpg |
| library-recipe-321.jpg | recipe-339 | Тост с извара и краставица | maintenance | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-322.jpg | recipe-298 | Тост с яйце и кашкавал | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-323.jpg | recipe-278 | Тостове с яйца и домат | active | approximate_not_verified_do_not_auto_link | Reuse existing | /images/meals/recipes/recipe-278.jpg |
| library-recipe-324.jpg | recipe-370 | Тофу с ориз и броколи | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-325.jpg | recipe-377 | Тофу с тиквички и булгур | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-326.jpg | recipe-404 | Хек с картофи и салата | active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-327.jpg | recipe-333 | Хек с печени зеленчуци и картоф | maintenance | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-328.jpg | recipe-337 | Ябълка и бадеми | maintenance, active | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |
| library-recipe-329.jpg | recipe-379 | Ябълка и фъстъчено масло | vegetarian | approximate_not_verified_do_not_auto_link | Not linked | /images/meals/meal-placeholder.svg |

Visual review identifies the depicted main dish and ingredients; it does not certify hidden ingredients or nutritional amounts. Blocked manifest statuses were honored regardless of apparent visual similarity.

No build, tests, commit or push were run.
