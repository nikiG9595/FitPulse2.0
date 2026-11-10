package com.fitpulse.model.dto.progress;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BodyMeasurementForm {
    private Long id;
    @NotBlank(message="Избери измерване.") private String type="waist";
    @NotNull(message="Въведи дата.") @PastOrPresent(message="Датата не може да е в бъдещето.") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) private LocalDate recordedAt=LocalDate.now();
    @NotNull(message="Въведи обиколка.") @DecimalMin(value="10",message="Обиколката трябва да е поне 10 см.") @DecimalMax(value="300",message="Обиколката не може да надвишава 300 см.") @Digits(integer=3,fraction=2,message="Използвай до два десетични знака.") private BigDecimal value;
    public Long getId(){return id;}public void setId(Long v){id=v;}public String getType(){return type;}public void setType(String v){type=v;}
    public LocalDate getRecordedAt(){return recordedAt;}public void setRecordedAt(LocalDate v){recordedAt=v;}public BigDecimal getValue(){return value;}public void setValue(BigDecimal v){value=v;}
}
