package com.fitpulse.model.entity;
import jakarta.persistence.*;
@Entity @Table(name="program_template_items") public class ProgramTemplateItem { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne private Exercise exercise; public Exercise getExercise(){return exercise;} public void setExercise(Exercise v){exercise=v;} @ManyToOne(optional=false) private ProgramTemplate template; private int dayNumber; @Column(nullable=false,length=150) private String exerciseName; private int setsCount; @Column(nullable=false,length=50) private String repetitions; private int sortOrder;
 @Column(nullable=false,length=100) private String sessionTitle="";
 @Column(nullable=false) private int restSeconds=90;
 @Column(nullable=false,length=500) private String instructions="Движи се контролирано и остави 2 повторения в резерв.";
 public String getSessionTitle(){return sessionTitle;} public void setSessionTitle(String v){sessionTitle=v==null?"":v;}
 public int getRestSeconds(){return restSeconds;} public void setRestSeconds(int v){restSeconds=v;}
 public String getInstructions(){return instructions;} public void setInstructions(String v){instructions=v==null?"":v;}
 public Long getId(){return id;} public ProgramTemplate getTemplate(){return template;} public void setTemplate(ProgramTemplate v){template=v;} public int getDayNumber(){return dayNumber;} public void setDayNumber(int v){dayNumber=v;} public String getExerciseName(){return exerciseName;} public void setExerciseName(String v){exerciseName=v;} public int getSetsCount(){return setsCount;} public void setSetsCount(int v){setsCount=v;} public String getRepetitions(){return repetitions;} public void setRepetitions(String v){repetitions=v;} public int getSortOrder(){return sortOrder;} public void setSortOrder(int v){sortOrder=v;} }
