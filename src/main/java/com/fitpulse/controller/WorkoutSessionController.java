package com.fitpulse.controller;

import com.fitpulse.service.WorkoutSessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.math.BigDecimal;
import java.util.Map;

@Controller
public class WorkoutSessionController {
    private final WorkoutSessionService sessions;
    public WorkoutSessionController(WorkoutSessionService sessions){this.sessions=sessions;}
    @PostMapping("/programs/my/{programId}/start") public String start(@PathVariable Long programId,@RequestParam int day){return "redirect:/training-sessions/"+sessions.start(programId,day);}
    @GetMapping("/training-sessions") public String list(Model m){m.addAttribute("sessions",sessions.history());return "sessions/list";}
    @GetMapping("/training-sessions/{id}") public String details(@PathVariable Long id,Model m){var session=sessions.owned(id);m.addAttribute("session",session);m.addAttribute("blocks",sessions.blocks(id));m.addAttribute("timer",sessions.timerState(session));return "sessions/current";}
    @PostMapping("/training-sessions/{id}/sets/{setId}") public ResponseEntity<?> save(@PathVariable Long id,@PathVariable Long setId,@RequestParam(required=false) Integer repetitions,@RequestParam(required=false)BigDecimal weight,@RequestParam(defaultValue="false") boolean completed,jakarta.servlet.http.HttpServletRequest request){sessions.saveSet(id,setId,repetitions,weight,completed);if(java.util.Optional.ofNullable(request.getHeader("Accept")).orElse("").contains("application/json"))return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("saved",true));return ResponseEntity.status(303).location(java.net.URI.create(request.getContextPath()+"/training-sessions/"+id)).build();}
    @PostMapping("/training-sessions/{id}/timer") public ResponseEntity<WorkoutSessionService.Timer> timer(@PathVariable Long id,@RequestParam String action,@RequestParam(defaultValue="90")int seconds){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(sessions.timer(id,action,seconds));}
    @PostMapping("/training-sessions/{id}/finish") public String finish(@PathVariable Long id){return "redirect:/journal/"+sessions.finish(id);}
    @PostMapping("/training-sessions/{id}/cancel") public String cancel(@PathVariable Long id){sessions.cancel(id);return "redirect:/training-sessions";}
}
