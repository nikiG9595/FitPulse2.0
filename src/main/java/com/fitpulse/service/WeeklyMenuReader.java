package com.fitpulse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.model.dto.WeeklyMenu;
import com.fitpulse.model.entity.MealPlanTemplate;
import org.springframework.stereotype.Component;

@Component
public class WeeklyMenuReader {
    private static final java.util.Set<String> MENU_CODES=java.util.Set.of("fat-loss","muscle","maintenance","vegetarian","active","low-carb");
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
        String source=MENU_CODES.contains(template.getDemoCode()==null?"":template.getDemoCode())?template.getDemoCode():name.contains("нисковъглехидрат")?"low-carb":name.contains("вегетари")?"vegetarian":template.getGoal()==com.fitpulse.model.enums.FitnessGoal.MUSCLE?"muscle":template.getGoal()==com.fitpulse.model.enums.FitnessGoal.FAT_LOSS?"fat-loss":template.getGoal()==com.fitpulse.model.enums.FitnessGoal.PERFORMANCE?"active":"maintenance";
        try(var stream=new org.springframework.core.io.ClassPathResource("content/menus/"+source+".json").getInputStream()){return new String(stream.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}catch(java.io.IOException e){throw new IllegalStateException("Липсва седмичният план.",e);}
    }
    public String cover(MealPlanTemplate template){String code=template.getDemoCode();if("low-carb".equals(code)){String image="/images/meals/coversDiets/no-carbs.png";if(new org.springframework.core.io.ClassPathResource("static"+image).exists())return image;return fallbackCover(template);}if(code!=null&&java.util.Set.of("vegetarian","active","fat-loss","maintenance","muscle").contains(code)){String image="/images/meals/coversDiets/"+code+".jpg";if(new org.springframework.core.io.ClassPathResource("static"+image).exists())return image;}return fallbackCover(template);} public String fallbackCover(MealPlanTemplate template){String name=template.getName().toLowerCase(java.util.Locale.ROOT);if("low-carb".equals(template.getDemoCode())||name.contains("нисковъглехидрат"))return "/images/meals/meal-placeholder.svg";String key=name.contains("вегетари")?"vegetarian":name.contains("баланс")?"balanced":template.getGoal()==com.fitpulse.model.enums.FitnessGoal.PERFORMANCE?"performance":template.getGoal()==com.fitpulse.model.enums.FitnessGoal.MUSCLE?"muscle":template.getGoal()==com.fitpulse.model.enums.FitnessGoal.FAT_LOSS?"fat-loss":"maintenance";String cover=catalogCovers.get(key);return cover!=null&&cover.startsWith("recipe-")?RecipeAssets.image(cover):cover;}
}
