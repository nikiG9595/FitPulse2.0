package com.fitpulse.controller;

import com.fitpulse.service.DailyMealIntakeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/meal-plans/intake")
public class DailyMealIntakeController {
    private final DailyMealIntakeService intake;

    public DailyMealIntakeController(DailyMealIntakeService intake) {
        this.intake = intake;
    }

    @GetMapping
    public String today(Model model, jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("intake", intake.today());
        return "fragments/daily-intake :: card";
    }

    @PostMapping("/plan")
    public ResponseEntity<Void> select(@RequestParam long planId) {
        return result(intake.selectPlan(planId));
    }

    @PostMapping("/meal")
    public ResponseEntity<Void> mark(@RequestParam long planId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int index, @RequestParam String revision,
            @RequestParam(defaultValue = "false") boolean eaten) {
        return result(intake.mark(planId, date, index, revision, eaten));
    }

    private ResponseEntity<Void> result(boolean saved) {
        return ResponseEntity.status(saved ? HttpStatus.NO_CONTENT : HttpStatus.CONFLICT).build();
    }
}
