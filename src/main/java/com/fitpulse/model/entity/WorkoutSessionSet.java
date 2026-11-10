package com.fitpulse.model.entity;
import jakarta.persistence.*;import java.time.*;import java.math.BigDecimal;
@Entity @Table(name="workout_session_sets") public class WorkoutSessionSet {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;public Long getId(){return id;}
@ManyToOne(optional=false) private WorkoutSession session;
public WorkoutSession getSession(){return session;}public void setSession(WorkoutSession v){session=v;}
@ManyToOne private Exercise exercise;
public Exercise getExercise(){return exercise;}public void setExercise(Exercise v){exercise=v;}
@Column(nullable=false,length=150) private String exerciseName;
public String getExerciseName(){return exerciseName;}public void setExerciseName(String v){exerciseName=v;}
@Column(nullable=false) private int exerciseOrder;
public int getExerciseOrder(){return exerciseOrder;}public void setExerciseOrder(int v){exerciseOrder=v;}
@Column(nullable=false) private int setNumber;
public int getSetNumber(){return setNumber;}public void setSetNumber(int v){setNumber=v;}
@Column(nullable=false,length=50) private String plannedRepetitions;
public String getPlannedRepetitions(){return plannedRepetitions;}public void setPlannedRepetitions(String v){plannedRepetitions=v;}
@Column(nullable=false) private int restSeconds;
public int getRestSeconds(){return restSeconds;}public void setRestSeconds(int v){restSeconds=v;}
 private Integer repetitions;
public Integer getRepetitions(){return repetitions;}public void setRepetitions(Integer v){repetitions=v;}
@Column(precision=7,scale=2) private BigDecimal weight;
public BigDecimal getWeight(){return weight;}public void setWeight(BigDecimal v){weight=v;}
@Column(nullable=false) private boolean completed=false;
public boolean isCompleted(){return completed;}public void setCompleted(boolean v){completed=v;}
}
