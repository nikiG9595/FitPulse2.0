package com.fitpulse.model.dto;

import com.fitpulse.model.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public class RoleChangeRequest {
    @NotNull(message = "Изберете валидна роля.")
    private UserRole role;

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
