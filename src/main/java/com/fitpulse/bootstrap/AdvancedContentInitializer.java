package com.fitpulse.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.model.entity.Exercise;
import com.fitpulse.repository.*;
import com.fitpulse.service.WeeklyMenuReader;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @Profile({"dev","prod"}) @Order(30)
@ConditionalOnProperty(name="fitpulse.local-admin-promotion.confirm",havingValue="false",matchIfMissing=true)
public class AdvancedContentInitializer implements ApplicationRunner {
    private final ExerciseRepository exercises;private final MealPlanTemplateRepository menus;private final WeeklyMenuReader reader;private final ObjectMapper mapper;
    public AdvancedContentInitializer(ExerciseRepository exercises,MealPlanTemplateRepository menus,WeeklyMenuReader reader,ObjectMapper mapper){this.exercises=exercises;this.menus=menus;this.reader=reader;this.mapper=mapper;}
    @Override @Transactional public void run(ApplicationArguments args)throws Exception{
        try(var stream=new ClassPathResource("content/exercises.json").getInputStream()){for(var source:mapper.readValue(stream,Exercise[].class)){var existing=exercises.findByCode(source.getCode());if(existing.isPresent()&&existing.get().getPrimaryZones()==null){existing.get().setPrimaryZones(source.getPrimaryZones());existing.get().setSecondaryZones(source.getSecondaryZones());}}}
        for(var menu:menus.findAll()){String original=menu.getWeeklyMenu();String structured=reader.sourceJson(menu);if(!java.util.Objects.equals(original,structured)){if(original!=null&&!original.isBlank()&&menu.getOriginalWeeklyMenu()==null)menu.setOriginalWeeklyMenu(original);menu.setWeeklyMenu(structured);}}
        // Original text, administrator content and all personal copies remain untouched.
    }
}
