package com.fitpulse.controller;

import com.fitpulse.service.WellnessService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ReminderController {
    private final WellnessService wellness;
    public ReminderController(WellnessService wellness) { this.wellness = wellness; }
    public record Reminder(Long id, String title, LocalDateTime startsAt) {}
    @GetMapping("/api/reminders")
    public ResponseEntity<List<Reminder>> reminders() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(wellness.reminders().stream()
                .map(e -> new Reminder(e.getId(), e.getTitle(), e.getStartsAt())).toList());
    }
}
