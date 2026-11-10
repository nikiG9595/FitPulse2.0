package com.fitpulse.model.entity;
import com.fitpulse.model.enums.FitnessGoal; import jakarta.persistence.*;
@Entity @Table(name="meal_plan_templates") public class MealPlanTemplate { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Lob @Column(columnDefinition="LONGTEXT") private String originalWeeklyMenu; public String getOriginalWeeklyMenu(){return originalWeeklyMenu;}public void setOriginalWeeklyMenu(String v){originalWeeklyMenu=v;}
 @ManyToOne private Trainer author;
 public Trainer getAuthor(){return author;} public void setAuthor(Trainer v){author=v;}
 @Column(nullable=false,length=150) private String name; @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private FitnessGoal goal; @Column(nullable=false,length=2000) private String description; @Lob @Column(nullable=false,columnDefinition="LONGTEXT") private String meals; @Column(nullable=false,length=3000) private String exampleFoods=""; @Column(nullable=false,length=1000) private String disclaimer; private boolean active=true;
 @Column(unique=true,length=40) private String demoCode;
 @Lob @Column(columnDefinition="LONGTEXT") private String weeklyMenu;
 public String getDemoCode(){return demoCode;} public void setDemoCode(String v){demoCode=v;}
 public String getWeeklyMenu(){return weeklyMenu;} public void setWeeklyMenu(String v){weeklyMenu=v;}
 public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;} public FitnessGoal getGoal(){return goal;} public void setGoal(FitnessGoal v){goal=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getMeals(){return meals;} public void setMeals(String v){meals=v;} public String getExampleFoods(){return exampleFoods;} public void setExampleFoods(String v){exampleFoods=v;} public String getDisclaimer(){return disclaimer;} public void setDisclaimer(String v){disclaimer=v;} public boolean isActive(){return active;} public void setActive(boolean v){active=v;} }
