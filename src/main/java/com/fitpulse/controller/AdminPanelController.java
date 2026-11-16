package com.fitpulse.controller;

import com.fitpulse.model.dto.GymForm;
import com.fitpulse.model.entity.MealPlanTemplate;
import com.fitpulse.model.entity.ProgramTemplate;
import com.fitpulse.model.enums.*;
import com.fitpulse.repository.UserRepository;
import com.fitpulse.service.GymDirectoryService;
import com.fitpulse.service.SupportService;
import com.fitpulse.service.WellnessService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminPanelController {
    private final GymDirectoryService gyms;
    private final WellnessService wellness;
    private final UserRepository users;
    private final SupportService support;

    public AdminPanelController(GymDirectoryService g, WellnessService w, UserRepository u, SupportService s) {
        gyms = g;
        wellness = w;
        users = u;
        support = s;
    }

    @GetMapping
    public String dashboard(Model m) {
        m.addAttribute("gymCount", gyms.publishedCount());
        m.addAttribute("pendingCount", gyms.pendingCount());
        m.addAttribute("unreadMessageCount", support.unreadCount());
        m.addAttribute("userCount", users.count());
        m.addAttribute("savedProgramCount", wellness.savedProgramCount());
        m.addAttribute("personalProgramCount", wellness.personalProgramCount());
        m.addAttribute("commentCount", gyms.commentCount());
        m.addAttribute("newCommentCount", gyms.newCommentCount());
        return "admin/dashboard";
    }

    @GetMapping("/gyms")
    public String gymList(@RequestParam(required = false) ContentStatus status, Model m) {
        m.addAttribute("gyms", gyms.allGyms().stream().filter(g -> status == null || g.getStatus() == status).toList());
        m.addAttribute("status", status);
        return "admin/gyms";
    }

    @GetMapping("/gyms/create")
    public String create(Model m) {
        prepareGym(m, new GymForm(), null);
        return "admin/gym-form";
    }

    @PostMapping("/gyms/create")
    public String createPost(@Valid @ModelAttribute GymForm gymForm, BindingResult e, Model m) {
        if (e.hasErrors()) {
            prepareGym(m, gymForm, null);
            return "admin/gym-form";
        }
        gyms.saveGym(null, gymForm);
        return "redirect:/admin/gyms";
    }

    @GetMapping("/gyms/{id}/edit")
    public String edit(@PathVariable Long id, Model m) {
        prepareGym(m, gyms.form(gyms.byId(id)), id);
        return "admin/gym-form";
    }

    @PostMapping("/gyms/{id}/edit")
    public String editPost(@PathVariable Long id, @Valid @ModelAttribute GymForm gymForm, BindingResult e, Model m) {
        if (e.hasErrors()) {
            prepareGym(m, gymForm, id);
            return "admin/gym-form";
        }
        gyms.saveGym(id, gymForm);
        return "redirect:/admin/gyms";
    }

    @PostMapping("/gyms/{id}/delete")
    public String deleteGym(@PathVariable Long id, RedirectAttributes ra) {
        gyms.deleteGym(id);
        ra.addFlashAttribute("success", "Фитнесът е премахнат от публичния каталог. Оценките, любимите и историята са запазени.");
        return "redirect:/admin/gyms";
    }

    @PostMapping("/gyms/{id}/photos")
    public String photo(@PathVariable Long id, @RequestParam String imageUrl, @RequestParam String altText) {
        gyms.addPhoto(id, imageUrl, altText);
        return "redirect:/admin/gyms/" + id + "/edit";
    }

    @PostMapping("/photos/{id}/delete")
    public String deletePhoto(@PathVariable Long id) {
        return "redirect:/admin/gyms/" + gyms.deletePhoto(id) + "/edit";
    }

    @GetMapping("/cities")
    public String cities(Model m) {
        m.addAttribute("cities", gyms.allCities());
        return "admin/cities";
    }

    @PostMapping("/cities")
    public String city(@RequestParam(required = false) Long id, @RequestParam String name) {
        gyms.saveCity(id, name);
        return "redirect:/admin/cities";
    }

    @GetMapping("/suggestions")
    public String suggestions(@RequestParam(required = false) ContentStatus status, Model m) {
        m.addAttribute("suggestions", gyms.allSuggestions().stream().filter(s -> status == null || s.getStatus() == status).toList());
        m.addAttribute("status", status);
        return "admin/suggestions";
    }

    @PostMapping("/suggestions/{id}")
    public String suggestion(@PathVariable Long id, @RequestParam ContentStatus status, @RequestParam(required = false) String adminNote, RedirectAttributes flash) {
        try {
            gyms.suggestionStatus(id, status, adminNote);
            flash.addFlashAttribute("success", status == ContentStatus.APPROVED ? "Предложението е одобрено и залата е публикувана в каталога." : "Предложението е отхвърлено и не е публикувано.");
        } catch (com.fitpulse.exception.FitPulseException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/suggestions";
    }

    @GetMapping("/reviews")
    public String reviews(@RequestParam(defaultValue = "all") String filter, Model m) {
        if (!java.util.Set.of("all", "new", "reviewed").contains(filter)) filter = "all";
        m.addAttribute("reviews", gyms.adminReviews(filter));
        m.addAttribute("filter", filter);
        m.addAttribute("detail", false);
        m.addAttribute("reports", gyms.allReports());
        return "admin/reviews";
    }

    @GetMapping("/reviews/{id}")
    public String reviewDetails(@PathVariable Long id, Model m) {
        m.addAttribute("reviews", java.util.List.of(gyms.openReview(id)));
        m.addAttribute("filter", "all");
        m.addAttribute("detail", true);
        m.addAttribute("reports", gyms.allReports());
        return "admin/reviews";
    }

    @PostMapping("/reviews/{id}")
    public String review(@PathVariable Long id, @RequestParam ContentStatus status) {
        gyms.reviewStatus(id, status);
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reports/{id}/resolve")
    public String resolveReport(@PathVariable Long id) {
        gyms.resolveReport(id);
        return "redirect:/admin/reviews";
    }

    @GetMapping("/programs")
    public String programs(@RequestParam(defaultValue = "catalog") String view, @RequestParam(defaultValue = "0") int page, Model m) {
        if (java.util.Set.of("saved", "personal", "unknown").contains(view)) {
            UserProgramOrigin origin = switch (view) {
                case "saved" -> UserProgramOrigin.CATALOG;
                case "personal" -> UserProgramOrigin.PERSONAL;
                default -> UserProgramOrigin.UNKNOWN;
            };
            m.addAttribute("userPrograms", wellness.adminUserPrograms(origin, page));
        } else {
            view = "catalog";
            m.addAttribute("templates", wellness.allTemplates());
        }
        m.addAttribute("view", view);
        return "admin/programs";
    }

    @GetMapping("/programs/create")
    public String createProgram(Model m) {
        m.addAttribute("template", new ProgramTemplate());
        m.addAttribute("goals", FitnessGoal.values());
        m.addAttribute("levels", ExperienceLevel.values());
        m.addAttribute("locations", TrainingLocation.values());
        return "admin/program-form";
    }

    @GetMapping("/programs/{id}/edit")
    public String editProgram(@PathVariable Long id, Model m) {
        m.addAttribute("template", wellness.template(id));
        m.addAttribute("goals", FitnessGoal.values());
        m.addAttribute("levels", ExperienceLevel.values());
        m.addAttribute("locations", TrainingLocation.values());
        return "admin/program-form";
    }

    @PostMapping("/programs")
    public String saveProgram(@RequestParam(required = false) Long id, @RequestParam String name, @RequestParam FitnessGoal goal, @RequestParam ExperienceLevel experience, @RequestParam int trainingDays, @RequestParam TrainingLocation location, @RequestParam String description) {
        wellness.saveTemplate(id, name, goal, experience, trainingDays, location, description);
        return "redirect:/admin/programs";
    }

    @PostMapping("/programs/{id}/active")
    public String activeProgram(@PathVariable Long id, @RequestParam boolean active) {
        wellness.templateActive(id, active);
        return "redirect:/admin/programs";
    }

    @PostMapping("/programs/{id}/delete")
    public String deleteProgram(@PathVariable Long id) {
        wellness.deleteTemplate(id);
        return "redirect:/admin/programs";
    }

    @GetMapping("/meal-plans")
    public String meals(Model m) {
        m.addAttribute("templates", wellness.allMealTemplates());
        return "admin/meal-plans";
    }

    @GetMapping("/meal-plans/create")
    public String createMeal(Model m) {
        m.addAttribute("template", new MealPlanTemplate());
        m.addAttribute("goals", FitnessGoal.values());
        return "admin/meal-plan-form";
    }

    @GetMapping("/meal-plans/{id}/edit")
    public String editMeal(@PathVariable Long id, Model m) {
        m.addAttribute("template", wellness.mealTemplate(id));
        m.addAttribute("goals", FitnessGoal.values());
        return "admin/meal-plan-form";
    }

    @PostMapping("/meal-plans")
    public String saveMeal(@RequestParam(required = false) Long id, @RequestParam String name, @RequestParam FitnessGoal goal, @RequestParam String description, @RequestParam String meals, @RequestParam String exampleFoods, @RequestParam String disclaimer) {
        wellness.saveMealTemplate(id, name, goal, description, meals, exampleFoods, disclaimer);
        return "redirect:/admin/meal-plans";
    }

    @PostMapping("/meal-plans/{id}/active")
    public String activeMeal(@PathVariable Long id, @RequestParam boolean active) {
        wellness.mealTemplateActive(id, active);
        return "redirect:/admin/meal-plans";
    }

    @PostMapping("/meal-plans/{id}/delete")
    public String deleteMeal(@PathVariable Long id) {
        wellness.deleteMealTemplate(id);
        return "redirect:/admin/meal-plans";
    }

    private void prepareGym(Model m, GymForm f, Long id) {
        m.addAttribute("gymForm", f);
        m.addAttribute("gymId", id);
        m.addAttribute("cities", gyms.allCities());
        m.addAttribute("statuses", new ContentStatus[]{ContentStatus.DRAFT, ContentStatus.PUBLISHED, ContentStatus.ARCHIVED});
        if (id != null) m.addAttribute("gym", gyms.byId(id));
    }
}
