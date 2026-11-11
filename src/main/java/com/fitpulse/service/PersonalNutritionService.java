package com.fitpulse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.WeeklyMenu;
import com.fitpulse.model.entity.*;
import com.fitpulse.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service @Transactional(readOnly=true)
public class PersonalNutritionService {
    private final UserMealPlanRepository plans;private final FoodRepository foods;private final ShoppingCheckRepository checks;private final UserService users;private final ObjectMapper mapper;
    public PersonalNutritionService(UserMealPlanRepository plans,FoodRepository foods,ShoppingCheckRepository checks,UserService users,ObjectMapper mapper){this.plans=plans;this.foods=foods;this.checks=checks;this.users=users;this.mapper=mapper;}
    public UserMealPlan owned(Long id){return plans.findByIdAndUser(id,users.getCurrentUser()).filter(UserMealPlan::isListed).orElseThrow(()->new FitPulseException("Личният режим не е намерен."));}
    private UserMealPlan locked(Long id){return plans.findOwnedForUpdate(id,users.getCurrentUser()).filter(UserMealPlan::isListed).orElseThrow(()->new FitPulseException("Личният режим не е намерен."));}
    @Transactional public void rename(Long id,String name){if(name==null||name.isBlank()||name.strip().length()>150)throw new FitPulseException("Въведи име до 150 знака.");locked(id).setName(name.strip());}
    public WeeklyMenu menu(UserMealPlan plan){if(plan.getWeeklyMenu()==null||plan.getWeeklyMenu().isBlank())throw new FitPulseException("Това е запазено текстово копие. За интерактивни грамажи добави ново копие от библиотеката.");return parse(plan.getWeeklyMenu());}
    public WeeklyMenu parse(String json){try{return mapper.readValue(json,WeeklyMenu.class);}catch(Exception e){throw new FitPulseException("Съдържанието на режима не може да се прочете.");}}
    public String canonicalJson(String json){
        WeeklyMenu menu=parse(RecipeInstructions.repair(json,mapper));List<Food> catalog=foods.findAll();List<WeeklyMenu.Day> days=new ArrayList<>();
        for(var day:menu.days()){
            List<WeeklyMenu.Meal> meals=new ArrayList<>();
            for(var meal:day.meals()){
                List<WeeklyMenu.Ingredient> ingredients=new ArrayList<>();
                for(var ingredient:meal.ingredients()){
                    Food food=catalog.stream().filter(f->f.getCode().equals(ingredient.foodCode())||(f.getName().equals(ingredient.name())&&f.getState().equals(ingredient.state()))).findFirst().orElseThrow(()->new FitPulseException("Продуктът липсва в хранителната база: "+ingredient.name()));
                    ingredients.add(ingredient(food,BigDecimal.valueOf(ingredient.grams())));
                }
                meals.add(new WeeklyMenu.Meal(meal.label(),meal.name(),ingredients,meal.preparation(),meal.substitution(),meal.recipeCode()));
            }
            days.add(new WeeklyMenu.Day(day.title(),meals));
        }
        return json(new WeeklyMenu(menu.author(),menu.methodology(),days));
    }
    private String json(WeeklyMenu menu){try{return mapper.writeValueAsString(menu);}catch(Exception e){throw new IllegalStateException(e);}}
    public record CalorieEstimate(long resting,long daily,long original){}
    public CalorieEstimate estimate(com.fitpulse.model.dto.MealPersonalizationForm form,String source){
        double resting=10*form.getWeight().doubleValue()+6.25*form.getHeight().doubleValue()-5*form.getAge()+(form.getSex().equals("MALE")?5:-161);
        double activity=switch(form.getActivity()){case "LOW"->1.2;case "MODERATE"->1.55;case "HIGH"->1.725;default->throw new FitPulseException("Изберете валидна активност.");};
        return new CalorieEstimate(Math.round(resting),Math.round(resting*activity),originalCalories(source));
    }
    public long originalCalories(String source){var menu=parse(canonicalJson(source));return Math.round(menu.days().stream().mapToDouble(d->4*d.protein()+9*d.fat()+4*d.carbs()).average().orElseThrow(()->new FitPulseException("Липсват дни в режима.")));}
    public String adaptedJson(String source,BigDecimal target){
        if(target==null||target.compareTo(new BigDecimal("1200"))<0||target.compareTo(new BigDecimal("6000"))>0||target.stripTrailingZeros().scale()>0)throw new FitPulseException("Потвърдете дневна цел между 1200 и 6000 цели ккал.");
        var menu=parse(canonicalJson(source));List<WeeklyMenu.Day> days=new ArrayList<>();
        for(var day:menu.days()){
            double calories=4*day.protein()+9*day.fat()+4*day.carbs();
            if(!Double.isFinite(calories)||calories<=0)throw new FitPulseException("Денят няма валидни хранителни стойности.");
            BigDecimal factor=target.divide(BigDecimal.valueOf(calories),12,java.math.RoundingMode.HALF_UP);
            List<WeeklyMenu.Meal> meals=new ArrayList<>();
            for(var meal:day.meals()){
                List<WeeklyMenu.Ingredient> ingredients=new ArrayList<>();
                for(var item:meal.ingredients()){
                    BigDecimal grams=BigDecimal.valueOf(item.grams()).multiply(factor).setScale(2,java.math.RoundingMode.HALF_UP);
                    if(grams.compareTo(BigDecimal.ONE)<0||grams.compareTo(new BigDecimal("2000"))>0)throw new FitPulseException("Тази цел води до порция извън допустимите 1–2000 г за продукт. Изберете друга цел; копие не е създадено.");
                    quantity(grams); // Reject impossible portions rather than silently breaking food ratios.
                    ingredients.add(new WeeklyMenu.Ingredient(item.name(),grams.doubleValue(),item.state(),item.proteinPer100(),item.fatPer100(),item.carbsPer100(),item.foodCode()));
                }
                meals.add(new WeeklyMenu.Meal(meal.label(),meal.name(),ingredients,meal.preparation(),meal.substitution(),meal.recipeCode()));
            }
            days.add(new WeeklyMenu.Day(day.title(),meals));
        }
        return json(new WeeklyMenu(menu.author(),menu.methodology()+" Лично копие с потвърдена дневна цел "+target.toPlainString()+" ккал; възможни са малки отклонения от закръгляването на грамажите.",days));
    }
    public String menuText(String source){return plainText(parse(source));}
    private WeeklyMenu.Ingredient ingredient(Food food,BigDecimal grams){return new WeeklyMenu.Ingredient(food.getName(),grams.doubleValue(),food.getState(),food.getProtein().doubleValue(),food.getFat().doubleValue(),food.getCarbs().doubleValue(),food.getCode());}
    public WeeklyMenu.Ingredient selected(WeeklyMenu menu,int day,int meal,int item){
        if(day<0||day>=menu.days().size()||meal<0||meal>=menu.days().get(day).meals().size()||item<0||item>=menu.days().get(day).meals().get(meal).ingredients().size())throw new FitPulseException("Съставката не е намерена.");return menu.days().get(day).meals().get(meal).ingredients().get(item);
    }
    public WeeklyMenu.Meal mealAt(Long id,int day,int meal){var menu=menu(owned(id));selected(menu,day,meal,0);return menu.days().get(day).meals().get(meal);}
    public List<RecipeAssets.Recipe> mealAlternatives(Long id,int day,int meal,String q){var current=mealAt(id,day,meal);String bucket=RecipeAssets.bucket(current.label());String needle=q==null?"":q.strip().toLowerCase(Locale.ROOT);return RecipeAssets.all().stream().filter(r->RecipeAssets.bucket(r.label()).equals(bucket)).filter(r->!r.name().equals(current.name())).filter(r->r.name().toLowerCase(Locale.ROOT).contains(needle)).toList();}
    @Transactional public void replaceMeal(Long id,int day,int meal,String code){
        var plan=locked(id);var menu=menu(plan);selected(menu,day,meal,0);var current=menu.days().get(day).meals().get(meal);
        var recipe=RecipeAssets.all().stream().filter(r->r.code().equals(code)&&RecipeAssets.bucket(r.label()).equals(RecipeAssets.bucket(current.label()))).findFirst().orElseThrow(()->new FitPulseException("Избери подходяща рецепта за това хранене."));
        var source=recipe.meal();var replacement=new WeeklyMenu.Meal(current.label(),recipe.name(),source.ingredients(),source.preparation(),source.substitution(),recipe.code());
        var normalized=parse(canonicalJson(newMenuWithMeal(menu,day,meal,replacement)));
        plan.setWeeklyMenu(json(normalized));plan.setMeals(plainText(normalized));
    }
    private String newMenuWithMeal(WeeklyMenu menu,int day,int meal,WeeklyMenu.Meal replacement){var days=new ArrayList<>(menu.days());var old=days.get(day);var meals=new ArrayList<>(old.meals());meals.set(meal,replacement);days.set(day,new WeeklyMenu.Day(old.title(),meals));return json(new WeeklyMenu(menu.author(),menu.methodology(),days));}
    private void quantity(BigDecimal grams){if(grams==null||grams.compareTo(new BigDecimal("1"))<0||grams.compareTo(new BigDecimal("2000"))>0||grams.stripTrailingZeros().scale()>2)throw new FitPulseException("Въведи количество от 1 до 2000 г с най-много два десетични знака.");}
    private boolean compatible(Food current,Food other){if(!current.getCategory().equals(other.getCategory()))return false;if(current.getCategory().equals("Млечни продукти"))return current.getName().toLowerCase(Locale.ROOT).contains("мляко")==other.getName().toLowerCase(Locale.ROOT).contains("мляко");return true;}
    @Transactional public void change(Long id,int day,int meal,int item,BigDecimal grams,String replacement){
        quantity(grams);UserMealPlan plan=locked(id);WeeklyMenu menu=menu(plan);var old=selected(menu,day,meal,item);
        Food current=foods.findByCode(old.foodCode()).orElseThrow(()->new FitPulseException("Продуктът не е намерен."));
        Food food=replacement==null?current:foods.findByCode(replacement).filter(f->compatible(current,f)).orElseThrow(()->new FitPulseException("Избери подходящ продукт от същата категория."));
        List<WeeklyMenu.Day> days=new ArrayList<>(menu.days());var selectedDay=days.get(day);var meals=new ArrayList<>(selectedDay.meals());var selectedMeal=meals.get(meal);var ingredients=new ArrayList<>(selectedMeal.ingredients());ingredients.set(item,ingredient(food,grams));
        String title=replacement==null?selectedMeal.name():"Личен вариант на "+selectedMeal.name().replaceFirst("^Личен вариант на ","");
        meals.set(meal,new WeeklyMenu.Meal(selectedMeal.label(),title,ingredients,selectedMeal.preparation(),selectedMeal.substitution(),selectedMeal.recipeCode()));days.set(day,new WeeklyMenu.Day(selectedDay.title(),meals));
        WeeklyMenu changed=new WeeklyMenu(menu.author(),menu.methodology(),days);plan.setWeeklyMenu(json(changed));plan.setMeals(plainText(changed));
    }
    public List<Food> alternatives(Long id,int day,int meal,int item,String q){var old=selected(menu(owned(id)),day,meal,item);Food current=foods.findByCode(old.foodCode()).orElseThrow();String query=q==null?"":q.strip().toLowerCase(Locale.ROOT);return foods.findAllByOrderByCategoryAscNameAsc().stream().filter(f->compatible(current,f)&&!f.getCode().equals(current.getCode())).filter(f->f.getName().toLowerCase(Locale.ROOT).contains(query)).toList();}
    public record ShoppingItem(String code,String name,String state,String category,BigDecimal grams,boolean checked){}
    @Transactional public List<Integer> resetShopping(Long id,List<Integer> requested){
        var plan=locked(id);var selected=shopping(id,requested).days();
        checks.findAllByPlanOrderByIdDesc(plan).forEach(check->check.setChecked(false));return selected;
    }
    public Map<String,Long> totals(WeeklyMenu.Meal meal){return Map.of("calories",meal.getCalories(),"protein",meal.getProtein(),"fat",meal.getFat(),"carbs",meal.getCarbs());}
    public Map<String,Long> totals(WeeklyMenu.Day day){return Map.of("calories",day.getCalories(),"protein",day.getProtein(),"fat",day.getFat(),"carbs",day.getCarbs());}
    public Map<String,Object> savedTotals(Long id,int day,int meal,BigDecimal grams){var menu=menu(owned(id));return Map.of("saved",true,"grams",grams,"meal",totals(menu.days().get(day).meals().get(meal)),"day",totals(menu.days().get(day)));}
    public record ShoppingList(String scope,List<Integer> days,List<ShoppingItem> items){}
    public ShoppingList shopping(Long id,List<Integer> requested){
        UserMealPlan plan=owned(id);WeeklyMenu menu=menu(plan);if(requested!=null&&requested.stream().anyMatch(Objects::isNull))throw new FitPulseException("Избери валидни дни.");List<Integer> days=requested==null?List.of():requested.stream().distinct().sorted().toList();
        if(days.stream().anyMatch(d->d<1||d>menu.days().size()))throw new FitPulseException("Избери валидни дни.");
        Map<String,BigDecimal> amounts=new TreeMap<>();Map<String,WeeklyMenu.Ingredient> ingredients=new HashMap<>();
        for(int day:days)for(var meal:menu.days().get(day-1).meals())for(var item:meal.ingredients()){amounts.merge(item.foodCode(),BigDecimal.valueOf(item.grams()),BigDecimal::add);ingredients.put(item.foodCode(),item);}
        String signature=days+"|"+amounts;String scope;try{scope=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(signature.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}
        Map<String,ShoppingCheck> purchased=checks.findAllByPlanAndScope(plan,digest(days.toString())).stream().collect(java.util.stream.Collectors.toMap(ShoppingCheck::getFoodCode,c->c));
        checks.findAllByPlanOrderByIdDesc(plan).forEach(c->purchased.putIfAbsent(c.getFoodCode(),c));
        Map<String,ShoppingCheck> persistent=checks.findAllByPlanAndScope(plan,digest("product-check-v11")).stream().collect(java.util.stream.Collectors.toMap(ShoppingCheck::getFoodCode,c->c));
        List<ShoppingItem> result=new ArrayList<>();for(var entry:amounts.entrySet()){Food food=foods.findByCode(entry.getKey()).orElseThrow();var snapshot=ingredients.get(entry.getKey());var check=purchased.get(food.getCode());var stable=persistent.get(food.getCode());boolean bought=stable!=null?stable.isChecked():check!=null&&check.isChecked();result.add(new ShoppingItem(food.getCode(),food.getName(),snapshot.state(),food.getCategory(),entry.getValue(),bought));}
        result.sort(Comparator.comparing(ShoppingItem::category).thenComparing(ShoppingItem::name));return new ShoppingList(scope,days,result);
    }
    @Transactional public void check(Long id,List<Integer> days,String scope,String code,boolean checked){
        var plan=locked(id);
        var shopping=shopping(id,days);if(!shopping.scope().equals(scope))throw new FitPulseException("Списъкът е променен. Презареди го преди отбелязване.");if(shopping.items().stream().noneMatch(i->i.code().equals(code)))throw new FitPulseException("Продуктът не е в списъка.");
        String selection=digest("product-check-v11");ShoppingCheck check=checks.findByPlanAndScopeAndFoodCode(plan,selection,code).orElseGet(ShoppingCheck::new);check.setPlan(plan);check.setScope(selection);check.setFoodCode(code);check.setGrams(shopping.items().stream().filter(i->i.code().equals(code)).findFirst().orElseThrow().grams());check.setChecked(checked);checks.save(check);
    }
    private String digest(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    private String plainText(WeeklyMenu menu){StringBuilder text=new StringBuilder("Лично интерактивно меню\n");for(var day:menu.days()){text.append("\n").append(day.title()).append(" — ").append(day.getCalories()).append(" ккал; Б ").append(day.getProtein()).append(" г / М ").append(day.getFat()).append(" г / В ").append(day.getCarbs()).append(" г\n");for(var meal:day.meals()){text.append(meal.label()).append(": ").append(meal.name()).append("\n");for(var i:meal.ingredients())text.append(i.name()).append(" — ").append(i.getQuantity()).append(" (").append(i.state()).append(")\n");text.append(meal.preparation()).append("\n");}}return text.toString();}
}
