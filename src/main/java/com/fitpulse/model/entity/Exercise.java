package com.fitpulse.model.entity;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="exercises") public class Exercise {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(length=500) private String primaryZones;
 @Column(length=500) private String secondaryZones;
 public String getPrimaryZones(){return primaryZones;}public void setPrimaryZones(String v){primaryZones=v;}
 public String getSecondaryZones(){return secondaryZones;}public void setSecondaryZones(String v){secondaryZones=v;}
 public Long getId(){return id;}
@Column(nullable=false,unique=true,length=80) private String code;
public String getCode(){return code;} public void setCode(String v){code=v;}
@Column(nullable=false,length=150) private String name;
public String getName(){return name;} public void setName(String v){name=v;}
@Column(nullable=false,length=50) private String muscle;
public String getMuscle(){return muscle;} public void setMuscle(String v){muscle=v;}
@Column(nullable=false,length=200) private String secondaryMuscles;
public String getSecondaryMuscles(){return secondaryMuscles;} public void setSecondaryMuscles(String v){secondaryMuscles=v;}
@Column(nullable=false,length=40) private String difficulty;
public String getDifficulty(){return difficulty;} public void setDifficulty(String v){difficulty=v;}
@Column(nullable=false,length=100) private String equipment;
public String getEquipment(){return equipment;} public void setEquipment(String v){equipment=v;}
@Column(nullable=false,length=30) private String location;
public String getLocation(){return location;} public void setLocation(String v){location=v;}
@Column(nullable=false,length=1000) private String startPosition;
public String getStartPosition(){return startPosition;} public void setStartPosition(String v){startPosition=v;}
@Column(nullable=false,length=2000) private String steps;
public String getSteps(){return steps;} public void setSteps(String v){steps=v;}
@Column(nullable=false,length=500) private String breathing;
public String getBreathing(){return breathing;} public void setBreathing(String v){breathing=v;}
@Column(nullable=false,length=800) private String posture;
public String getPosture(){return posture;} public void setPosture(String v){posture=v;}
@Column(nullable=false,length=800) private String mistakes;
public String getMistakes(){return mistakes;} public void setMistakes(String v){mistakes=v;}
@Column(nullable=false,length=800) private String safety;
public String getSafety(){return safety;} public void setSafety(String v){safety=v;}
@Column(nullable=false,length=500) private String easier;
public String getEasier(){return easier;} public void setEasier(String v){easier=v;}
@Column(nullable=false,length=500) private String harder;
public String getHarder(){return harder;} public void setHarder(String v){harder=v;}
@Column(nullable=false,length=800) private String aliases;
public String getAliases(){return aliases;} public void setAliases(String v){aliases=v;}
@Column(nullable=false,length=500) private String alternatives;
public String getAlternatives(){return alternatives;} public void setAlternatives(String v){alternatives=v;}

}
