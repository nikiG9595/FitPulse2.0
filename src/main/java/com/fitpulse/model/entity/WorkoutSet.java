package com.fitpulse.model.entity;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
@Entity @Table(name="workout_sets")
public class WorkoutSet {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne private Exercise exercise; public Exercise getExercise(){return exercise;} public void setExercise(Exercise v){exercise=v;}
    public Long getId() { return id; }
    @ManyToOne(optional=false) private WorkoutLog workoutLog;
    public WorkoutLog getWorkoutLog() { return workoutLog; }
    public void setWorkoutLog(WorkoutLog value) { workoutLog = value; }
    @Column(nullable=false,length=150) private String exerciseName;
    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String value) { exerciseName = value; }
    @Column(nullable=false) private int setNumber;
    public int getSetNumber() { return setNumber; }
    public void setSetNumber(int value) { setNumber = value; }
    @Column(nullable=false) private int repetitions;
    public int getRepetitions() { return repetitions; }
    public void setRepetitions(int value) { repetitions = value; }
    @Column(nullable=false,precision=7,scale=2) private BigDecimal weight;
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal value) { weight = value; }
}
