package com.fitpulse.controller;
import com.fitpulse.model.dto.*; import com.fitpulse.model.entity.Gym; import com.fitpulse.service.GymDirectoryService; import jakarta.validation.Valid; import org.springframework.security.core.Authentication; import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.validation.BindingResult; import org.springframework.web.bind.annotation.*; import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller public class GymDirectoryController { private final GymDirectoryService service; public GymDirectoryController(GymDirectoryService s){service=s;}
 @GetMapping("/gyms") public String list(@RequestParam(required=false)String city,@RequestParam(required=false)String q,@RequestParam(defaultValue="0")int page,Model m){var result=service.search(city,q,page);m.addAttribute("gyms",result);var covers=new java.util.LinkedHashMap<Long,com.fitpulse.model.entity.GymPhoto>();for(var gym:result.getContent())gym.getPhotos().stream().filter(p->!com.fitpulse.service.GymContent.text(p.getImageUrl()).isBlank()&&com.fitpulse.service.GymContent.imageUrl(p.getImageUrl())).findFirst().ifPresent(p->covers.put(gym.getId(),p));m.addAttribute("covers",covers);m.addAttribute("cities",service.activeCities());m.addAttribute("city",city);m.addAttribute("q",q);return "gyms/list";}
 @GetMapping("/gyms/{slug}") public String details(@PathVariable String slug,Model m,Authentication a){return detailsModel(service.published(slug),m,a);}
 private String detailsModel(Gym g,Model m,Authentication a){
  boolean member=a!=null&&a.isAuthenticated()&&!(a instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
  var own=member?service.ownReview(g):null;m.addAttribute("gym",g);m.addAttribute("reviews",service.reviews(g));m.addAttribute("averageRating",service.rating(g));m.addAttribute("ownReview",own);
  m.addAttribute("gymPhotos",g.getPhotos().stream().filter(p->!com.fitpulse.service.GymContent.text(p.getImageUrl()).isBlank()&&com.fitpulse.service.GymContent.imageUrl(p.getImageUrl())).toList());m.addAttribute("websiteUsable",!com.fitpulse.service.GymContent.text(g.getWebsiteUrl()).isBlank()&&com.fitpulse.service.GymContent.webUrl(g.getWebsiteUrl()));
  if(!m.containsAttribute("reviewForm")){ReviewForm form=new ReviewForm();if(own!=null){form.setRating(own.getRating());form.setComment(own.getComment());}m.addAttribute("reviewForm",form);}
  m.addAttribute("favorite",member&&service.favorite(g));return "gyms/details";
 }
 @PostMapping("/gyms/{id}/favorite") public String favorite(@PathVariable Long id){var gym=service.publishedById(id);service.toggleFavorite(id);return "redirect:/gyms/"+gym.getSlug();}
 @PostMapping("/gyms/{id}/review") public String review(@PathVariable Long id,@Valid @ModelAttribute ReviewForm reviewForm,BindingResult errors,Model model,Authentication authentication,RedirectAttributes ra){
  var gym=service.publishedById(id);if(errors.hasErrors())return detailsModel(gym,model,authentication);
  var saved=service.review(id,reviewForm);ra.addFlashAttribute("success",saved.getStatus()==com.fitpulse.model.enums.ContentStatus.PENDING?"Редактираното ревю е запазено и очаква преглед от администратор.":"Оценката и ревюто са запазени.");return "redirect:/gyms/"+gym.getSlug()+"#reviews";
 }
 @PostMapping("/reviews/{id}/report") public String report(@PathVariable Long id,@RequestParam String reason,RedirectAttributes ra){String slug=service.report(id,reason);ra.addFlashAttribute("success","Сигналът е изпратен за преглед от администратор.");return "redirect:/gyms/"+slug+"#reviews";}
 @GetMapping("/suggest-gym") public String suggest(Model m){if(!m.containsAttribute("gymSuggestionForm"))m.addAttribute("gymSuggestionForm",new GymSuggestionForm());return "gyms/suggest";}
 @PostMapping("/suggest-gym") public String submit(@Valid @ModelAttribute GymSuggestionForm gymSuggestionForm,BindingResult errors,RedirectAttributes ra){if(errors.hasErrors())return "gyms/suggest";service.suggest(gymSuggestionForm);ra.addFlashAttribute("success","Предложението е изпратено за одобрение.");return "redirect:/suggest-gym";}
}
