package com.fitpulse.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_meal_intake")
public class DailyMealIntake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    // Keep the original plan ID even if the user later deletes that plan.
    @Column(nullable = false)
    private Long planId;
    @Column(nullable = false)
    private LocalDate intakeDate;
    @Column(nullable = false)
    private int mealIndex;
    @Column(nullable = false, length = 64)
    private String mealRevision;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;

    protected DailyMealIntake() {}

    public DailyMealIntake(User user, Long planId, LocalDate date, int mealIndex, String mealRevision,
                           double calories, double protein, double carbs, double fat) {
        this.user = user;
        this.planId = planId;
        this.intakeDate = date;
        this.mealIndex = mealIndex;
        this.mealRevision = mealRevision;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public int getMealIndex() { return mealIndex; }
    public String getMealRevision() { return mealRevision; }
    public double getCalories() { return calories; }
    public double getProtein() { return protein; }
    public double getCarbs() { return carbs; }
    public double getFat() { return fat; }
}
