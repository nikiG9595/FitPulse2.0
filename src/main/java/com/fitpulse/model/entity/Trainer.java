package com.fitpulse.model.entity;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
@Entity @Table(name="trainers")
public class Trainer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    public Long getId() { return id; }
    @Column(nullable=false,length=150) private String name;
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    @Column(nullable=false) private int age;
    public int getAge() { return age; }
    public void setAge(int value) { age = value; }
    @Column(nullable=false,length=100) private String country;
    public String getCountry() { return country; }
    public void setCountry(String value) { country = value; }
    @Column(nullable=false) private int experienceYears;
    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int value) { experienceYears = value; }
    @Column(nullable=false,length=2000) private String bio;
    public String getBio() { return bio; }
    public void setBio(String value) { bio = value; }
    @Column(nullable=false,length=1000) private String photoUrl;
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String value) { photoUrl = value; }
    @Column(nullable=false) private boolean published = false;
    public boolean isPublished() { return published; }
    public void setPublished(boolean value) { published = value; }
}
