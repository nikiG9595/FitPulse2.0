package com.fitpulse.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReviewForm {
    @NotBlank(message = "{ui.enter.a.comment}")
    @Size(max = 2000, message = "{ui.comments.can.contain.up.to.max.characters}")
    private String comment;

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
