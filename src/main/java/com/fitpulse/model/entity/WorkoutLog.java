package com.fitpulse.model.entity;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
@Entity @Table(name="workout_logs")
public class WorkoutLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    public Long getId() { return id; }
    @ManyToOne(optional=false) private User user;
    public User getUser() { return user; }
    public void setUser(User value) { user = value; }
    @ManyToOne private WorkoutEvent event;
    public WorkoutEvent getEvent() { return event; }
    public void setEvent(WorkoutEvent value) { event = value; }
    @Column(nullable=false,length=150) private String title;
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    @Column(nullable=false) private LocalDateTime performedAt;
    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime value) { performedAt = value; }
    @Column(length=1500) private String notes;
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value; }
}
