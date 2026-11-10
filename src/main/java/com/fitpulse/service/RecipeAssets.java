package com.fitpulse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fitpulse.model.dto.WeeklyMenu;
import java.util.*;

/** Immutable local recipe metadata. Food calculations still use structured ingredient snapshots. */
public final class RecipeAssets {
    @JsonIgnoreProperties(ignoreUnknown=true)
    public record Recipe(String code,String name,String label,String imagePath,int preparationMinutes,List<String> steps,WeeklyMenu.Meal meal){
        public Recipe { steps=List.copyOf(steps); }
    }
    private static final List<Recipe> RECIPES=load();
    private RecipeAssets(){}
    private static List<Recipe> load(){try(var stream=RecipeAssets.class.getResourceAsStream("/content/recipes.json")){if(stream==null)throw new IllegalStateException("Липсват рецептите.");return List.of(new ObjectMapper().readValue(stream,Recipe[].class));}catch(Exception e){throw new ExceptionInInitializerError(e);}}
    public static List<Recipe> all(){return RECIPES;}
    public static Recipe find(String code,String name){return RECIPES.stream().filter(r->code!=null&&r.code().equals(code)).findFirst().orElseGet(()->RECIPES.stream().filter(r->r.name().equals(clean(name))).findFirst().orElse(null));}
    public static String code(String name){var recipe=find(null,name);return recipe==null?null:recipe.code();}
    private static String clean(String name){return name==null?"":name.replaceFirst("^Личен вариант на ","");}
    public static String image(WeeklyMenu.Meal meal){var recipe=find(meal.recipeCode(),meal.name());if(recipe==null)return "/images/meals/meal-placeholder.svg";
        if(recipe.imagePath().endsWith(".svg"))return recipe.imagePath();
        Set<String> expected=recipe.meal().ingredients().stream().map(WeeklyMenu.Ingredient::name).collect(java.util.stream.Collectors.toSet());
        Set<String> actual=meal.ingredients().stream().map(WeeklyMenu.Ingredient::name).collect(java.util.stream.Collectors.toSet());
        return expected.equals(actual)?recipe.imagePath():"/images/meals/recipes/"+recipe.code()+".svg";
    }
    public static int minutes(WeeklyMenu.Meal meal){var recipe=find(meal.recipeCode(),meal.name());return recipe==null?20:recipe.preparationMinutes();}
    public static List<String> steps(WeeklyMenu.Meal meal){var recipe=find(meal.recipeCode(),meal.name());return recipe==null||!Objects.equals(recipe.meal().preparation(),meal.preparation())?Arrays.stream(meal.preparation().split("(?<=[.!?])\\s+")).filter(s->!s.isBlank()).toList():List.copyOf(recipe.steps());}
    public static String bucket(String label){if(label.contains("Закуска"))return "breakfast";if(label.contains("Междинно")||label.contains("Следобедно"))return "snack";return "main";}
}
