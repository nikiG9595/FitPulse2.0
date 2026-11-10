package com.fitpulse.controller;

import com.fitpulse.service.PersonalNutritionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.*;

@Controller @RequestMapping("/meal-plans/my/{id}")
public class PersonalNutritionController {
    private final PersonalNutritionService nutrition;
    public PersonalNutritionController(PersonalNutritionService nutrition){this.nutrition=nutrition;}
    @GetMapping public String details(@PathVariable Long id,Model m){var plan=nutrition.owned(id);m.addAttribute("plan",plan);m.addAttribute("menu",nutrition.menu(plan));return "programs/personal-meal";}
    @PostMapping("/name") public String rename(@PathVariable Long id,@RequestParam String name,RedirectAttributes flash){nutrition.rename(id,name);flash.addFlashAttribute("success","Името на личния режим е запазено.");return "redirect:/meal-plans/my/"+id;}
    @GetMapping("/recipe") public String recipe(@PathVariable Long id,@RequestParam int day,@RequestParam int meal,Model m){m.addAttribute("cookingMeal",nutrition.mealAt(id,day,meal));m.addAttribute("returnPath","/meal-plans/my/"+id+"#menu-day-"+day);m.addAttribute("personal",true);return "programs/recipe";}
    @GetMapping("/meal-replacements") public String replacements(@PathVariable Long id,@RequestParam int day,@RequestParam int meal,@RequestParam(defaultValue="")String q,Model m){m.addAttribute("plan",nutrition.owned(id));m.addAttribute("current",nutrition.mealAt(id,day,meal));m.addAttribute("recipes",nutrition.mealAlternatives(id,day,meal,q));m.addAttribute("day",day);m.addAttribute("meal",meal);m.addAttribute("q",q);return "programs/meal-replacements";}
    @PostMapping("/meal-replacements") public String replaceMeal(@PathVariable Long id,@RequestParam int day,@RequestParam int meal,@RequestParam String recipeCode,RedirectAttributes flash){nutrition.replaceMeal(id,day,meal,recipeCode);flash.addFlashAttribute("success","Храненето е заменено само в личното копие. Дневните стойности са преизчислени.");return "redirect:/meal-plans/my/"+id+"#menu-day-"+day;}
    @PostMapping("/ingredients/{day}/{meal}/{item}") public org.springframework.http.ResponseEntity<?> grams(@PathVariable Long id,@PathVariable int day,@PathVariable int meal,@PathVariable int item,@RequestParam BigDecimal grams,jakarta.servlet.http.HttpServletRequest request){
        nutrition.change(id,day,meal,item,grams,null);
        if(java.util.Optional.ofNullable(request.getHeader("Accept")).orElse("").contains("application/json"))return org.springframework.http.ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore()).body(Map.of("saved",true,"grams",grams));
        return org.springframework.http.ResponseEntity.status(303).location(java.net.URI.create(request.getContextPath()+"/meal-plans/my/"+id+"#menu-day-"+day)).build();
    }
    @GetMapping("/substitutions") public String substitutes(@PathVariable Long id,@RequestParam int day,@RequestParam int meal,@RequestParam int item,@RequestParam(defaultValue="") String q,Model m){var plan=nutrition.owned(id);m.addAttribute("plan",plan);m.addAttribute("current",nutrition.selected(nutrition.menu(plan),day,meal,item));m.addAttribute("foods",nutrition.alternatives(id,day,meal,item,q));m.addAttribute("q",q);m.addAttribute("day",day);m.addAttribute("meal",meal);m.addAttribute("item",item);return "programs/substitutions";}
    @PostMapping("/substitutions") public String substitute(@PathVariable Long id,@RequestParam int day,@RequestParam int meal,@RequestParam int item,@RequestParam String foodCode,@RequestParam BigDecimal grams,RedirectAttributes flash){nutrition.change(id,day,meal,item,grams,foodCode);flash.addFlashAttribute("success","Продуктът е заменен. Стойностите са преизчислени според новия състав.");return "redirect:/meal-plans/my/"+id+"#menu-day-"+day;}
    @GetMapping("/shopping") public String shopping(@PathVariable Long id,@RequestParam(required=false) List<Integer> days,Model m){var plan=nutrition.owned(id);m.addAttribute("plan",plan);m.addAttribute("shopping",nutrition.shopping(id,days));return "programs/shopping";}
    @PostMapping("/shopping/check") public String check(@PathVariable Long id,@RequestParam(required=false) List<Integer> days,@RequestParam String scope,@RequestParam String foodCode,@RequestParam(defaultValue="false") boolean checked){nutrition.check(id,days,scope,foodCode,checked);return "redirect:/meal-plans/my/"+id+"/shopping?days="+String.join(",",nutrition.shopping(id,days).days().stream().map(String::valueOf).toList());}
}
