package com.fitpulse.model.dto;
import jakarta.validation.constraints.*;
public class TrainerForm {
    @NotBlank @Size(max=150) public String name;
    @Min(18) @Max(100) public int age=18;
    @NotBlank @Size(max=100) public String country;
    @Min(0) @Max(80) public int experienceYears;
    @NotBlank @Size(max=2000) public String bio;
    @NotBlank @Size(max=1000) @Pattern(regexp="(?:/css/images/[A-Za-z0-9_.-]+|https://[^\\s]+)") public String photoUrl;
    public boolean published;
}
