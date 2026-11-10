package com.fitpulse.model.entity;
import jakarta.persistence.*;
@Entity @Table(name="cities") public class City {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=100) private String name; @Column(nullable=false,unique=true,length=120) private String slug; @Column(nullable=false) private boolean active=true;
 public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;} public String getSlug(){return slug;} public void setSlug(String v){slug=v;} public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
