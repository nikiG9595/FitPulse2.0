package com.fitpulse.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.model.dto.WeeklyMenu;
import com.fitpulse.model.entity.*;
import com.fitpulse.model.enums.FitnessGoal;
import com.fitpulse.repository.*;
import com.fitpulse.service.TrainingSessions;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Adds missing demo content after Flyway. Existing personal copies are never updated. */
@Component @org.springframework.core.annotation.Order(10) @Profile({"dev","prod"})
@ConditionalOnProperty(name="fitpulse.local-admin-promotion.confirm",havingValue="false",matchIfMissing=true)
public class DemoPlanContentInitializer implements ApplicationRunner {
    private final MealPlanTemplateRepository meals;
    private final ProgramTemplateRepository programs;
    private final ProgramTemplateItemRepository exercises;
    private final TrainerRepository trainers;
    private final ObjectMapper mapper;
    public DemoPlanContentInitializer(MealPlanTemplateRepository meals,ProgramTemplateRepository programs,
            ProgramTemplateItemRepository exercises,TrainerRepository trainers,ObjectMapper mapper){
        this.meals=meals;this.programs=programs;this.exercises=exercises;this.trainers=trainers;this.mapper=mapper;
    }
    public record MenuDefinition(String code,String name,String goal) {}

    @Override @Transactional
    public void run(ApplicationArguments args) throws Exception {
        Trainer author=trainers.findFirstByName("Charles Walker").orElse(null);
        for(ProgramTemplate program:programs.findAll()){
            // Populate only empty templates; never replace administrator-authored exercises.
            if(program.getItems().isEmpty()){
                for(ProgramTemplateItem item:TrainingSessions.outline(program)){
                    item.setTemplate(program);exercises.save(item);
                }
                if(program.getAuthor()==null)program.setAuthor(author);
                if("Upper/lower разпределение за сила.".equals(program.getDescription()))program.setDescription("Редуване на горна и долна част за развитие на сила.");
            }
        }
        MenuDefinition[] definitions=mapper.readValue(resource("index"),MenuDefinition[].class);
        for(MenuDefinition definition:definitions){
            if(meals.findByDemoCode(definition.code()).isPresent())continue;
            List<String> aliases=switch(definition.code()){
                case "muscle" -> List.of(definition.name(),"Пример с повече протеин");
                case "maintenance" -> List.of(definition.name(),"Балансиран примерен ден");
                default -> List.of(definition.name());
            };
            // Reuse an exact existing demo name rather than introducing a duplicate.
            List<MealPlanTemplate> existing=meals.findAll();
            MealPlanTemplate template=existing.stream()
                    .filter(m->definition.name().equalsIgnoreCase(m.getName().strip()))
                    .min(Comparator.comparing(MealPlanTemplate::getId))
                    .orElseGet(()->existing.stream().filter(m->aliases.stream().anyMatch(name->name.equalsIgnoreCase(m.getName().strip())))
                            .min(Comparator.comparing(MealPlanTemplate::getId)).orElseGet(MealPlanTemplate::new));
            String json=resource(definition.code());WeeklyMenu menu=mapper.readValue(json,WeeklyMenu.class);
            template.setDemoCode(definition.code());template.setName(definition.name());
            template.setGoal(FitnessGoal.valueOf(definition.goal()));template.setAuthor(author);template.setActive(true);
            template.setDescription("Разнообразен седемдневен демонстрационен план с конкретни количества, приготвяне и ориентировъчни хранителни стойности.");
            template.setDisclaimer("Общ демонстрационен пример, а не индивидуално предписание. Charles Walker е демонстрационен автор, без удостоверено реално авторство. Потребностите са индивидуални; този прием не гарантира отслабване, поддържане или покачване. Не е предназначен за деца, бременност или медицински състояния. Съобразете алергените и личните си нужди със специалист.");
            template.setWeeklyMenu(json);template.setMeals(plainText(menu));
            template.setExampleFoods(menu.days().stream().flatMap(d->d.meals().stream()).flatMap(m->m.ingredients().stream()).map(WeeklyMenu.Ingredient::name).distinct().reduce((a,b)->a+", "+b).orElse(""));
            meals.save(template);
        }
    }
    private String resource(String code) throws java.io.IOException {
        try(var stream=new ClassPathResource("content/menus/"+code+".json").getInputStream()){
            return new String(stream.readAllBytes(),StandardCharsets.UTF_8);
        }
    }
    private String plainText(WeeklyMenu menu){
        StringBuilder text=new StringBuilder("Демонстрационен автор: Charles Walker (неудостоверено авторство).\n\n");
        for(var day:menu.days()){
            text.append(day.title()).append(" — ≈ ").append(day.getCalories()).append(" ккал; белтъчини ").append(day.getProtein()).append(" г; мазнини ").append(day.getFat()).append(" г; въглехидрати ").append(day.getCarbs()).append(" г.\n");
            for(var meal:day.meals()){
                text.append(meal.label()).append(": ").append(meal.name()).append("\n");
                for(var ingredient:meal.ingredients())text.append(" • ").append(ingredient.name()).append(" — ").append(ingredient.getQuantity()).append(" (").append(ingredient.state()).append(").\n");
                text.append("Приготвяне: ").append(meal.preparation()).append("\nЗамяна: ").append(meal.substitution()).append("\n");
            }
            text.append("\n");
        }
        return text.append(menu.methodology()).append("\nОбщ пример, а не индивидуално предписание. Няма гарантиран резултат.").toString();
    }
}
