package com.fitpulse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.model.dto.WeeklyMenu;
import com.fitpulse.model.entity.MealPlanTemplate;
import org.springframework.stereotype.Component;

@Component
public class WeeklyMenuReader {
    private final ObjectMapper mapper;
    private final java.util.Map<String,String> catalogCovers;
    public WeeklyMenuReader(ObjectMapper mapper){this.mapper=mapper;try(var stream=new org.springframework.core.io.ClassPathResource("content/meal-covers.json").getInputStream()){this.catalogCovers=mapper.readValue(stream,new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String,String>>(){});}catch(java.io.IOException e){throw new IllegalStateException("Липсват снимките за каталога с режими.",e);}}
    public WeeklyMenu read(MealPlanTemplate template){
        try{return mapper.readValue(sourceJson(template),WeeklyMenu.class);}
        catch(JsonProcessingException e){throw new IllegalStateException("Невалидно седмично меню за режим "+template.getId(),e);}
    }
    public String sourceJson(MealPlanTemplate template){
        if(template.getWeeklyMenu()!=null&&!template.getWeeklyMenu().isBlank()){
            try{var menu=mapper.readValue(template.getWeeklyMenu(),WeeklyMenu.class);if(menu.days()!=null&&menu.days().size()==7&&menu.days().stream().allMatch(d->d.meals()!=null&&d.meals().size()>=3))return template.getWeeklyMenu();}catch(JsonProcessingException ignored){}
        }
        String name=template.getName().toLowerCase(java.util.Locale.ROOT);
        String source=name.contains("вегетари")?"vegetarian":name.contains("средизем")?"mediterranean":template.getGoal()==com.fitpulse.model.enums.FitnessGoal.MUSCLE?"muscle":template.getGoal()==com.fitpulse.model.enums.FitnessGoal.FAT_LOSS?"fat-loss":"maintenance";
        try(var stream=new org.springframework.core.io.ClassPathResource("content/menus/"+source+".json").getInputStream()){return new String(stream.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}catch(java.io.IOException e){throw new IllegalStateException("Липсва седмичният план.",e);}
    }
    public String cover(MealPlanTemplate template){String name=template.getName().toLowerCase(java.util.Locale.ROOT);String key=name.contains("вегетари")?"vegetarian":name.contains("средизем")?"mediterranean":name.contains("баланс")?"balanced":template.getGoal()==com.fitpulse.model.enums.FitnessGoal.PERFORMANCE?"performance":template.getGoal()==com.fitpulse.model.enums.FitnessGoal.MUSCLE?"muscle":template.getGoal()==com.fitpulse.model.enums.FitnessGoal.FAT_LOSS?"fat-loss":"maintenance";return catalogCovers.get(key);}
}
