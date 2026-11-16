# Library photo mapping review

68 supplied JPG files were inspected visually against the recipe ingredients. Filename suffixes 100-167 were treated only as image identifiers. All manifest original_code/name pairs match the existing catalog.

8 confident mappings are linked. The other 60 images remain unlinked pending review, including duplicate alternatives and one protected muscle-menu recipe. No original codes, recipe content, menu links, or image files were changed.

## Existing database records

WeeklyMenu.Meal.getImage() calls RecipeAssets.image(meal), which finds the original recipeCode in recipes.json and uses its current imagePath after checking ingredient names. Recipe metadata is loaded at application startup. Existing database-backed templates and personal snapshots with those recipe codes therefore use these updated images after restart; no SQL migration, recipeCode replacement or database write is needed or performed. Personalized ingredient combinations that fail the existing guard continue to use the placeholder.

The three completed menus were checked: none of their catalog imagePath values are changed. In particular, recipe-008 keeps its muscle image; library-recipe-103.jpg remains unlinked.

## Per-image decisions

| File | Original recipe code | Decision | Reason |
|---|---|---|---|
| library-recipe-100.jpg | recipe-003 | Linked | Manifest code/name match; pictured main ingredients and dish agree with the recipe. |
| library-recipe-101.jpg | recipe-006 | Pending review | Tomato component not visible; review required. |
| library-recipe-102.jpg | recipe-007 | Linked | Manifest code/name match; pictured main ingredients and dish agree with the recipe. |
| library-recipe-103.jpg | recipe-008 | Pending review | Original recipe is used by the completed muscle menu; its existing muscle-specific photo is retained. |
| library-recipe-104.jpg | recipe-010 | Pending review | Bread missing; additional green vegetables visible. |
| library-recipe-105.jpg | recipe-014 | Linked | Manifest code/name match; pictured main ingredients and dish agree with the recipe. |
| library-recipe-106.jpg | recipe-015 | Pending review | Potato component unclear; additional peppers visible. |
| library-recipe-107.jpg | recipe-016 | Pending review | Peanut butter component unclear; additional topping visible. |
| library-recipe-108.jpg | recipe-018 | Linked | Manifest code/name match; pictured main ingredients and dish agree with the recipe. |
| library-recipe-109.jpg | recipe-020 | Pending review | Cucumber/tomato salad shown instead of the listed pepper/spinach combination. |
| library-recipe-110.jpg | recipe-021 | Pending review | White cheese component not visible. |
| library-recipe-111.jpg | recipe-024 | Linked | Manifest code/name match; pictured main ingredients and dish agree with the recipe. |
| library-recipe-112.jpg | recipe-025 | Pending review | Visible round legumes resemble chickpeas rather than the listed white beans. |
| library-recipe-113.jpg | recipe-026 | Pending review | Banana topping is not part of the recipe. |
| library-recipe-114.jpg | recipe-027 | Linked | Manifest code/name match; pictured main ingredients and dish agree with the recipe. |
| library-recipe-115.jpg | recipe-028 | Pending review | Zucchini component unclear. |
| library-recipe-116.jpg | recipe-029 | Pending review | Orange component missing; additional drizzle visible. |
| library-recipe-117.jpg | recipe-030 | Pending review | Legumes/vegetable mixture does not clearly match lentils, carrots, peppers and cheese. |
| library-recipe-118.jpg | recipe-031 | Pending review | Nut topping cannot confidently be identified as the listed almonds. |
| library-recipe-119.jpg | recipe-033 | Linked | Manifest code/name match; pictured main ingredients and dish agree with the recipe. |
| library-recipe-120.jpg | recipe-034 | Pending review | Duplicate candidate for recipe-034; mixed fruit differs from apple, walnuts and yogurt. |
| library-recipe-121.jpg | recipe-035 | Pending review | Duplicate candidate for recipe-035; broccoli missing and peppers visible. |
| library-recipe-122.jpg | recipe-036 | Pending review | Duplicate candidate for recipe-036; pictured cooked vegetables differ from tomato/cucumber ingredients. |
| library-recipe-123.jpg | recipe-037 | Pending review | Duplicate candidate for recipe-037; broccoli missing and carrots visible. |
| library-recipe-124.jpg | recipe-038 | Pending review | Duplicate candidate for recipe-038 (also image 132); no arbitrary selection between review-marked alternatives. |
| library-recipe-125.jpg | recipe-039 | Pending review | Duplicate candidate for recipe-039; spinach not evident. |
| library-recipe-126.jpg | recipe-040 | Pending review | Duplicate candidate for recipe-040; listed zucchini/carrot combination unclear. |
| library-recipe-127.jpg | recipe-041 | Pending review | Duplicate candidate for recipe-041; broccoli missing. |
| library-recipe-128.jpg | recipe-034 | Pending review | Duplicate candidate for recipe-034; mixed fruit differs from apple, walnuts and yogurt. |
| library-recipe-129.jpg | recipe-035 | Pending review | Duplicate candidate for recipe-035; broccoli missing. |
| library-recipe-130.jpg | recipe-036 | Pending review | Duplicate candidate for recipe-036; pictured cooked vegetables differ from tomato/cucumber ingredients. |
| library-recipe-131.jpg | recipe-037 | Pending review | Duplicate candidate for recipe-037; broccoli missing. |
| library-recipe-132.jpg | recipe-038 | Pending review | Duplicate candidate for recipe-038 (also image 124); no arbitrary selection between review-marked alternatives. |
| library-recipe-133.jpg | recipe-039 | Pending review | Duplicate candidate for recipe-039; spinach not evident. |
| library-recipe-134.jpg | recipe-040 | Pending review | Duplicate candidate for recipe-040; listed zucchini/carrot combination unclear. |
| library-recipe-135.jpg | recipe-041 | Pending review | Duplicate candidate for recipe-041; broccoli missing. |
| library-recipe-136.jpg | recipe-042 | Pending review | Chickpea and bread components unclear; additional vegetables visible. |
| library-recipe-137.jpg | recipe-043 | Pending review | Broccoli/zucchini combination missing. |
| library-recipe-138.jpg | recipe-044 | Linked | Manifest code/name match; pictured main ingredients and dish agree with the recipe. |
| library-recipe-139.jpg | recipe-046 | Pending review | Listed wholegrain bread component missing. |
| library-recipe-140.jpg | recipe-047 | Pending review | Chia pudding with blueberries instead of yogurt with raspberries, chia and oats. |
| library-recipe-141.jpg | recipe-048 | Pending review | Scrambled eggs instead of turkey/cucumber sandwich. |
| library-recipe-142.jpg | recipe-049 | Pending review | Fish and grains instead of buckwheat, cottage cheese and pear. |
| library-recipe-143.jpg | recipe-050 | Pending review | Chicken and rice instead of baked eggs with spinach and feta. |
| library-recipe-144.jpg | recipe-052 | Pending review | Grilled meat with potatoes instead of chicken with quinoa and zucchini. |
| library-recipe-145.jpg | recipe-053 | Pending review | Pancakes instead of beef with green beans and potatoes. |
| library-recipe-146.jpg | recipe-054 | Pending review | Yogurt/oat/fruit cup instead of cod with lentil salad. |
| library-recipe-147.jpg | recipe-055 | Pending review | Skewers and rice instead of turkey meatballs with bulgur and peppers. |
| library-recipe-148.jpg | recipe-056 | Pending review | Fish, broccoli and grains instead of pork tenderloin, cabbage and potatoes. |
| library-recipe-149.jpg | recipe-058 | Pending review | Egg/chickpea bowl instead of trout with buckwheat and broccoli. |
| library-recipe-150.jpg | recipe-059 | Pending review | Grain salad instead of peppers stuffed with turkey and lentils. |
| library-recipe-151.jpg | recipe-060 | Pending review | Vegetable patties instead of white bean soup with spinach and cottage cheese. |
| library-recipe-152.jpg | recipe-061 | Pending review | Stuffed mushrooms instead of shrimp with zucchini and bulgur. |
| library-recipe-153.jpg | recipe-062 | Pending review | White fish with vegetables instead of chicken with cabbage and carrots. |
| library-recipe-154.jpg | recipe-063 | Pending review | Egg/vegetable muffins instead of hake with cauliflower puree and peas. |
| library-recipe-155.jpg | recipe-064 | Pending review | Stuffed peppers instead of baked eggplant with tomato and mozzarella. |
| library-recipe-156.jpg | recipe-065 | Pending review | Pumpkin soup instead of lentil, pumpkin and cheese salad. |
| library-recipe-157.jpg | recipe-066 | Pending review | Greek-style vegetable/cheese salad instead of kefir with kiwi and pumpkin seeds. |
| library-recipe-158.jpg | recipe-067 | Pending review | Bean stew instead of cottage cheese with peach and almonds. |
| library-recipe-159.jpg | recipe-068 | Pending review | Mixed fruit bowl instead of carrots with lemon hummus. |
| library-recipe-160.jpg | recipe-069 | Pending review | Grilled meat and grains instead of yogurt with plum and hazelnuts. |
| library-recipe-161.jpg | recipe-070 | Pending review | Green smoothie instead of baked apple with ricotta. |
| library-recipe-162.jpg | recipe-071 | Pending review | Shrimp/avocado salad instead of skyr with orange and cocoa. |
| library-recipe-163.jpg | recipe-072 | Pending review | Baked eggplant instead of cucumber with cottage cheese dip and seeds. |
| library-recipe-164.jpg | recipe-073 | Pending review | Beans/rice/vegetable plate instead of strawberry yogurt cream with flax. |
| library-recipe-165.jpg | recipe-074 | Pending review | Yogurt/berries/seeds instead of roasted chickpeas with peppers and lemon. |
| library-recipe-166.jpg | recipe-075 | Pending review | Stuffed rolls instead of pear with kefir and oats. |
| library-recipe-167.jpg | recipe-076 | Pending review | Chicken wrap instead of oat porridge with cottage cheese and dates. |

Review was visual and conservative; uncertainty is recorded rather than treated as a confirmed ingredient identification. No build, tests, commit or push were run.
