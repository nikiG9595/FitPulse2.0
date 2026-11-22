package com.fitpulse.model.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class WorkoutSetForm { public Long exerciseId;
    @NotBlank(message = "{ui.select.or.enter.an.exercise}") @Size(max=150,message = "{ui.the.name.can.contain.up.to.150}") public String exerciseName;
    @Min(value=1,message = "{ui.the.set.number.must.be.at.least}") @Max(value=100,message = "{ui.the.set.number.cannot.exceed.100}") public int setNumber=1;
    @Min(value=1,message = "{ui.enter.at.least.one.rep}") @Max(value=1000,message = "{ui.reps.cannot.exceed.1000}") public int repetitions=10;
    @NotNull(message = "{ui.enter.a.weight.including.0.for.bodyweight}") @DecimalMin(value="0",message = "{ui.weight.cannot.be.negative}") @DecimalMax(value="99999.99",message = "{ui.weight.is.out.of.range}") @Digits(integer=5,fraction=2,message = "{ui.use.up.to.two.decimal.places}") public BigDecimal weight=BigDecimal.ZERO;
}
