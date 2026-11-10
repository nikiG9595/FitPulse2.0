package com.fitpulse.model.entity;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="body_measurements") public class BodyMeasurement {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; public Long getId(){return id;}
@ManyToOne(optional=false) private User user;
public User getUser(){return user;} public void setUser(User v){user=v;}
@Column(nullable=false,length=20) private String type;
public String getType(){return type;} public void setType(String v){type=v;}
@Column(nullable=false) private LocalDate recordedAt;
public LocalDate getRecordedAt(){return recordedAt;} public void setRecordedAt(LocalDate v){recordedAt=v;}
@Column(name="measurement_value",nullable=false,precision=6,scale=2) private BigDecimal value;
public BigDecimal getValue(){return value;} public void setValue(BigDecimal v){value=v;}

}
