package com.fitpulse.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {
    @NotBlank(message = "Потребителското име е задължително.")
    @Size(min = 3, max = 30, message = "Потребителското име трябва да бъде между 3 и 30 знака.")
    private String username;

    @NotBlank(message = "Имейл адресът е задължителен.")
    @Email(message = "Въведете валиден имейл адрес.")
    private String email;
    @Size(max = 80) private String displayName;
    @Size(max = 500) private String bio;
    @Size(max = 100) private String homeCity;
    private boolean publicProfile;
    private boolean showCity;
    private boolean showFavorites;

    public boolean isShowCity() { return showCity; }
    public void setShowCity(boolean showCity) { this.showCity = showCity; }
    public boolean isShowFavorites() { return showFavorites; }
    public void setShowFavorites(boolean showFavorites) { this.showFavorites = showFavorites; }

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
    public String getDisplayName(){return displayName;} public void setDisplayName(String v){displayName=v;}
    public String getBio(){return bio;} public void setBio(String v){bio=v;}
    public String getHomeCity(){return homeCity;} public void setHomeCity(String v){homeCity=v;}
    public boolean isPublicProfile(){return publicProfile;} public void setPublicProfile(boolean v){publicProfile=v;}
}
