package com.fitpulse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.MealPersonalizationForm;
import com.fitpulse.model.dto.WeeklyMenu;
import com.fitpulse.model.entity.MealPlanTemplate;
import com.fitpulse.model.enums.FitnessGoal;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/** Read-only preview. The identical server-generated JSON is used when saving a new copy. */
@Service @Transactional(readOnly=true)
public class MealPersonalizationService {
    private final PersonalNutritionService nutrition;
    private final ObjectMapper mapper;
    private final Validator validator;
    public MealPersonalizationService(PersonalNutritionService nutrition,ObjectMapper mapper,Validator validator){this.nutrition=nutrition;this.mapper=mapper;this.validator=validator;}
    public record Preview(long resting,long daily,long target,long calories,long protein,long carbs,long fat,
                          WeeklyMenu menu,String json,String sourceHash,List<String> warnings,boolean canSave,Instant calculatedAt) {
        public Preview { warnings=List.copyOf(warnings); }
    }
    public static String inputKey(MealPersonalizationForm f){
        return hash(f.getSex()+"|"+f.getAge()+"|"+f.getHeight().stripTrailingZeros().toPlainString()+"|"+f.getWeight().stripTrailingZeros().toPlainString()+"|"+f.getActivity()+"|"+f.getEligible());
    }
    private static String hash(String text){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    private String canonicalSourceHash(MealPlanTemplate template,String canonical){return hash(canonical+"|"+template.getGoal()+"|"+template.getDemoCode()+"|"+template.getName()+"|"+template.getExampleFoods());}
    public String sourceHash(MealPlanTemplate template,String source){return canonicalSourceHash(template,nutrition.canonicalJson(source));}
    public boolean fresh(Preview preview){return preview!=null&&preview.calculatedAt().isAfter(Instant.now().minusSeconds(900));}
    public Preview calculate(MealPersonalizationForm form,MealPlanTemplate template,String source){
        if(!validator.validate(form).isEmpty())throw new FitPulseException("Проверете личните данни и потвърждението за приложимост.");
        String canonical=nutrition.canonicalJson(source);
        WeeklyMenu original=nutrition.parse(canonical);
        if(original.days()==null||original.days().size()!=7||original.days().stream().anyMatch(d->d.meals()==null||d.meals().size()!=5))throw new FitPulseException("Режимът трябва да съдържа точно 7 дни с по 5 хранения.");
        double weight=form.getWeight().doubleValue(),height=form.getHeight().doubleValue();
        double bmr=10*weight+6.25*height-5*form.getAge()+(form.getSex().equals("MALE")?5:-161);
        double activity=switch(form.getActivity()){case "LOW"->1.2;case "MODERATE"->1.55;case "HIGH"->1.725;default->throw new FitPulseException("Невалидна активност.");};
        double tdee=bmr*activity;
        double multiplier=switch(Objects.toString(template.getDemoCode(),"")){
            case "fat-loss"->.85;case "muscle"->1.10;
            case "maintenance","active","vegetarian","low-carb"->1;
            default->template.getGoal()==FitnessGoal.FAT_LOSS?.85:template.getGoal()==FitnessGoal.MUSCLE?1.10:1;
        };
        long target=Math.round(tdee*multiplier);
        if(!Double.isFinite(bmr)||!Double.isFinite(tdee)||bmr<=0||target<=0)throw new FitPulseException("Не може да се изчисли достоверна цел. Необходима е индивидуална професионална оценка.");
        List<String> warnings=new ArrayList<>();boolean canSave=true;
        double bmi=weight/Math.pow(height/100,2);
        // Conservative software support limits, not diagnostic criteria or a safety guarantee.
        if(form.getAge()>75||bmi<18.5||bmi>=40||target<bmr||target/weight<18||target/weight>60){
            warnings.add("Тези данни или изчисления са извън консервативния обхват на автоматичното персонализиране. Необходима е индивидуална професионална оценка; копие не може да се създаде.");canSave=false;
        }
        List<WeeklyMenu.Day> days=new ArrayList<>();
        for(var day:original.days()){
            double base=energy(day);if(!Double.isFinite(base)||base<=0)throw new FitPulseException("Менюто съдържа невалидни хранителни стойности.");
            double upper=1.5;
            for(var meal:day.meals()){
                if(meal.ingredients()==null||meal.ingredients().isEmpty())throw new FitPulseException("Липсват продукти за хранене.");
                double mass=0;
                for(var item:meal.ingredients()){
                    if(!Double.isFinite(item.grams())||item.grams()<=0||!Double.isFinite(item.proteinPer100())||!Double.isFinite(item.fatPer100())||!Double.isFinite(item.carbsPer100())||item.proteinPer100()<0||item.fatPer100()<0||item.carbsPer100()<0)throw new FitPulseException("Невалиден продукт в менюто.");
                    mass+=item.grams();upper=Math.min(upper,portionCap(item)/item.grams());
                }
                String label=meal.label().toLowerCase(Locale.ROOT);
                double mealCap=label.contains("междин")||label.contains("следобед")?450:label.contains("закуска")?650:1000;
                upper=Math.min(upper,mealCap/mass);
            }
            if(upper<.65)throw new FitPulseException("Наличните рецепти не могат да се адаптират в разумните граници за порции. Необходима е индивидуална професионална оценка.");
            double wanted=target/base,factor=Math.max(.65,Math.min(upper,wanted));
            List<WeeklyMenu.Meal> meals=new ArrayList<>();
            for(var meal:day.meals()){
                List<WeeklyMenu.Ingredient> ingredients=new ArrayList<>();
                for(var i:meal.ingredients()){
                    // Keep tiny seasonings within the existing personal quantity editor's 1 g minimum.
                    double grams=Math.max(1,java.math.BigDecimal.valueOf(i.grams()*factor).setScale(2,java.math.RoundingMode.HALF_UP).doubleValue());
                    if(grams<=0||grams>portionCap(i)+.01)throw new FitPulseException("Получава се неподходящо количество за продукт.");
                    ingredients.add(new WeeklyMenu.Ingredient(i.name(),grams,i.state(),i.proteinPer100(),i.fatPer100(),i.carbsPer100(),i.foodCode()));
                }
                meals.add(new WeeklyMenu.Meal(meal.label(),meal.name(),List.copyOf(ingredients),meal.preparation(),meal.substitution(),meal.recipeCode()));
            }
            var adaptedDay=new WeeklyMenu.Day(day.title(),List.copyOf(meals));days.add(adaptedDay);
            double deviation=Math.abs(energy(adaptedDay)-target)/target;
            if(deviation>.05)warnings.add(day.title()+": реално постигнати "+adaptedDay.getCalories()+" ккал при цел "+target+" ккал. Ограниченията на порциите не позволяват по-близък резултат.");
            if(deviation>.15)canSave=false;
        }
        if(!canSave&&warnings.stream().noneMatch(w->w.contains("професионална")))warnings.add("Отклонението е прекалено голямо за автоматично записване. Необходима е индивидуална професионална оценка или друга подходяща основа за меню.");
        var menu=new WeeklyMenu(original.author(),Objects.toString(original.methodology(),"")+" Персонализирани количества за новото лично копие; изчислена дневна цел: "+target+" ккал. Стойностите са ориентировъчни и са изчислени от съставките. Показаната цел не гарантира безопасност или индивидуален резултат.",List.copyOf(days));
        String json;try{json=mapper.writeValueAsString(menu);}catch(Exception e){throw new IllegalStateException(e);}
        // Preview the serialized snapshot that will be stored, including derived nutritional totals.
        menu=nutrition.parse(json);
        long calories=Math.round(menu.days().stream().mapToDouble(MealPersonalizationService::energy).average().orElseThrow());
        long protein=Math.round(menu.days().stream().mapToDouble(WeeklyMenu.Day::protein).average().orElseThrow());
        long carbs=Math.round(menu.days().stream().mapToDouble(WeeklyMenu.Day::carbs).average().orElseThrow());
        long fat=Math.round(menu.days().stream().mapToDouble(WeeklyMenu.Day::fat).average().orElseThrow());
        return new Preview(Math.round(bmr),Math.round(tdee),target,calories,protein,carbs,fat,menu,json,canonicalSourceHash(template,canonical),warnings,canSave,Instant.now());
    }
    private static double energy(WeeklyMenu.Day day){return 4*day.protein()+4*day.carbs()+9*day.fat();}
    private static double portionCap(WeeklyMenu.Ingredient i){
        String name=i.name().toLowerCase(Locale.ROOT),state=i.state().toLowerCase(Locale.ROOT);
        if(name.contains("зехтин")||name.contains("олио"))return 20;
        if(name.contains("сметана"))return 120;
        if(name.contains("орех")||name.contains("бадем")||name.contains("семе")||name.contains("чиа")||name.contains("тахан")||name.contains("фъстъчено"))return 40;
        if(name.contains("мед"))return 30;
        if(name.contains("яйц"))return 250;
        if(name.contains("сирене")||name.contains("кашкавал")||name.contains("фета"))return 100;
        if(name.contains("мляко")||name.contains("скир")||name.contains("извара"))return 400;
        if(state.contains("сух")&&(name.contains("ориз")||name.contains("паста")||name.contains("булгур")||name.contains("кускус")||name.contains("овес")))return 150;
        if(name.contains("хляб")||name.contains("тортила")||name.contains("питка")||name.contains("сухари"))return 150;
        return 350;
    }
}
