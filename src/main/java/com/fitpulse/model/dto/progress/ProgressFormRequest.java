package com.fitpulse.model.dto.progress;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProgressFormRequest {
    @NotNull(message = "Теглото е задължително.")
    @DecimalMin(value = "20.0", message = "Теглото трябва да бъде поне 20 кг.")
    @DecimalMax(value = "400.0", message = "Теглото не може да надвишава 400 кг.")
    @jakarta.validation.constraints.Digits(integer=3,fraction=2,message="Използвай до два десетични знака.")
    private BigDecimal weight;

    @DecimalMin(value = "1.0", message = "Телесните мазнини трябва да бъдат поне 1%.")
    @DecimalMax(value = "80.0", message = "Телесните мазнини не могат да надвишават 80%.")
    private BigDecimal bodyFatPercentage;

    @NotNull(message = "Датата е задължителна.")
    @PastOrPresent(message = "Датата не може да бъде в бъдещето.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate recordedAt;

    @Size(max = 500, message = "Бележката не може да надвишава 500 знака.")
    private String note;

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getBodyFatPercentage() {
        return bodyFatPercentage;
    }

    public void setBodyFatPercentage(BigDecimal bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }

    public LocalDate getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDate recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
