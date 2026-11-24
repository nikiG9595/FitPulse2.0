package com.fitpulse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.model.dto.WeeklyMenu;
import com.fitpulse.model.entity.DailyMealIntake;
import com.fitpulse.model.entity.User;
import com.fitpulse.model.entity.UserMealPlan;
import com.fitpulse.repository.DailyMealIntakeRepository;
import com.fitpulse.repository.UserMealPlanRepository;
import com.fitpulse.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

@Service
public class DailyMealIntakeService {
    private static final ZoneId INTAKE_ZONE = ZoneId.of("Europe/Sofia");
    private final UserService users;
    private final UserRepository accounts;
    private final UserMealPlanRepository plans;
    private final DailyMealIntakeRepository intake;
    private final PersonalNutritionService nutrition;
    private final EntityManager entities;
    private final ObjectMapper mapper;

    public DailyMealIntakeService(UserService users, UserRepository accounts, UserMealPlanRepository plans,
                                  DailyMealIntakeRepository intake, PersonalNutritionService nutrition,
                                  EntityManager entities, ObjectMapper mapper) {
        this.users = users;
        this.accounts = accounts;
        this.plans = plans;
        this.intake = intake;
        this.nutrition = nutrition;
        this.entities = entities;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Today today() {
        var user = users.getCurrentUser();
        var date = LocalDate.now(INTAKE_ZONE);
        var choices = availablePlans(user);
        var active = activePlan(choices);
        var meals = active == null ? List.<WeeklyMenu.Meal>of() : mealsFor(active, date);
        var entries = active == null ? List.<DailyMealIntake>of()
                : intake.findAllByUserAndPlanIdAndIntakeDate(user, active.getId(), date);
        var planned = Totals.ZERO;
        var items = new ArrayList<MealItem>();
        int previousPortions = entries.size();
        for (int i = 0; i < meals.size(); i++) {
            var meal = meals.get(i);
            var revision = revision(meal);
            var totals = Totals.from(meal);
            var eaten = entryFor(entries, i, revision) != null;
            if (eaten) previousPortions--;
            planned = planned.plus(totals);
            items.add(new MealItem(i, revision, meal, totals, eaten));
        }
        var consumed = entries.stream().map(Totals::from).reduce(Totals.ZERO, Totals::plus);
        return new Today(date, choices, active, items, planned, consumed, previousPortions);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean selectPlan(long planId) {
        var user = lockAccount();
        var plan = plans.findOwnedForUpdate(planId, user).filter(DailyMealIntakeService::trackable).orElse(null);
        if (plan == null) return false;
        entities.createQuery("update UserMealPlan p set p.intakeSelected = false where p.user = :user and p.intakeSelected = true")
                .setParameter("user", user).executeUpdate();
        entities.createQuery("update UserMealPlan p set p.intakeSelected = true where p.id = :id and p.user = :user")
                .setParameter("id", planId).setParameter("user", user).executeUpdate();
        return true;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean mark(long planId, LocalDate date, int index, String revision, boolean eaten) {
        var user = lockAccount();
        if (!LocalDate.now(INTAKE_ZONE).equals(date)) return false;
        var plan = activePlan(availablePlans(user));
        if (plan == null || !Objects.equals(plan.getId(), planId)) return false;
        // Portion changes and recipe substitutions also take a write lock on this plan.
        entities.refresh(plan, LockModeType.PESSIMISTIC_WRITE);
        if (!trackable(plan)) return false;
        var meals = mealsFor(plan, date);
        if (index < 0 || index >= meals.size()) return false;
        var meal = meals.get(index);
        if (!revision(meal).equals(revision)) return false;

        var entries = intake.findAllByUserAndPlanIdAndIntakeDate(user, planId, date);
        var entry = entryFor(entries, index, revision);
        if (eaten && entry == null) {
            var totals = Totals.from(meal);
            intake.save(new DailyMealIntake(user, planId, date, index, revision,
                    totals.calories(), totals.protein(), totals.carbs(), totals.fat()));
        } else if (!eaten && entry != null) {
            intake.delete(entry);
        }
        return true;
    }

    private User lockAccount() {
        return accounts.lockForLibrary(users.getCurrentUser().getId()).orElseThrow();
    }

    private List<UserMealPlan> availablePlans(User user) {
        return plans.findAllByUserOrderByCreatedAtDesc(user).stream()
                .filter(DailyMealIntakeService::trackable).toList();
    }

    private static boolean trackable(UserMealPlan plan) {
        return plan.isListed() && plan.getWeeklyMenu() != null && !plan.getWeeklyMenu().isBlank();
    }

    private static UserMealPlan activePlan(List<UserMealPlan> choices) {
        return choices.stream().max(Comparator.comparing(UserMealPlan::isIntakeSelected)
                .thenComparing(UserMealPlan::getId)).orElse(null);
    }

    private List<WeeklyMenu.Meal> mealsFor(UserMealPlan plan, LocalDate date) {
        try {
            var days = nutrition.menu(plan).days();
            int index = date.getDayOfWeek().getValue() - 1;
            return days != null && index < days.size() && days.get(index).meals() != null
                    ? days.get(index).meals() : List.of();
        } catch (com.fitpulse.exception.FitPulseException ignored) {
            return List.of();
        }
    }

    private static DailyMealIntake entryFor(List<DailyMealIntake> entries, int index, String revision) {
        return entries.stream().filter(entry -> entry.getMealIndex() == index
                && entry.getMealRevision().equals(revision)).findFirst().orElse(null);
    }

    private String revision(WeeklyMenu.Meal meal) {
        try {
            byte[] recipe = mapper.writeValueAsBytes(new MealRevision(meal.recipeCode(), meal.ingredients()));
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(recipe));
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot compute meal revision", e);
        }
    }

    private record MealRevision(String recipe, List<WeeklyMenu.Ingredient> ingredients) {}

    public record MealItem(int index, String revision, WeeklyMenu.Meal meal, Totals totals, boolean eaten) {}

    public record Today(LocalDate date, List<UserMealPlan> choices, UserMealPlan plan, List<MealItem> meals,
                        Totals planned, Totals consumed, int previousPortions) {}

    public record Totals(double calories, double protein, double carbs, double fat) {
        private static final Totals ZERO = new Totals(0, 0, 0, 0);

        private static Totals from(WeeklyMenu.Meal meal) {
            double protein = meal.protein(), carbs = meal.carbs(), fat = meal.fat();
            return new Totals(4 * protein + 4 * carbs + 9 * fat, protein, carbs, fat);
        }

        private static Totals from(DailyMealIntake entry) {
            return new Totals(entry.getCalories(), entry.getProtein(), entry.getCarbs(), entry.getFat());
        }

        private Totals plus(Totals other) {
            return new Totals(calories + other.calories, protein + other.protein, carbs + other.carbs, fat + other.fat);
        }

    }
}
