package com.fitpulse.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Inputs populated from the private profile before calculation. */
public class MealPersonalizationForm {
    @NotBlank(message="Изберете пол.") @Pattern(regexp="MALE|FEMALE",message="Изберете Мъж или Жена.")
    private String sex;
    @Past(message="Въведи минала дата на раждане.")
    @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private java.time.LocalDate birthDate;
    public java.time.LocalDate getBirthDate(){return birthDate;}
    public void setBirthDate(java.time.LocalDate value){birthDate=value;}
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
    @NotNull(message="Потвърдете приложимостта на примерния калкулатор.") @AssertTrue(message="Калкулаторът не е предназначен за посочените специални потребности.")
    private Boolean eligible;
    public Boolean getEligible(){return eligible;}
    public void setEligible(Boolean value){eligible=value;}
}

