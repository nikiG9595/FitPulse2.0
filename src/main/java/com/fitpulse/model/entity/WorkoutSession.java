package com.fitpulse.model.entity;
import jakarta.persistence.*;import java.time.*;import java.math.BigDecimal;
@Entity @Table(name="workout_sessions") public class WorkoutSession {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;public Long getId(){return id;}
@ManyToOne(optional=false) private User user;
public User getUser(){return user;}public void setUser(User v){user=v;}
@ManyToOne private UserProgram program;
public UserProgram getProgram(){return program;}public void setProgram(UserProgram v){program=v;}
@Column(nullable=false) private int dayNumber;
public int getDayNumber(){return dayNumber;}public void setDayNumber(int v){dayNumber=v;}
@Column(nullable=false,length=150) private String title;
public String getTitle(){return title;}public void setTitle(String v){title=v;}
@Column(nullable=false,length=20) private String status="DRAFT";
public String getStatus(){return status;}public void setStatus(String v){status=v;}
@Column(unique=true,length=120) private String activeKey;
public String getActiveKey(){return activeKey;}public void setActiveKey(String v){activeKey=v;}
@Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
public LocalDateTime getCreatedAt(){return createdAt;}public void setCreatedAt(LocalDateTime v){createdAt=v;}
@Column(nullable=false) private LocalDateTime updatedAt=LocalDateTime.now();
public LocalDateTime getUpdatedAt(){return updatedAt;}public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
@OneToOne @JoinColumn(unique=true) private WorkoutLog finishedLog;
public WorkoutLog getFinishedLog(){return finishedLog;}public void setFinishedLog(WorkoutLog v){finishedLog=v;}
@Column(nullable=false) private int timerRemaining=90;
public int getTimerRemaining(){return timerRemaining;}public void setTimerRemaining(int v){timerRemaining=v;}
 private Long timerDeadline;
public Long getTimerDeadline(){return timerDeadline;}public void setTimerDeadline(Long v){timerDeadline=v;}
}
