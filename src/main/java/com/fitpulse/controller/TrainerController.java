package com.fitpulse.controller;

import com.fitpulse.model.dto.TrainerForm;
import com.fitpulse.model.entity.Trainer;
import com.fitpulse.repository.TrainerRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class TrainerController {
    private final TrainerRepository trainers;
    public TrainerController(TrainerRepository trainers) { this.trainers=trainers; }
    @InitBinder("trainerForm") void bind(WebDataBinder binder) { binder.initDirectFieldAccess(); }
    @GetMapping("/trainers") public String list(Model m) { m.addAttribute("trainers",trainers.findAllByPublishedTrueOrderByName()); return "trainers/list"; }
    @GetMapping("/trainers/{id}") public String profile(@PathVariable Long id, Model m) {
        m.addAttribute("trainer",trainers.findByIdAndPublishedTrue(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)));
        return "trainers/profile";
    }
    @GetMapping("/admin/trainers") public String admin(Model m) { m.addAttribute("trainers",trainers.findAllByOrderByName()); return "admin/trainers"; }
    @GetMapping("/admin/trainers/create") public String create(Model m) { m.addAttribute("trainerForm",new TrainerForm()); return "admin/trainer-form"; }
    @GetMapping("/admin/trainers/{id}/edit") public String edit(@PathVariable Long id,Model m) {
        Trainer t=trainer(id); TrainerForm f=new TrainerForm(); f.name=t.getName(); f.age=t.getAge(); f.country=t.getCountry(); f.experienceYears=t.getExperienceYears(); f.bio=t.getBio(); f.photoUrl=t.getPhotoUrl(); f.published=t.isPublished();
        m.addAttribute("trainerForm",f); m.addAttribute("trainerId",id); return "admin/trainer-form";
    }
    @PostMapping({"/admin/trainers/create","/admin/trainers/{id}/edit"})
    public String save(@PathVariable(required=false) Long id,@Valid @ModelAttribute TrainerForm trainerForm,BindingResult errors,Model m) {
        if(trainerForm.experienceYears>trainerForm.age-16) errors.rejectValue("experienceYears","experience","Опитът не съответства на възрастта.");
        if(errors.hasErrors()) { m.addAttribute("trainerId",id); return "admin/trainer-form"; }
        Trainer t=id==null?new Trainer():trainer(id); t.setName(trainerForm.name.trim()); t.setAge(trainerForm.age); t.setCountry(trainerForm.country.trim()); t.setExperienceYears(trainerForm.experienceYears); t.setBio(trainerForm.bio); t.setPhotoUrl(trainerForm.photoUrl); t.setPublished(trainerForm.published); trainers.save(t); return "redirect:/admin/trainers";
    }
    @PostMapping("/admin/trainers/{id}/delete") public String delete(@PathVariable Long id) { trainers.delete(trainer(id)); return "redirect:/admin/trainers"; }
    private Trainer trainer(Long id) { return trainers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)); }
}
