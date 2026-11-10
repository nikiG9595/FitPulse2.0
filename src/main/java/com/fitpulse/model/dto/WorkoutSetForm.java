package com.fitpulse.model.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class WorkoutSetForm { public Long exerciseId;
    @NotBlank(message="Избери или въведи упражнение.") @Size(max=150,message="Името може да съдържа до 150 знака.") public String exerciseName;
    @Min(value=1,message="Номерът на серията трябва да е поне 1.") @Max(value=100,message="Номерът на серията не може да надвишава 100.") public int setNumber=1;
    @Min(value=1,message="Въведи поне едно повторение.") @Max(value=1000,message="Повторенията не могат да надвишават 1000.") public int repetitions=10;
    @NotNull(message="Въведи тежест, включително 0 за собствено тегло.") @DecimalMin(value="0",message="Тежестта не може да е отрицателна.") @DecimalMax(value="99999.99",message="Тежестта е извън допустимия обхват.") @Digits(integer=5,fraction=2,message="Използвай до два десетични знака.") public BigDecimal weight=BigDecimal.ZERO;
}
