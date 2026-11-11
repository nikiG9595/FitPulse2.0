package com.fitpulse.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Request-only inputs: personal measurements are not stored in a profile or public template. */
public class MealPersonalizationForm {
    @NotBlank(message="Изберете пол.") @Pattern(regexp="MALE|FEMALE",message="Изберете Мъж или Жена.")
    private String sex;
    public String getSex(){return sex;}
    public void setSex(String value){sex=value;}
    @NotNull(message="Въведете възраст.") @Min(value=18,message="Функцията е само за пълнолетни.") @Max(value=100,message="Поддържана възраст: 18–100 години.")
    private Integer age;
    public Integer getAge(){return age;}
    public void setAge(Integer value){age=value;}
    @NotNull(message="Въведете ръст.") @DecimalMin(value="120",message="Поддържан ръст: 120–230 см.") @DecimalMax(value="230",message="Поддържан ръст: 120–230 см.") @Digits(integer=3,fraction=2,message="Ръстът допуска до два десетични знака.")
    private BigDecimal height;
    public BigDecimal getHeight(){return height;}
    public void setHeight(BigDecimal value){height=value;}
    @NotNull(message="Въведете тегло.") @DecimalMin(value="35",message="Поддържано тегло: 35–300 кг.") @DecimalMax(value="300",message="Поддържано тегло: 35–300 кг.") @Digits(integer=3,fraction=2,message="Теглото допуска до два десетични знака.")
    private BigDecimal weight;
    public BigDecimal getWeight(){return weight;}
    public void setWeight(BigDecimal value){weight=value;}
    @NotBlank(message="Изберете активност.") @Pattern(regexp="LOW|MODERATE|HIGH",message="Изберете ниска, умерена или висока активност.")
    private String activity;
    public String getActivity(){return activity;}
    public void setActivity(String value){activity=value;}
    @DecimalMin(value="1200",message="Целта трябва да е между 1200 и 6000 ккал.") @DecimalMax(value="6000",message="Целта трябва да е между 1200 и 6000 ккал.") @Digits(integer=4,fraction=0,message="Въведете цели килокалории.")
    private BigDecimal target;
    public BigDecimal getTarget(){return target;}
    public void setTarget(BigDecimal value){target=value;}
    @NotNull(message="Потвърдете приложимостта на примерния калкулатор.") @AssertTrue(message="Калкулаторът не е предназначен за посочените специални потребности.")
    private Boolean eligible;
    public Boolean getEligible(){return eligible;}
    public void setEligible(Boolean value){eligible=value;}
}

