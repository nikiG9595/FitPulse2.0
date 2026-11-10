package com.fitpulse.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.repository.UserMealPlanRepository;
import com.fitpulse.service.RecipeInstructions;
import org.springframework.boot.*;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @Profile({"dev","prod"}) @Order(40)
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="fitpulse.local-admin-promotion.confirm",havingValue="false",matchIfMissing=true)
public class RecipeInstructionRepair implements ApplicationRunner {
    private final UserMealPlanRepository plans;private final ObjectMapper mapper;
    public RecipeInstructionRepair(UserMealPlanRepository plans,ObjectMapper mapper){this.plans=plans;this.mapper=mapper;}
    @Override @Transactional public void run(ApplicationArguments args){
        int repaired=0;
        for(var plan:plans.findAll()){
            String original=plan.getWeeklyMenu(),cleaned=RecipeInstructions.repair(original,mapper);
            if(!java.util.Objects.equals(original,cleaned)){
                plan.setWeeklyMenu(cleaned);plan.setMeals(RecipeInstructions.cleanAppleText(plan.getMeals()));repaired++;
                org.slf4j.LoggerFactory.getLogger(getClass()).info("Repaired identical apple-recipe instructions in personal meal plan {}",plan.getId());
            }
        }
        org.slf4j.LoggerFactory.getLogger(getClass()).info("V12 targeted recipe repair: {} personal copies changed; other instructions preserved",repaired);
    }
}
