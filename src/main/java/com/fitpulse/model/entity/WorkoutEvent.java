package com.fitpulse.model.entity;
import com.fitpulse.model.enums.WorkoutStatus; import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="workout_events") public class WorkoutEvent { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne private UserProgram program;
 @ManyToOne private UserMealPlan mealPlan;
 @Column(nullable=false) private int reminderMinutes=30;
 @Column(nullable=false) private boolean reminderDismissed;
 public UserProgram getProgram(){return program;} public void setProgram(UserProgram v){program=v;}
 public UserMealPlan getMealPlan(){return mealPlan;} public void setMealPlan(UserMealPlan v){mealPlan=v;}
 public int getReminderMinutes(){return reminderMinutes;} public void setReminderMinutes(int v){reminderMinutes=v;}
 public boolean isReminderDismissed(){return reminderDismissed;} public void setReminderDismissed(boolean v){reminderDismissed=v;}
 @ManyToOne(optional=false) private User user; @ManyToOne private Gym gym; @Column(nullable=false,length=150) private String title; @Column(length=1500) private String notes; @Column(nullable=false) private LocalDateTime startsAt; @Column(nullable=false) private int durationMinutes; @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private WorkoutStatus status=WorkoutStatus.PLANNED; @Column(nullable=false) private LocalDateTime createdAt; @PrePersist void create(){createdAt=LocalDateTime.now();} public Long getId(){return id;} public User getUser(){return user;} public void setUser(User v){user=v;} public Gym getGym(){return gym;} public void setGym(Gym v){gym=v;} public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getNotes(){return notes;} public void setNotes(String v){notes=v;} public LocalDateTime getStartsAt(){return startsAt;} public void setStartsAt(LocalDateTime v){startsAt=v;} public int getDurationMinutes(){return durationMinutes;} public void setDurationMinutes(int v){durationMinutes=v;} public WorkoutStatus getStatus(){return status;} public void setStatus(WorkoutStatus v){status=v;} }
