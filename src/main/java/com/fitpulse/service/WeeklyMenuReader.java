package com.fitpulse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.model.dto.WeeklyMenu;
import com.fitpulse.model.entity.MealPlanTemplate;
import org.springframework.stereotype.Component;

@Component
public class WeeklyMenuReader {
    private final ObjectMapper mapper;
    public WeeklyMenuReader(ObjectMapper mapper){this.mapper=mapper;}
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
    public String cover(MealPlanTemplate template){var menu=read(template);String name=template.getName().toLowerCase(java.util.Locale.ROOT);if(name.contains("вегетари"))return imageAt(menu,3,1);if(name.contains("средизем"))return imageAt(menu,1,0);if(template.getGoal()==com.fitpulse.model.enums.FitnessGoal.PERFORMANCE)return imageAt(menu,2,3);if(template.getGoal()==com.fitpulse.model.enums.FitnessGoal.MUSCLE)return imageAt(menu,4,2);if(template.getGoal()==com.fitpulse.model.enums.FitnessGoal.FAT_LOSS)return imageAt(menu,0,2);return imageAt(menu,0,0);}
    private String imageAt(WeeklyMenu menu,int day,int meal){var meals=menu.days().get(Math.min(day,menu.days().size()-1)).meals();return meals.get(Math.min(meal,meals.size()-1)).getImage();}
}
