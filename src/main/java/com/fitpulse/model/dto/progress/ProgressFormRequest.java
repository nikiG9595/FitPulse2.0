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
    @NotNull(message = "{ui.weight.is.required}")
    @DecimalMin(value = "20.0", message = "{ui.weight.must.be.at.least.20.kg}")
    @DecimalMax(value = "400.0", message = "{ui.weight.cannot.exceed.400.kg}")
    @jakarta.validation.constraints.Digits(integer=3,fraction=2,message = "{ui.use.up.to.two.decimal.places}")
    private BigDecimal weight;

    @DecimalMin(value = "1.0", message = "{ui.body.fat.must.be.at.least.1}")
    @DecimalMax(value = "80.0", message = "{ui.body.fat.cannot.exceed.80}")
    private BigDecimal bodyFatPercentage;

    @NotNull(message = "{ui.a.date.is.required}")
    @PastOrPresent(message = "{ui.the.date.cannot.be.in.the.future.2}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate recordedAt;

    @Size(max = 500, message = "{ui.the.note.cannot.exceed.500.characters}")
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
