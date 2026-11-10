package com.fitpulse.controller;
import com.fitpulse.model.dto.*;
import com.fitpulse.model.entity.WorkoutLog;
import com.fitpulse.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller @RequestMapping("/journal")
public class WorkoutJournalController {
    private final WorkoutJournalService journal; private final WellnessService wellness;
    public WorkoutJournalController(WorkoutJournalService j,WellnessService w) { journal=j;wellness=w; }
    @InitBinder({"workoutLogForm","workoutSetForm"}) void bind(WebDataBinder b) { b.initDirectFieldAccess(); }
    @GetMapping public String list(Model m) { m.addAttribute("logs",journal.history()); return "journal/list"; }
    @GetMapping("/create") public String create(@RequestParam(required=false) Long eventId,Model m) {
        WorkoutLogForm f=new WorkoutLogForm();
        if(eventId!=null) { var e=wellness.ownedEvent(eventId); f.eventId=e.getId(); f.title=e.getTitle(); f.notes=e.getNotes(); }
        prepare(m,f,null); return "journal/form";
    }
    @GetMapping("/{id}/edit") public String edit(@PathVariable Long id,Model m) {
        WorkoutLog l=journal.owned(id); WorkoutLogForm f=new WorkoutLogForm(); f.title=l.getTitle(); f.notes=l.getNotes(); f.performedAt=l.getPerformedAt(); f.eventId=l.getEvent()==null?null:l.getEvent().getId(); prepare(m,f,id); return "journal/form";
    }
    @PostMapping({"/create","/{id}/edit"}) public String save(@PathVariable(required=false) Long id,@Valid @ModelAttribute WorkoutLogForm workoutLogForm,BindingResult errors,Model m) {
        if(errors.hasErrors()) { prepare(m,workoutLogForm,id); return "journal/form"; } return "redirect:/journal/"+journal.save(id,workoutLogForm);
    }
    @GetMapping("/{id}") public String details(@PathVariable Long id,Model m) { detailsModel(id,m); m.addAttribute("workoutSetForm",new WorkoutSetForm()); return "journal/details"; }
    @PostMapping("/{id}/sets") public String saveSet(@PathVariable Long id,@RequestParam(required=false) Long setId,@Valid @ModelAttribute WorkoutSetForm workoutSetForm,BindingResult errors,Model m) {
        if(errors.hasErrors()) { detailsModel(id,m); return "journal/details"; } journal.saveSet(id,setId,workoutSetForm); return "redirect:/journal/"+id;
    }
    @PostMapping("/{id}/sets/{setId}/delete") public String deleteSet(@PathVariable Long id,@PathVariable Long setId) { journal.deleteSet(id,setId); return "redirect:/journal/"+id; }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id) { journal.delete(id); return "redirect:/journal"; }
    private void prepare(Model m,WorkoutLogForm f,Long id) { m.addAttribute("workoutLogForm",f);m.addAttribute("logId",id);m.addAttribute("events",wellness.calendar()); }
    private void detailsModel(Long id,Model m) { m.addAttribute("log",journal.owned(id));m.addAttribute("sets",journal.sets(id)); }
}
