package com.fitpulse.model.dto.progress;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BodyMeasurementForm {
    private Long id;
    @NotBlank(message = "{ui.select.a.measurement.2}") private String type="waist";
    @NotNull(message = "{ui.enter.a.date}") @PastOrPresent(message = "{ui.the.date.cannot.be.in.the.future}") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) private LocalDate recordedAt=LocalDate.now();
    @NotNull(message = "{ui.enter.a.measurement}") @DecimalMin(value="10",message = "{ui.the.measurement.must.be.at.least.10}") @DecimalMax(value="300",message = "{ui.the.measurement.cannot.exceed.300.cm}") @Digits(integer=3,fraction=2,message = "{ui.use.up.to.two.decimal.places}") private BigDecimal value;
    public Long getId(){return id;}public void setId(Long v){id=v;}public String getType(){return type;}public void setType(String v){type=v;}
    public LocalDate getRecordedAt(){return recordedAt;}public void setRecordedAt(LocalDate v){recordedAt=v;}public BigDecimal getValue(){return value;}public void setValue(BigDecimal v){value=v;}
}
