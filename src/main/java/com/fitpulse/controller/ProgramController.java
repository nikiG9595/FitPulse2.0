package com.fitpulse.controller;
import com.fitpulse.model.dto.ProgramItemForm; import com.fitpulse.service.WellnessService; import jakarta.validation.Valid; import org.springframework.format.annotation.DateTimeFormat; import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.web.bind.annotation.*; import java.time.LocalDateTime;
@Controller public class ProgramController {private final WellnessService service;private final com.fitpulse.service.WeeklyMenuReader menuReader;public ProgramController(WellnessService s,com.fitpulse.service.WeeklyMenuReader reader){service=s;menuReader=reader;}
 @ModelAttribute public void libraryMembership(Model model,org.springframework.security.core.Authentication authentication){
     var programLinks=new java.util.LinkedHashMap<Long,Long>();var mealLinks=new java.util.LinkedHashMap<Long,Long>();
     var programCounts=new java.util.HashMap<Long,Integer>();var mealCounts=new java.util.HashMap<Long,Integer>();
     if(authentication!=null&&authentication.isAuthenticated()&&!(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)){
         for(var p:service.myPrograms())if(p.getSourceTemplate()!=null){programLinks.merge(p.getSourceTemplate().getId(),p.getId(),Math::min);programCounts.merge(p.getSourceTemplate().getId(),1,Integer::sum);}
         for(var p:service.myMeals())if(p.getSourceTemplate()!=null){mealLinks.merge(p.getSourceTemplate().getId(),p.getId(),Math::min);mealCounts.merge(p.getSourceTemplate().getId(),1,Integer::sum);}
     }
     model.addAttribute("myProgramLinks",programLinks);model.addAttribute("myMealLinks",mealLinks);
     model.addAttribute("legacyProgramDuplicates",programCounts.values().stream().filter(n->n>1).count());model.addAttribute("legacyMealDuplicates",mealCounts.values().stream().filter(n->n>1).count());
 }
 @PostMapping("/programs/my/{id}/remove") public String removeProgram(@PathVariable Long id,org.springframework.web.servlet.mvc.support.RedirectAttributes flash){service.removeProgram(id);flash.addFlashAttribute("success","Програмата е премахната от списъка. Личното копие и историята са запазени.");return "redirect:/programs/my";}
 @PostMapping("/meal-plans/my/{id}/remove") public String removeMeal(@PathVariable Long id,org.springframework.web.servlet.mvc.support.RedirectAttributes flash){service.removeMeal(id);flash.addFlashAttribute("success","Режимът е премахнат от списъка. Редакциите и покупките са запазени.");return "redirect:/meal-plans/my";}
 @GetMapping("/programs") public String library(@RequestParam(defaultValue="") String q,@RequestParam(required=false) com.fitpulse.model.enums.FitnessGoal goal,@RequestParam(required=false) com.fitpulse.model.enums.ExperienceLevel level,@RequestParam(required=false) com.fitpulse.model.enums.TrainingLocation location,Model m){
     String search=q.strip();String needle=search.toLowerCase(java.util.Locale.ROOT);
     m.addAttribute("templates",service.library().stream().filter(p->goal==null||p.getGoal()==goal).filter(p->level==null||p.getExperience()==level).filter(p->location==null||p.getLocation()==location).filter(p->needle.isEmpty()||(p.getName()+" "+p.getDescription()).toLowerCase(java.util.Locale.ROOT).contains(needle)).toList());
     m.addAttribute("q",search);m.addAttribute("goal",goal);m.addAttribute("level",level);m.addAttribute("location",location);
     m.addAttribute("goals",com.fitpulse.model.enums.FitnessGoal.values());m.addAttribute("levels",com.fitpulse.model.enums.ExperienceLevel.values());m.addAttribute("locations",com.fitpulse.model.enums.TrainingLocation.values());return "programs/library";
 }
 @GetMapping("/programs/{id}") public String preview(@PathVariable Long id,Model m){var template=service.publishedProgram(id);m.addAttribute("template",template);m.addAttribute("sessions",com.fitpulse.service.TrainingSessions.templateSessions(service.programOutline(template)));return "programs/preview";}
 @PostMapping("/programs/{id}/copy") public String copy(@PathVariable Long id,org.springframework.web.servlet.mvc.support.RedirectAttributes flash){try{return "redirect:/programs/my/"+service.copyProgram(id);}catch(com.fitpulse.exception.FitPulseException e){flash.addFlashAttribute("error",e.getMessage());return "redirect:/programs";}}
 @GetMapping("/programs/my") public String mine(Model m){m.addAttribute("programs",service.myPrograms());return "programs/my";}
 @GetMapping("/meal-plans/my") public String myMeals(Model m){m.addAttribute("mealPlans",service.myMeals());return "programs/my-meals";}
 @GetMapping("/programs/my/{id}") public String details(@PathVariable Long id,Model m){var program=service.ownedProgram(id);m.addAttribute("program",program);m.addAttribute("sessions",com.fitpulse.service.TrainingSessions.personalSessions(program));m.addAttribute("programItemForm",new ProgramItemForm());return "programs/details";}
 @PostMapping("/programs/my/{id}/items") public String addItem(@PathVariable Long id,@Valid @ModelAttribute ProgramItemForm form){service.addProgramItem(id,form);return "redirect:/programs/my/"+id;}
 @PostMapping("/programs/my/{id}/items/{itemId}") public String updateItem(@PathVariable Long id,@PathVariable Long itemId,@Valid @ModelAttribute ProgramItemForm form){service.updateProgramItem(id,itemId,form);return "redirect:/programs/my/"+id;}
 @PostMapping("/programs/my/{id}/items/{itemId}/delete") public String deleteItem(@PathVariable Long id,@PathVariable Long itemId){service.deleteProgramItem(id,itemId);return "redirect:/programs/my/"+id;}
 @PostMapping("/programs/my/{id}/calendar") public String calendar(@PathVariable Long id,@RequestParam int day,@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm") LocalDateTime startsAt){service.addDayToCalendar(id,day,startsAt);return "redirect:/calendar";}
 @PostMapping("/programs/my/{id}/settings") public String settings(@PathVariable Long id,@RequestParam String name,@RequestParam(defaultValue="false") boolean active){service.programSettings(id,name,active);return "redirect:/programs/my/"+id;}
 @GetMapping("/meal-plans")
 public String meals(@RequestParam(defaultValue="") String q,@RequestParam(required=false) com.fitpulse.model.enums.FitnessGoal goal,Model m){
     String search=q.strip();
     var library=service.mealLibrary();
     String needle=search.toLowerCase(java.util.Locale.ROOT);
     var filtered=library.stream().filter(p->goal==null || p.getGoal()==goal)
             .filter(p->needle.isEmpty() || (p.getDisplayName()+" "+p.getName()+" "+p.getDisplayDescription()+" "+p.getExampleFoods()).toLowerCase(java.util.Locale.ROOT).contains(needle)).toList();
     m.addAttribute("mealTemplates",filtered);m.addAttribute("q",search);m.addAttribute("goal",goal);
     var covers=new java.util.LinkedHashMap<Long,String>();for(var template:filtered)covers.put(template.getId(),menuReader.cover(template));m.addAttribute("mealCovers",covers);
     m.addAttribute("goals",library.stream().map(p->p.getGoal()).distinct().toList());
     m.addAttribute("total",library.size());return "programs/meals";
 }
 @GetMapping("/meal-plans/{id}")
 public String mealDetails(@PathVariable Long id,Model m){
     var meal=service.mealTemplate(id);
     if(!meal.isActive())throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
     m.addAttribute("meal",meal);m.addAttribute("weeklyMenu",menuReader.read(meal));
     m.addAttribute("mealSections",java.util.Arrays.stream(meal.getMeals().split("[;\\r\\n]+")).map(String::strip).filter(s->!s.isEmpty()).toList());
     return "programs/meal-details";
 }
 @GetMapping("/meal-plans/{id}/recipe") public String recipe(@PathVariable Long id,@RequestParam int day,@RequestParam int meal,Model m){var template=service.mealTemplate(id);if(!template.isActive())throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);var menu=menuReader.read(template);if(day<0||day>=menu.days().size()||meal<0||meal>=menu.days().get(day).meals().size())throw new com.fitpulse.exception.FitPulseException("Храненето не е намерено.");m.addAttribute("cookingMeal",menu.days().get(day).meals().get(meal));m.addAttribute("returnPath","/meal-plans/"+id+"#menu-day-"+day);m.addAttribute("personal",false);return "programs/recipe";}
 @PostMapping("/meal-plans/{id}/copy") public String copyMeal(@PathVariable Long id,org.springframework.web.servlet.mvc.support.RedirectAttributes flash){try{Long copiedId=service.copyMeal(id);flash.addFlashAttribute("success","Режимът е добавен в личните ти копия. Можеш да го редактираш по-долу.");return service.ownedMeal(copiedId).getWeeklyMenu()==null?"redirect:/meal-plans/my":"redirect:/meal-plans/my/"+copiedId;}catch(com.fitpulse.exception.FitPulseException e){flash.addFlashAttribute("error",e.getMessage());return "redirect:/meal-plans";}}
 @PostMapping("/meal-plans/my/{id}") public String editMeal(@PathVariable Long id,@RequestParam String name,@RequestParam String meals,@RequestParam String exampleFoods,org.springframework.web.servlet.mvc.support.RedirectAttributes flash){service.updateMeal(id,name,meals,exampleFoods);flash.addFlashAttribute("success","Личният ти хранителен режим е запазен.");return "redirect:/meal-plans/my";}
}
