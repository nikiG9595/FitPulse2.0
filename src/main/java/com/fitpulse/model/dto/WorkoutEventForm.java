package com.fitpulse.model.dto; import jakarta.validation.constraints.*; import org.springframework.format.annotation.DateTimeFormat; import java.time.LocalDateTime;
public class WorkoutEventForm { @NotBlank @Size(max=150) private String title; @Size(max=1500) private String notes; @NotNull @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm") private LocalDateTime startsAt; @Min(10) @Max(480) private int durationMinutes=60; private Long gymId;
 @Min(0) @Max(10080) private int reminderMinutes=30;
 private Long programId; private Long mealPlanId;
 public int getReminderMinutes(){return reminderMinutes;} public void setReminderMinutes(int v){reminderMinutes=v;}
 public Long getProgramId(){return programId;} public void setProgramId(Long v){programId=v;}
 public Long getMealPlanId(){return mealPlanId;} public void setMealPlanId(Long v){mealPlanId=v;}
 public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getNotes(){return notes;} public void setNotes(String v){notes=v;} public LocalDateTime getStartsAt(){return startsAt;} public void setStartsAt(LocalDateTime v){startsAt=v;} public int getDurationMinutes(){return durationMinutes;} public void setDurationMinutes(int v){durationMinutes=v;} public Long getGymId(){return gymId;} public void setGymId(Long v){gymId=v;} }
