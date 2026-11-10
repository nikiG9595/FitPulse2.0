package com.fitpulse.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.model.entity.*;
import com.fitpulse.repository.*;
import com.fitpulse.service.ExerciseLibraryService;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @Profile({"dev","prod"}) @Order(20)
@ConditionalOnProperty(name="fitpulse.local-admin-promotion.confirm",havingValue="false",matchIfMissing=true)
public class ModuleContentInitializer implements ApplicationRunner {
    private final ExerciseRepository exercises;private final FoodRepository foods;private final ObjectMapper mapper;
    private final ProgramTemplateItemRepository templates;private final UserProgramItemRepository personal;private final WorkoutSetRepository sets;
    public ModuleContentInitializer(ExerciseRepository exercises,FoodRepository foods,ObjectMapper mapper,ProgramTemplateItemRepository templates,UserProgramItemRepository personal,WorkoutSetRepository sets){this.exercises=exercises;this.foods=foods;this.mapper=mapper;this.templates=templates;this.personal=personal;this.sets=sets;}
    @Override @Transactional public void run(ApplicationArguments args)throws Exception {
        try(var stream=new ClassPathResource("content/exercises.json").getInputStream()){for(Exercise e:mapper.readValue(stream,Exercise[].class))if(exercises.findByCode(e.getCode()).isEmpty())exercises.save(e);}
        try(var stream=new ClassPathResource("content/foods.json").getInputStream()){for(Food f:mapper.readValue(stream,Food[].class))if(foods.findByCode(f.getCode()).isEmpty())foods.save(f);}
        exercises.flush();
        java.util.Map<String,Exercise> lookup=new java.util.HashMap<>();
        for(var exercise:exercises.findAllByOrderByName()){
            lookup.putIfAbsent(ExerciseLibraryService.normalize(exercise.getName()),exercise);
            for(String alias:exercise.getAliases().split("\\|"))if(!alias.isBlank())lookup.putIfAbsent(ExerciseLibraryService.normalize(alias),exercise);
        }
        for(var item:templates.findAll())if(item.getExercise()==null)item.setExercise(lookup.get(ExerciseLibraryService.normalize(item.getExerciseName())));
        for(var item:personal.findAll())if(item.getExercise()==null)item.setExercise(lookup.get(ExerciseLibraryService.normalize(item.getExerciseName())));
        for(var set:sets.findAll())if(set.getExercise()==null)set.setExercise(lookup.get(ExerciseLibraryService.normalize(set.getExerciseName())));
    }
}
