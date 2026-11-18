package com.fitpulse.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReviewForm {
    @NotBlank(message = "Въведи коментар.")
    @Size(max = 2000, message = "Коментарът допуска най-много {max} знака.")
    private String comment;

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
