package com.fitpulse.model.dto;

import java.util.List;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown=true) public record WeeklyMenu(String author, String methodology, List<Day> days) {
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown=true) public record Ingredient(String name, double grams, String state, double proteinPer100, double fatPer100, double carbsPer100, String foodCode) {
        public double protein(){return grams*proteinPer100/100;}
        public double fat(){return grams*fatPer100/100;}
        public double carbs(){return grams*carbsPer100/100;}
        public String getQuantity(){return java.math.BigDecimal.valueOf(grams).stripTrailingZeros().toPlainString()+" г";}
    }
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown=true) public record Meal(String label,String name,List<Ingredient> ingredients,String preparation,String substitution,String recipeCode) {
        public Meal(String label,String name,List<Ingredient> ingredients,String preparation,String substitution){this(label,name,ingredients,preparation,substitution,com.fitpulse.service.RecipeAssets.code(name));}
        public double protein(){return ingredients.stream().mapToDouble(Ingredient::protein).sum();}
        public double fat(){return ingredients.stream().mapToDouble(Ingredient::fat).sum();}
        public double carbs(){return ingredients.stream().mapToDouble(Ingredient::carbs).sum();}
        public long getCalories(){return Math.round(4*protein()+9*fat()+4*carbs());}
        public long getProtein(){return Math.round(protein());}
        public long getFat(){return Math.round(fat());}
        public long getCarbs(){return Math.round(carbs());}
        public String getDescription(){return "Примерна порция с "+ingredients.stream().map(Ingredient::name).limit(3).reduce((a,b)->a+", "+b).orElse("")+".";}
        public String getImage(){return com.fitpulse.service.RecipeAssets.image(this);}
        public int getPreparationMinutes(){return com.fitpulse.service.RecipeAssets.minutes(this);}
        @com.fasterxml.jackson.annotation.JsonIgnore public List<String> getCookingSteps(){return com.fitpulse.service.RecipeAssets.steps(this);}
    }
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown=true) public record Day(String title,List<Meal> meals) {
        public double protein(){return meals.stream().mapToDouble(Meal::protein).sum();}
        public double fat(){return meals.stream().mapToDouble(Meal::fat).sum();}
        public double carbs(){return meals.stream().mapToDouble(Meal::carbs).sum();}
        public long getProtein(){return Math.round(protein());}
        public long getFat(){return Math.round(fat());}
        public long getCarbs(){return Math.round(carbs());}
        public long getCalories(){return Math.round(4*protein()+9*fat()+4*carbs());}
    }
}
