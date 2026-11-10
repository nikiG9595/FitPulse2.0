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
    @GetMapping public String details(@PathVariable Long id,Model m,jakarta.servlet.http.HttpServletResponse response){response.setHeader("Cache-Control","private, no-store, max-age=0");response.setHeader("Pragma","no-cache");var plan=nutrition.owned(id);m.addAttribute("plan",plan);m.addAttribute("menu",nutrition.menu(plan));return "programs/personal-meal";}
    @PostMapping("/name") public String rename(@PathVariable Long id,@RequestParam String name,RedirectAttributes flash){nutrition.rename(id,name);flash.addFlashAttribute("success","Името на личния режим е запазено.");return "redirect:/meal-plans/my/"+id;}
    @GetMapping("/recipe") public String recipe(@PathVariable Long id,@RequestParam int day,@RequestParam int meal,Model m){m.addAttribute("cookingMeal",nutrition.mealAt(id,day,meal));m.addAttribute("returnPath","/meal-plans/my/"+id+"#menu-meal-"+day+"-"+meal);m.addAttribute("personal",true);return "programs/recipe";}
    @GetMapping("/meal-replacements") public String replacements(@PathVariable Long id,@RequestParam int day,@RequestParam int meal,@RequestParam(defaultValue="")String q,Model m){m.addAttribute("plan",nutrition.owned(id));m.addAttribute("current",nutrition.mealAt(id,day,meal));m.addAttribute("recipes",nutrition.mealAlternatives(id,day,meal,q));m.addAttribute("day",day);m.addAttribute("meal",meal);m.addAttribute("q",q);return "programs/meal-replacements";}
    @PostMapping("/meal-replacements") public String replaceMeal(@PathVariable Long id,@RequestParam int day,@RequestParam int meal,@RequestParam String recipeCode,RedirectAttributes flash){try{nutrition.replaceMeal(id,day,meal,recipeCode);flash.addFlashAttribute("success","Храненето е заменено само в личното копие. Дневните стойности са преизчислени.");}catch(com.fitpulse.exception.FitPulseException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/meal-plans/my/"+id+"#menu-meal-"+day+"-"+meal;}
    @PostMapping("/ingredients/{day}/{meal}/{item}") public org.springframework.http.ResponseEntity<?> grams(@PathVariable Long id,@PathVariable int day,@PathVariable int meal,@PathVariable int item,@RequestParam BigDecimal grams,jakarta.servlet.http.HttpServletRequest request){
        nutrition.change(id,day,meal,item,grams,null);
        if(java.util.Optional.ofNullable(request.getHeader("Accept")).orElse("").contains("application/json"))return org.springframework.http.ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore()).body(nutrition.savedTotals(id,day,meal,grams));
        return org.springframework.http.ResponseEntity.status(303).location(java.net.URI.create(request.getContextPath()+"/meal-plans/my/"+id+"#menu-meal-"+day+"-"+meal)).build();
    }
    @GetMapping("/substitutions") public String substitutes(@PathVariable Long id,@RequestParam int day,@RequestParam int meal,@RequestParam int item,@RequestParam(defaultValue="") String q,Model m){var plan=nutrition.owned(id);m.addAttribute("plan",plan);m.addAttribute("current",nutrition.selected(nutrition.menu(plan),day,meal,item));m.addAttribute("foods",nutrition.alternatives(id,day,meal,item,q));m.addAttribute("q",q);m.addAttribute("day",day);m.addAttribute("meal",meal);m.addAttribute("item",item);return "programs/substitutions";}
    @PostMapping("/substitutions") public String substitute(@PathVariable Long id,@RequestParam int day,@RequestParam int meal,@RequestParam int item,@RequestParam String foodCode,@RequestParam BigDecimal grams,RedirectAttributes flash){try{nutrition.change(id,day,meal,item,grams,foodCode);flash.addFlashAttribute("success","Продуктът е заменен. Стойностите са преизчислени според новия състав.");}catch(com.fitpulse.exception.FitPulseException e){flash.addFlashAttribute("error",e.getMessage());}return "redirect:/meal-plans/my/"+id+"#menu-meal-"+day+"-"+meal;}
    @GetMapping("/shopping") public String shopping(@PathVariable Long id,@RequestParam(required=false) List<Integer> days,Model m){var plan=nutrition.owned(id);m.addAttribute("plan",plan);m.addAttribute("shopping",nutrition.shopping(id,days));return "programs/shopping";}
    @PostMapping("/shopping/reset") public String resetShopping(@PathVariable Long id,@RequestParam(required=false) List<Integer> days){var selected=nutrition.resetShopping(id,days);return "redirect:/meal-plans/my/"+id+"/shopping"+(selected.isEmpty()?"":"?days="+String.join(",",selected.stream().map(String::valueOf).toList()));}
    @PostMapping("/shopping/check") public org.springframework.http.ResponseEntity<?> check(@PathVariable Long id,@RequestParam(required=false) List<Integer> days,@RequestParam String scope,@RequestParam String foodCode,@RequestParam(defaultValue="false") boolean checked,jakarta.servlet.http.HttpServletRequest request){nutrition.check(id,days,scope,foodCode,checked);if(java.util.Optional.ofNullable(request.getHeader("Accept")).orElse("").contains("application/json"))return org.springframework.http.ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore()).body(Map.of("saved",true,"checked",checked));return org.springframework.http.ResponseEntity.status(303).location(java.net.URI.create(request.getContextPath()+"/meal-plans/my/"+id+"/shopping?days="+String.join(",",nutrition.shopping(id,days).days().stream().map(String::valueOf).toList()))).build();}
}
