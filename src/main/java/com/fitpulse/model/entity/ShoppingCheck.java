package com.fitpulse.model.entity;
import jakarta.persistence.*; import java.time.*; import java.math.BigDecimal;
@Entity @Table(name="shopping_checks") public class ShoppingCheck {
@Column(nullable=false,precision=10,scale=2) private BigDecimal grams=BigDecimal.ZERO;
public BigDecimal getGrams(){return grams;}public void setGrams(BigDecimal v){grams=v;}
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; public Long getId(){return id;}
@ManyToOne(optional=false) private UserMealPlan plan;
public UserMealPlan getPlan(){return plan;} public void setPlan(UserMealPlan v){plan=v;}
@Column(nullable=false,length=80) private String foodCode;
public String getFoodCode(){return foodCode;} public void setFoodCode(String v){foodCode=v;}
@Column(nullable=false,length=64) private String scope;
public String getScope(){return scope;} public void setScope(String v){scope=v;}
@Column(nullable=false) private boolean checked=false;
public boolean isChecked(){return checked;} public void setChecked(boolean v){checked=v;}

}
