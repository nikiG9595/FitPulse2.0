package com.fitpulse.model.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
public class WorkoutLogForm {
    @NotBlank @Size(max=150) public String title;
    @NotNull @PastOrPresent @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm") public LocalDateTime performedAt=LocalDateTime.now().withSecond(0).withNano(0);
    @Size(max=1500) public String notes;
    public Long eventId;
}
