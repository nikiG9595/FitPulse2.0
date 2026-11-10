package com.fitpulse.service;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.util.regex.Pattern;

/** Narrow repair for the demonstrated single-step apple/cottage-cheese recipe, not generic deduplication. */
public final class RecipeInstructions {
    public static final String APPLE="Нарежи ябълката и поднеси отделно от изварата.";
    private static final Pattern REPEATED=Pattern.compile(Pattern.quote(APPLE)+"(?:\\s*"+Pattern.quote(APPLE)+")+");
    private RecipeInstructions(){}
    public static String cleanAppleText(String text){return text==null?null:REPEATED.matcher(text).replaceAll(java.util.regex.Matcher.quoteReplacement(APPLE));}
    public static String repair(String json,ObjectMapper mapper){
        if(json==null||json.isBlank())return json;
        try{
            JsonNode root=mapper.readTree(json);boolean changed=false;
            for(var day:root.path("days"))for(var meal:day.path("meals")){
                String recipeName=meal.path("name").asText().replaceFirst("^Личен вариант на ","");
                boolean known=meal.path("recipeCode").asText().equals("recipe-002")||recipeName.equals("Извара и ябълка")||recipeName.equals("Извара 5% и ябълка");
                if(!known)continue;
                boolean cottage=false,apple=false;
                for(var ingredient:meal.path("ingredients")){String name=ingredient.path("name").asText();cottage|=name.contains("Извара");apple|=name.equals("Ябълка");}
                if(!cottage||!apple||!(meal instanceof ObjectNode object))continue;
                if(!meal.path("preparation").isTextual())continue;
                String old=meal.path("preparation").asText();String cleaned=cleanAppleText(old);
                if(!old.equals(cleaned)){object.put("preparation",cleaned);changed=true;}
                // Previously serialized derived steps are not instructions to clone back into the recipe.
                for(String field:new String[]{"cookingSteps","steps"})if(meal.path(field) instanceof ArrayNode array){
                    ArrayNode result=mapper.createArrayNode();boolean previousApple=false;
                    for(var step:array){boolean current=step.isTextual()&&step.asText().strip().equals(APPLE);if(current&&previousApple){changed=true;continue;}result.add(step);previousApple=current;}
                    if(result.size()!=array.size())object.set(field,result);
                }
            }
            return changed?mapper.writeValueAsString(root):json;
        }catch(com.fasterxml.jackson.core.JsonProcessingException e){return json;}
    }
}
