package com.fitpulse.model.dto; import jakarta.validation.constraints.*; public class ProgramItemForm { @Min(1) @Max(7) private int dayNumber; @NotBlank @Size(max=150) private String exerciseName; @Min(1) @Max(20) private int setsCount; @NotBlank @Size(max=50) private String repetitions;
 @Size(max=100) private String sessionTitle;
 @Min(15) @Max(600) private int restSeconds=90;
 @Size(max=500) private String instructions;
 public String getSessionTitle(){return sessionTitle;} public void setSessionTitle(String v){sessionTitle=v;}
 public int getRestSeconds(){return restSeconds;} public void setRestSeconds(int v){restSeconds=v;}
 public String getInstructions(){return instructions;} public void setInstructions(String v){instructions=v;}
 public int getDayNumber(){return dayNumber;} public void setDayNumber(int v){dayNumber=v;} public String getExerciseName(){return exerciseName;} public void setExerciseName(String v){exerciseName=v;} public int getSetsCount(){return setsCount;} public void setSetsCount(int v){setsCount=v;} public String getRepetitions(){return repetitions;} public void setRepetitions(String v){repetitions=v;} }
