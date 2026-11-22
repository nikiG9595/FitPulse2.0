package com.fitpulse.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Inputs populated from the private profile before calculation. */
public class MealPersonalizationForm {
    @NotBlank(message = "{ui.select.a.sex}") @Pattern(regexp="MALE|FEMALE",message = "{ui.select.male.or.female}")
    private String sex;
    @Past(message = "{ui.enter.a.date.of.birth.in.the}")
    @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private java.time.LocalDate birthDate;
    public java.time.LocalDate getBirthDate(){return birthDate;}
    public void setBirthDate(java.time.LocalDate value){birthDate=value;}
    public String getSex(){return sex;}
    public void setSex(String value){sex=value;}
    @NotNull(message = "{ui.enter.an.age}") @Min(value=18,message = "{ui.this.feature.is.for.adults.only}") @Max(value=100,message = "{ui.supported.age.18.100.years}")
    private Integer age;
    public Integer getAge(){return age;}
    public void setAge(Integer value){age=value;}
    @NotNull(message = "{ui.enter.your.height}") @DecimalMin(value="120",message = "{ui.supported.height.120.230.cm}") @DecimalMax(value="230",message = "{ui.supported.height.120.230.cm}") @Digits(integer=3,fraction=2,message = "{ui.enter.height.with.up.to.two.decimal}")
    private BigDecimal height;
    public BigDecimal getHeight(){return height;}
    public void setHeight(BigDecimal value){height=value;}
    @NotNull(message = "{ui.enter.your.weight}") @DecimalMin(value="35",message = "{ui.supported.weight.35.300.kg}") @DecimalMax(value="300",message = "{ui.supported.weight.35.300.kg}") @Digits(integer=3,fraction=2,message = "{ui.enter.weight.with.up.to.two.decimal}")
    private BigDecimal weight;
    public BigDecimal getWeight(){return weight;}
    public void setWeight(BigDecimal value){weight=value;}
    @NotBlank(message = "{ui.select.an.activity.level}") @Pattern(regexp="LOW|MODERATE|HIGH",message = "{ui.select.low.moderate.or.high.activity}")
    private String activity;
    public String getActivity(){return activity;}
    public void setActivity(String value){activity=value;}
    @NotNull(message = "{ui.confirm.that.the.example.calculator.is.suitable}") @AssertTrue(message = "{ui.the.calculator.is.not.intended.for.the}")
    private Boolean eligible;
    public Boolean getEligible(){return eligible;}
    public void setEligible(Boolean value){eligible=value;}
}

