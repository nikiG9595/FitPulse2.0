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
        public String getImage(){return RecipeAssets.image(code);}
    }
    private static final List<Recipe> RECIPES=load();
    // Reviewed photo links, including the seven dedicated recipe photos added later.
    // Keep this separate from recipe identity: one existing photo can illustrate several recipes.
    private static final Map<String,String> IMAGE_LINKS=Map.ofEntries(
            Map.entry("recipe-008",""), // Existing photo does not reliably show turkey with bulgur.
            Map.entry("recipe-003","recipe-032"), // Chicken with rice, not the omelette in 003.jpg.
            Map.entry("recipe-006","recipe-026"), // Spinach omelette, not avocado and fried egg toast.
            Map.entry("recipe-036","recipe-030"), // Lentils with rice, not the pasta visible in 036.jpg.
            Map.entry("recipe-011","toast-cottage-cheese-tomato"),
            Map.entry("recipe-014","milk-oats-fruit"),
            Map.entry("recipe-016","oats-milk-apple"),
            Map.entry("recipe-017","cottage-cheese-berries"),
            Map.entry("recipe-021","eggs-bread-cucumber"),
            Map.entry("recipe-026","oat-pancakes"),
            Map.entry("recipe-032","toast-cottage-cheese")
    );
    private RecipeAssets(){}
    private static List<Recipe> load(){try(var stream=RecipeAssets.class.getResourceAsStream("/content/recipes.json")){if(stream==null)throw new IllegalStateException("Липсват рецептите.");return List.of(new ObjectMapper().readValue(stream,Recipe[].class));}catch(Exception e){throw new ExceptionInInitializerError(e);}}
    public static List<Recipe> all(){return RECIPES;}
    public static Recipe find(String code,String name){return RECIPES.stream().filter(r->code!=null&&r.code().equals(code)).findFirst().orElseGet(()->RECIPES.stream().filter(r->r.name().equals(clean(name))).findFirst().orElse(null));}
    public static String code(String name){var recipe=find(null,name);return recipe==null?null:recipe.code();}
    private static String clean(String name){return name==null?"":name.replaceFirst("^Личен вариант на ","");}
    /** Recipe identity stays stable; reviewed image links may reuse a photo or select the existing placeholder. */
    public static String image(String code){
        if(code==null||!code.matches("recipe-[0-9]{3}"))return "/images/meals/meal-placeholder.svg";
        // New recipes carry an explicit reviewed photo or the neutral placeholder.
        // Keep the original 44 recipe identities and their established overrides intact.
        if(Integer.parseInt(code.substring(7))>44){
            var recipe=find(code,null);
            String path=recipe==null?null:recipe.imagePath();
            if(path!=null&&path.matches("/images/meals/recipes/[a-zA-Z0-9-]+\\.jpg")
                    &&RecipeAssets.class.getResource("/static"+path)!=null)return path;
            return "/images/meals/meal-placeholder.svg";
        }
        String imageCode=IMAGE_LINKS.getOrDefault(code,code);
        if(imageCode.isEmpty())return "/images/meals/meal-placeholder.svg";
        String base="/images/meals/recipes/"+imageCode;
        if(RecipeAssets.class.getResource("/static"+base+".jpg")!=null)return base+".jpg";
        return svg(code);
    }
    private static String svg(String code){String path="/images/meals/recipes/"+code+".svg";return RecipeAssets.class.getResource("/static"+path)!=null?path:"/images/meals/meal-placeholder.svg";}
    public static String image(WeeklyMenu.Meal meal){var recipe=find(meal.recipeCode(),meal.name());if(recipe==null)return "/images/meals/meal-placeholder.svg";
        Set<String> expected=recipe.meal().ingredients().stream().map(WeeklyMenu.Ingredient::name).collect(java.util.stream.Collectors.toSet());
        Set<String> actual=meal.ingredients().stream().map(WeeklyMenu.Ingredient::name).collect(java.util.stream.Collectors.toSet());
        return expected.equals(actual)?image(recipe.code()):svg(recipe.code());
    }
    public static int minutes(WeeklyMenu.Meal meal){var recipe=find(meal.recipeCode(),meal.name());return recipe==null?20:recipe.preparationMinutes();}
    public static List<String> steps(WeeklyMenu.Meal meal){var recipe=find(meal.recipeCode(),meal.name());return recipe==null||!Objects.equals(recipe.meal().preparation(),meal.preparation())?Arrays.stream(meal.preparation().split("(?<=[.!?])\\s+")).filter(s->!s.isBlank()).toList():List.copyOf(recipe.steps());}
    public static String bucket(String label){if(label.contains("Закуска"))return "breakfast";if(label.contains("Междинно")||label.contains("Следобедно"))return "snack";return "main";}
}
