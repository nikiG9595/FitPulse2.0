package com.fitpulse.controller;

import com.fitpulse.service.ExerciseLibraryService;
import com.fitpulse.model.entity.Exercise;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller @RequestMapping("/exercises")
public class ExerciseLibraryController {
    private final ExerciseLibraryService library;
    public ExerciseLibraryController(ExerciseLibraryService library){this.library=library;}
    @GetMapping public String list(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String muscle,
            @RequestParam(defaultValue="") String difficulty,@RequestParam(defaultValue="") String equipment,
            @RequestParam(defaultValue="") String location,@RequestParam(defaultValue="0") int page,Model m){
        var all=library.all();m.addAttribute("exercises",library.search(q,muscle,difficulty,equipment,location,page));
        m.addAttribute("muscles",all.stream().map(Exercise::getMuscle).distinct().sorted().toList());m.addAttribute("difficulties",List.of("Начинаещ","Средно напреднал","Напреднал"));
        m.addAttribute("equipmentOptions",all.stream().map(Exercise::getEquipment).distinct().sorted().toList());
        m.addAttribute("q",q);m.addAttribute("muscle",muscle);m.addAttribute("difficulty",difficulty);m.addAttribute("equipment",equipment);m.addAttribute("location",location);return "exercises/list";
    }
    public record Choice(Long id,String code,String name,String muscle,String equipment){}
    @GetMapping("/search") @ResponseBody public List<Choice> search(@RequestParam(defaultValue="") String q){return library.search(q,null,null,null,null,0).getContent().stream().map(e->new Choice(e.getId(),e.getCode(),e.getName(),e.getMuscle(),e.getEquipment())).toList();}
    @GetMapping("/{code}") public String details(@PathVariable String code,Model m){var exercise=library.get(code);m.addAttribute("exercise",exercise);m.addAttribute("primaryNames",com.fitpulse.service.AnatomyMuscles.names(exercise.getPrimaryZones()));m.addAttribute("secondaryNames",com.fitpulse.service.AnatomyMuscles.names(exercise.getSecondaryZones()));m.addAttribute("steps",exercise.getSteps().split("\\|"));m.addAttribute("alternatives",Arrays.stream(exercise.getAlternatives().split(",")).filter(s->!s.isBlank()).map(library::get).toList());return "exercises/details";}
}
