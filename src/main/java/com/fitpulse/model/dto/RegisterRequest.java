package com.fitpulse.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Потребителското име е задължително.")
    @Size(min = 3, max = 30, message = "Потребителското име трябва да бъде между 3 и 30 знака.")
    private String username;

    @NotBlank(message = "Имейл адресът е задължителен.")
    @Email(message = "Въведете валиден имейл адрес.")
    private String email;

    @NotBlank(message = "Паролата е задължителна.")
    @Size(min = 6, message = "Паролата трябва да съдържа поне 6 знака.")
    private String password;

    @NotBlank(message = "Потвърждението на паролата е задължително.")
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
