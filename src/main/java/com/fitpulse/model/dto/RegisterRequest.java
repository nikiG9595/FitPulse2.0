package com.fitpulse.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "{ui.username.is.required}")
    @Size(min = 3, max = 30, message = "{ui.username.must.be.between.3.and.30}")
    private String username;

    @NotBlank(message = "{ui.email.is.required}")
    @Email(message = "{ui.enter.a.valid.email.address}")
    private String email;

    @NotBlank(message = "{ui.password.is.required}")
    @Size(min = 6, message = "{ui.the.password.must.be.at.least.6}")
    private String password;

    @NotBlank(message = "{ui.password.confirmation.is.required}")
    private String confirmPassword;

    @AssertTrue(message = "{legal.terms.required}")
    private boolean termsAccepted;
    private String termsVersion;

    public boolean isTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(boolean termsAccepted) { this.termsAccepted = termsAccepted; }
    public String getTermsVersion() { return termsVersion; }
    public void setTermsVersion(String termsVersion) { this.termsVersion = termsVersion; }

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
