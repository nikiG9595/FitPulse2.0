package com.fitpulse.model.entity;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="foods") public class Food {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; public Long getId(){return id;}
@Column(nullable=false,unique=true,length=80) private String code;
public String getCode(){return code;} public void setCode(String v){code=v;}
@Column(nullable=false,length=150) private String name;
public String getName(){return name;} public void setName(String v){name=v;}
@Column(nullable=false,length=120) private String state;
public String getState(){return state;} public void setState(String v){state=v;}
@Column(nullable=false,length=80) private String category;
public String getCategory(){return category;} public void setCategory(String v){category=v;}
@Column(nullable=false,precision=8,scale=2) private BigDecimal protein;
public BigDecimal getProtein(){return protein;} public void setProtein(BigDecimal v){protein=v;}
@Column(nullable=false,precision=8,scale=2) private BigDecimal fat;
public BigDecimal getFat(){return fat;} public void setFat(BigDecimal v){fat=v;}
@Column(nullable=false,precision=8,scale=2) private BigDecimal carbs;
public BigDecimal getCarbs(){return carbs;} public void setCarbs(BigDecimal v){carbs=v;}
public long getCalories(){return protein.multiply(BigDecimal.valueOf(4)).add(fat.multiply(BigDecimal.valueOf(9))).add(carbs.multiply(BigDecimal.valueOf(4))).setScale(0,java.math.RoundingMode.HALF_UP).longValue();}
}
