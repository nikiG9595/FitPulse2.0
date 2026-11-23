package com.fitpulse.model.entity;

import com.fitpulse.model.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    @NotBlank
    @Size(min = 3, max = 30)
    private String username;

    @Column(nullable = false, unique = true)
    @NotBlank
    @Email
    private String email;

    @Column(nullable = false)
    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private UserRole role;

    @Column(length = 32)
    private String acceptedTermsVersion;
    private Instant termsAcceptedAt;

    public String getAcceptedTermsVersion() { return acceptedTermsVersion; }
    public Instant getTermsAcceptedAt() { return termsAcceptedAt; }

    public void acceptTerms(String version, Instant acceptedAt) {
        this.acceptedTermsVersion = version;
        this.termsAcceptedAt = acceptedAt;
    }

    @Column(nullable = false) private boolean welcomeShown = true;
    public boolean isWelcomeShown(){return welcomeShown;}
    public void setWelcomeShown(boolean value){welcomeShown=value;}

    @Column(length = 80) private String displayName;
    @Column(length = 500) private String bio;
    @Column(length = 100) private String homeCity;
    @Column(nullable = false) private boolean publicProfile;
    @Column(nullable = false) private boolean showCity;
    @Column(nullable = false) private boolean showFavorites;

    public boolean isShowCity() { return showCity; }
    public void setShowCity(boolean showCity) { this.showCity = showCity; }
    public boolean isShowFavorites() { return showFavorites; }
    public void setShowFavorites(boolean showFavorites) { this.showFavorites = showFavorites; }

    @Column(unique = true)
    private String googleSubject;

    public String getGoogleSubject() { return googleSubject; }
    public void setGoogleSubject(String googleSubject) { this.googleSubject = googleSubject; }

    public UUID getId() {
        return id;
    }

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
        this.email = email == null ? null : email.strip().toLowerCase(java.util.Locale.ROOT);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getHomeCity() { return homeCity; }
    public void setHomeCity(String homeCity) { this.homeCity = homeCity; }
    public boolean isPublicProfile() { return publicProfile; }
    public void setPublicProfile(boolean publicProfile) { this.publicProfile = publicProfile; }


    @jakarta.validation.constraints.Pattern(regexp="MALE|FEMALE", message = "{ui.select.a.sex.2}")
    private String sex;
    @jakarta.validation.constraints.Past(message = "{ui.enter.a.date.of.birth.in.the}")
    @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    private java.time.LocalDate birthDate;
    @jakarta.validation.constraints.DecimalMin("120")
    @jakarta.validation.constraints.DecimalMax("230")
    @jakarta.validation.constraints.Digits(integer=3, fraction=2)
    private java.math.BigDecimal height;
    public String getSex(){return sex;}
    public void setSex(String v){sex=v==null||v.isBlank()?null:v;}
    public java.time.LocalDate getBirthDate(){return birthDate;}
    public void setBirthDate(java.time.LocalDate v){birthDate=v;}
    public java.math.BigDecimal getHeight(){return height;}
    public void setHeight(java.math.BigDecimal v){height=v;}
    public Integer getAge(){return birthDate==null?null:java.time.Period.between(birthDate,java.time.LocalDate.now()).getYears();}

}
