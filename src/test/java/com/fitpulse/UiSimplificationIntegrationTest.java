package com.fitpulse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitpulse.model.dto.*;
import com.fitpulse.model.dto.progress.ProgressFormRequest;
import com.fitpulse.model.entity.*;
import com.fitpulse.model.enums.*;
import com.fitpulse.repository.*;
import com.fitpulse.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Transactional
class UiSimplificationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired UserService userService;
    @Autowired PersonalDataService data;
    @Autowired ProgressService progress;
    @Autowired ProgressRecordRepository records;
    @Autowired MealPlanTemplateRepository meals;
    @Autowired UserMealPlanRepository personalMeals;
    @Autowired FoodRepository foods;
    @Autowired BodyMeasurementRepository bodyMeasurements;
    @Autowired ObjectMapper mapper;
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder passwords;
    User account;

    @BeforeEach void setup(){
        account=new User();account.setUsername("ui"+UUID.randomUUID().toString().substring(0,12));
        account.setEmail(account.getUsername()+"@example.com");account.setPassword(passwords.encode("password123"));
        account.setRole(UserRole.MEMBER);users.saveAndFlush(account);
        SecurityContextHolder.clearContext();
    }
    @AfterEach void clear(){SecurityContextHolder.clearContext();}
    void authenticate(){SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(account.getUsername(),null,List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_MEMBER"))));}
    void completeProfile(){account.setSex("MALE");account.setBirthDate(LocalDate.of(1995,5,12));account.setHeight(new BigDecimal("180"));users.saveAndFlush(account);data.saveWeight(account,new BigDecimal("80"));}
    MealPlanTemplate template() throws Exception {
        Food food=new Food();food.setCode("test"+UUID.randomUUID().toString().substring(0,8));food.setName(food.getCode());food.setState("готов");food.setCategory("Тест");food.setProtein(new BigDecimal("20"));food.setFat(new BigDecimal("10"));food.setCarbs(new BigDecimal("40"));foods.saveAndFlush(food);
        var ingredient=new WeeklyMenu.Ingredient(food.getName(),150,"готов",20,10,40,food.getCode());
        var meal=new WeeklyMenu.Meal("Обяд","Тестово ястие",List.of(ingredient),"Приготви.","");
        var days=java.util.stream.IntStream.range(0,7).mapToObj(i->new WeeklyMenu.Day("Ден "+(i+1),Collections.nCopies(5,meal))).toList();
        MealPlanTemplate t=new MealPlanTemplate();t.setName("Оригинален режим");t.setDescription("Описание");t.setGoal(FitnessGoal.MUSCLE);t.setMeals("Меню");t.setDisclaimer("Пример");t.setWeeklyMenu(mapper.writeValueAsString(new WeeklyMenu("FitPulse","",days)));return meals.saveAndFlush(t);
    }

    @Test void dailyIntakeKeepsMarksScopedAndPreservesTheConsumedPortion() throws Exception {
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Europe/Sofia"));
        var days = java.util.stream.IntStream.rangeClosed(1, 7).mapToObj(day ->
                new WeeklyMenu.Day("Day " + day, List.of(new WeeklyMenu.Meal("Breakfast", "Recipe " + day,
                        List.of(new WeeklyMenu.Ingredient("Oats", day * 100, "raw", 20, 10, 40, "oats")),
                        "", "", "oats")))).toList();
        var oldPlan = new UserMealPlan();
        oldPlan.setName("Alternate plan");
        oldPlan.setUser(account);
        oldPlan.setMeals("Weekly meals");
        oldPlan.setWeeklyMenu(mapper.writeValueAsString(new WeeklyMenu("", "", days)));
        entityManager.persist(oldPlan);
        var activePlan = new UserMealPlan();
        activePlan.setName("Personal plan");
        activePlan.setUser(account);
        activePlan.setMeals("Weekly meals");
        activePlan.setWeeklyMenu(oldPlan.getWeeklyMenu());
        entityManager.persist(activePlan);
        var member = user(account.getUsername()).roles("MEMBER");
        String card = mvc.perform(get("/meal-plans/intake").with(member)).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(card).contains("Recipe " + today.getDayOfWeek().getValue()).doesNotContain("checked=\"checked\"");
        var revision = java.util.regex.Pattern.compile("name=\"revision\" value=\"([0-9a-f]{64})\"").matcher(card);
        assertThat(revision.find()).isTrue();
        String hash = revision.group(1);
        var mark = post("/meal-plans/intake/meal").param("planId", activePlan.getId().toString())
                .param("date", today.toString()).param("index", "0").param("revision", hash);
        mvc.perform(mark.with(member)).andExpect(status().isForbidden());
        mvc.perform(post("/meal-plans/intake/meal").with(member).with(csrf())
                .param("planId", activePlan.getId().toString()).param("date", today.minusDays(1).toString())
                .param("index", "0").param("revision", hash).param("eaten", "true")).andExpect(status().isConflict());
        mvc.perform(post("/meal-plans/intake/meal").with(member).with(csrf())
                .param("planId", activePlan.getId().toString()).param("date", today.toString())
                .param("index", "0").param("revision", hash).param("eaten", "true")).andExpect(status().isNoContent());
        var other = new User();
        other.setUsername("intake" + UUID.randomUUID().toString().substring(0, 12));
        other.setEmail(other.getUsername() + "@example.com");
        other.setPassword(account.getPassword());
        other.setRole(UserRole.MEMBER);
        users.saveAndFlush(other);
        mvc.perform(post("/meal-plans/intake/meal").with(user(other.getUsername()).roles("MEMBER")).with(csrf())
                .param("planId", activePlan.getId().toString()).param("date", today.toString())
                .param("index", "0").param("revision", hash)).andExpect(status().isConflict());
        mvc.perform(get("/meal-plans/intake").with(member)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("checked=\"checked\"")));
        mvc.perform(mark.with(member).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/meal-plans/intake").with(member)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("checked=\"checked\""))));
        mvc.perform(post("/meal-plans/intake/meal").with(member).with(csrf())
                .param("planId", activePlan.getId().toString()).param("date", today.toString())
                .param("index", "0").param("revision", hash).param("eaten", "true")).andExpect(status().isNoContent());
        int grams = today.getDayOfWeek().getValue() * 100;
        activePlan.setWeeklyMenu(activePlan.getWeeklyMenu().replace("\"grams\":" + grams + ".0", "\"grams\":" + (grams + 50) + ".0"));
        entityManager.flush();
        mvc.perform(get("/meal-plans/intake").with(member)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fp-intake-note")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("checked=\"checked\""))));
        mvc.perform(post("/meal-plans/intake/plan").with(member).with(csrf()).param("planId", oldPlan.getId().toString()))
                .andExpect(status().isNoContent());
        entityManager.clear();
        mvc.perform(get("/meal-plans/intake").with(member)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("fp-intake-note"))));
    }

    @Test void mobileDashboardAndInstallAssetsUseExistingAuthenticatedRoutes() throws Exception {
        String guest = mvc.perform(get("/")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(guest).contains("href=\"/manifest.webmanifest\"", "href=\"/images/pwa/apple-touch-icon.png\"")
                .doesNotContain("data-mobile-tab", "data-progress-url");
        String member = mvc.perform(get("/").with(user(account.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(member).contains("fp-mobile-home", account.getUsername(), "data-progress-url=\"/progress/data?period=90\"",
                "data-mobile-tab=\"training\"", "data-mobile-tab=\"nutrition\"");
        mvc.perform(get("/programs").param("goal", "FAT_LOSS").with(user(account.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("data-program-goal=\"FAT_LOSS\"")));
        mvc.perform(get("/meal-plans").param("goal", "MUSCLE").with(user(account.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("fp-goal-shortcuts")));
        mvc.perform(get("/profile").with(user(account.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk());
        mvc.perform(get("/manifest.webmanifest")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/manifest+json"))
                .andExpect(jsonPath("$.display").value("standalone"));
        mvc.perform(get("/service-worker.js")).andExpect(status().isOk());
        mvc.perform(get("/images/pwa/icon-512.png")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/png"));
        mvc.perform(get("/progress/data")).andExpect(status().is3xxRedirection());
    }

    @Test void languageCookieSelectsEnglishWithoutTranslatingProfileContent() throws Exception {
        var english = new jakarta.servlet.http.Cookie("fitpulse-language", "en");
        String home = mvc.perform(get("/")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(home).contains("lang=\"bg\"", "Започни сега", ">EN</button>");

        String about = mvc.perform(get("/about").cookie(english))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(about).contains("lang=\"en\"", "<h1>About FitPulse</h1>", "Personalized nutrition", ">BG</button>");

        String registration = mvc.perform(post("/register").cookie(english).with(csrf()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(registration).contains("Username is required.");

        account.setDisplayName("Потребител на FitPulse");
        users.saveAndFlush(account);
        String profile = mvc.perform(get("/profile").cookie(english).with(user(account.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(profile).contains("lang=\"en\"", "Edit your profile", account.getDisplayName());

        for (String path : List.of("/gyms", "/exercises", "/programs", "/meal-plans", "/progress", "/admin")) {
            String html = mvc.perform(get(path).cookie(english).with(user(account.getUsername()).roles("ADMIN")))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(html).contains("lang=\"en\"", ">BG</button>");
        }

        var program = new ProgramTemplate();
        program.setName("Старт у дома 3 дни");
        program.setDescription("Основни движения за изграждане на навик.");
        program.setGoal(FitnessGoal.GENERAL);
        program.setExperience(ExperienceLevel.BEGINNER);
        program.setLocation(TrainingLocation.HOME);
        program.setTrainingDays(3);
        entityManager.persist(program);

        try (var library = getClass().getResourceAsStream("/content/exercises.json")) {
            for (var exercise : mapper.readTree(library)) {
                if ("glute-bridge".equals(exercise.path("code").asText())) {
                    entityManager.persist(mapper.treeToValue(exercise, Exercise.class));
                    break;
                }
            }
        }
        String workout = mvc.perform(get("/programs/{id}", program.getId()).cookie(english)
                        .with(user(account.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(workout).contains("Home Starter", "Chair squat", "Leave 2–3 reps in reserve");
        assertThat(workout).containsPattern("<details class=\"exercise-detail program-exercise-card\">\\s*<summary><h4>Glute bridge</h4></summary>")
                .containsPattern("<details class=\"program-exercise exercise-video-disclosure\">\\s*<summary>How to perform it\\?</summary>")
                .containsPattern("(?s)<details class=\"program-exercise exercise-video-disclosure\">(?:(?!</details>|<iframe).)*</details>"
                        + "\\s*<section class=\"exercise-video\">\\s*<details class=\"exercise-video-disclosure\">")
                .contains("glute-bridge-start.webp", "glute-bridge-finish.webp", "Starting position", "End position")
                .doesNotContain("chair-squat-start.webp");
        mvc.perform(get("/images/exercises/glute-bridge-start.webp")).andExpect(status().isOk());
        String copyUrl = mvc.perform(post("/programs/{id}/copy", program.getId()).with(csrf())
                        .with(user(account.getUsername()).roles("MEMBER")))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        String copy = mvc.perform(get(copyUrl).with(user(account.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(copy).containsPattern("<details class=\"exercise-detail program-exercise-card\">\\s*<summary><h4>Глутеус мост</h4></summary>")
                .contains("glute-bridge-start.webp", "Начална позиция");
        assertThat(program.getName()).isEqualTo("Старт у дома 3 дни");

        var meal = new MealPlanTemplate();
        meal.setName("Хранителен режим за отслабване");
        meal.setDescription("Разнообразен седемдневен демонстрационен план с конкретни количества, приготвяне и ориентировъчни хранителни стойности.");
        meal.setGoal(FitnessGoal.FAT_LOSS);
        meal.setMeals("Пример");
        meal.setDisclaimer("Пример");
        meal.setDemoCode("fat-loss");
        try (var menu = getClass().getResourceAsStream("/content/menus/fat-loss.json")) {
            meal.setWeeklyMenu(new String(menu.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        }
        entityManager.persist(meal);

        String nutrition = mvc.perform(get("/meal-plans/{id}", meal.getId()).cookie(english)
                        .with(user(account.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(nutrition).contains("Weight Loss Meal Plan", "Skyr with apple, oats and flaxseed",
                "Mix the oats into the dairy base");
    }

    @Test void sharedThemeLoadsBeforeStylesAndIsAvailableToGuestsAndMembers() throws Exception {
        mvc.perform(get("/js/theme.js")).andExpect(status().isOk());

        for (String path : List.of("/", "/about", "/login", "/register")) {
            String html = mvc.perform(get(path)).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertThat(html).containsPattern("(?s)<script src=\"/js/theme.js\"></script>.*<link rel=\"stylesheet\"")
                    .containsOnlyOnce("data-theme-toggle");
        }

        String profile = mvc.perform(get("/profile").with(user(account.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(profile).contains("src=\"/js/theme.js\"").containsOnlyOnce("data-theme-toggle");
    }

    @Test void homeAndAboutOfferTheExpectedLinksForGuestsAndMembers() throws Exception {
        var guestHome = mvc.perform(get("/")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(guestHome).contains("href=\"/register?returnTo=", "href=\"/about\">Научи повече",
                "href=\"/support\"", "Откривай. Планирай. <span class=\"nav-slogan-accent\">Напредвай.");
        assertThat(guestHome.indexOf("id=\"home-slogan\"")).isBetween(guestHome.indexOf("<nav "), guestHome.indexOf("</nav>"));
        assertThat(guestHome).containsOnlyOnce("id=\"home-slogan\"");

        var memberHome = mvc.perform(get("/").with(user(account.getUsername()).roles("MEMBER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(memberHome).contains("href=\"/profile\">Започни сега").doesNotContain("href=\"/register");

        var about = mvc.perform(get("/about")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(about).contains("href=\"/about\">За FitPulse", "Персонализирано хранене", "Тренировъчни програми",
                "Библиотека с упражнения", "Личен прогрес", "Каталог с фитнес зали").doesNotContain("href=\"/suggest-gym");

        var gyms = mvc.perform(get("/gyms")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(gyms).doesNotContain("href=\"/suggest-gym");
    }

    @Test void gymCatalogLinksToGymDetailsAndTheExactMapsPlaceIsOnTheDetailsPage() throws Exception {
        City city = new City();
        city.setName("Catalog test city");
        city.setSlug("catalog-test-city");
        entityManager.persist(city);

        Gym photographed = catalogGym(city, "Zeta Gym", "fitness-flex-ruse");
        catalogGym(city, "Alpha Gym", "alpha-gym-test");
        GymPhoto gallery = new GymPhoto();
        gallery.setGym(photographed);
        gallery.setImageUrl("/images/gyms/fitness-flex-ruse-02.webp");
        gallery.setAltText("Zeta Gym");
        GymPhoto external = new GymPhoto();
        external.setGym(photographed);
        external.setImageUrl("https://example.com/external-photo.webp");
        external.setAltText("External Zeta Gym photo");
        external.setSortOrder(2);
        GymPhoto cover = new GymPhoto();
        cover.setGym(photographed);
        cover.setImageUrl("/images/gyms/fitness-flex-ruse-01.webp");
        cover.setAltText("Zeta Gym");
        cover.setCoverPhoto(true);
        cover.setSortOrder(1);
        photographed.getPhotos().addAll(List.of(gallery, cover, external));
        entityManager.persist(gallery);
        entityManager.persist(cover);
        entityManager.persist(external);
        entityManager.flush();

        var alphabetical = mvc.perform(get("/gyms").param("city", city.getSlug()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(alphabetical).containsPattern("(?s)Alpha Gym.*Zeta Gym")
                .contains("href=\"/gyms/fitness-flex-ruse\"")
                .contains("src=\"/images/gyms/fitness-flex-ruse-01.webp\"", "class=\"gym-wordmark\"")
                .doesNotContain("query_place_id=", "fitness-flex-ruse-02.webp");
        var details = mvc.perform(get("/gyms/fitness-flex-ruse"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(details).contains("query_place_id=ChIJsdlNI-9hrkARq0LNbzWxlCg", "target=\"_blank\"")
                .containsPattern("(?s)gym-detail-cover[^>]*fitness-flex-ruse-01.webp.*gym-detail-photos.*fitness-flex-ruse-02.webp")
                .contains("data-external-src=\"https://example.com/external-photo.webp\"")
                .doesNotContain(" src=\"https://example.com/external-photo.webp\"");
        mvc.perform(get("/images/gyms/fitness-flex-ruse-01.webp")).andExpect(status().isOk());
    }

    @Test void gymCommentsValidateEscapeAndEnforceDeletionRights() throws Exception {
        City city = new City();
        city.setName("Comment test city");
        city.setSlug("comment-test-city");
        entityManager.persist(city);
        Gym gym = catalogGym(city, "Comment test gym", "comment-test-gym");
        Review legacy = new Review();
        legacy.setGym(gym);
        legacy.setUser(account);
        legacy.setRating(5);
        legacy.setComment("Old gym review");
        entityManager.persist(legacy);

        User moderator = new User();
        moderator.setUsername("gym-comment-moderator");
        moderator.setEmail("gym-comment-moderator@example.com");
        moderator.setPassword(account.getPassword());
        moderator.setRole(UserRole.MEMBER);
        users.saveAndFlush(moderator);
        String page = "/gyms/" + gym.getSlug();
        String commentUrl = "/gyms/" + gym.getId() + "/review";
        String deleteUrl = "/reviews/" + legacy.getId() + "/delete";

        mvc.perform(get(page)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Old gym review")));
        mvc.perform(post(commentUrl).with(user(account.getUsername())).with(csrf()).param("comment", "  "))
                .andExpect(model().attributeHasFieldErrors("reviewForm", "comment"));
        mvc.perform(post(commentUrl).with(user(account.getUsername())).with(csrf()).param("comment", "<script>alert(1)</script>"))
                .andExpect(redirectedUrl(page + "#reviews"));
        mvc.perform(get(page)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;alert(1)&lt;/script&gt;")));
        assertThat(legacy.getRating()).isEqualTo(5);

        mvc.perform(post(deleteUrl).with(user(moderator.getUsername()).roles("MEMBER")).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post(deleteUrl).with(user(account.getUsername()).roles("MEMBER")).with(csrf()))
                .andExpect(redirectedUrl(page + "#reviews"));
        assertThat(legacy.getStatus()).isEqualTo(ContentStatus.ARCHIVED);
        assertThat(legacy.getRating()).isEqualTo(5);

        mvc.perform(post(commentUrl).with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("comment", "New gym comment"))
                .andExpect(redirectedUrl(page + "#reviews"));
        moderator.setRole(UserRole.ADMIN);
        users.saveAndFlush(moderator);
        mvc.perform(post(deleteUrl).with(user(moderator.getUsername()).roles("ADMIN")).with(csrf()))
                .andExpect(redirectedUrl(page + "#reviews"));
        mvc.perform(get(page)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Все още няма коментари.")));
        assertThat(legacy.getRating()).isEqualTo(5);
    }

    private Gym catalogGym(City city, String name, String slug) {
        Gym gym = new Gym();
        gym.setCity(city);
        gym.setName(name);
        gym.setSlug(slug);
        gym.setAddress("Catalog test address");
        gym.setDescription("Catalog test description");
        gym.setStatus(ContentStatus.PUBLISHED);
        entityManager.persist(gym);
        return gym;
    }

    @Test void registrationSignsInAndOpensOptionalProfile() throws Exception {
        String name="new"+UUID.randomUUID().toString().substring(0,10);
        var registration = post("/register").with(csrf()).param("username",name).param("email",name+"@example.com")
                .param("password","password123").param("confirmPassword","password123");
        mvc.perform(registration).andExpect(status().isOk()).andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "termsAccepted"));
        assertThat(users.findByUsername(name)).isEmpty();
        mvc.perform(registration.param("termsAccepted","true").param("termsVersion","obsolete"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("registerRequest", "termsAccepted"));
        assertThat(users.findByUsername(name)).isEmpty();
        String version = com.fitpulse.config.LegalTerms.VERSION;
        var result=mvc.perform(post("/register").with(csrf()).param("username",name).param("email",name+"@example.com")
                        .param("password","password123").param("confirmPassword","password123")
                        .param("termsAccepted","true").param("termsVersion",version))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/profile")).andReturn();
        var created = users.findByUsername(name).orElseThrow();
        assertThat(created.getAcceptedTermsVersion()).isEqualTo(version);
        assertThat(created.getTermsAcceptedAt()).isNotNull();
        var session=(MockHttpSession)result.getRequest().getSession();
        assertThat(session.getAttribute("FITPULSE_LOGIN_TOAST")).isNull();
        mvc.perform(get("/profile").session(session)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Добре дошъл във FitPulse!")));
        mvc.perform(get("/programs").session(session)).andExpect(status().isOk());
    }

    @Test void legalPagesAndSettingsArePublicAndEnglishFooterLinksAreRendered() throws Exception {
        var english = new jakarta.servlet.http.Cookie("fitpulse-language", "en");
        for (String path : List.of("/terms", "/privacy", "/cookies", "/login", "/register")) {
            var page = mvc.perform(get(path).cookie(english)).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertThat(page).contains("href=\"/terms\"", "href=\"/privacy\"", "href=\"/cookies\"",
                    "Privacy settings", "data-media-banner").doesNotContain("cdn.jsdelivr.net");
        }
        mvc.perform(get("/js/media-preferences.js")).andExpect(status().isOk());
        mvc.perform(get("/css/bootstrap-5.3.3.min.css")).andExpect(status().isOk());
    }
    @Test void ordinaryLoginOpensProfileAndToastIsShownOnce() throws Exception {
        account.setSex("MALE");account.setDisplayName("Иван Петров");users.saveAndFlush(account);
        var result=mvc.perform(post("/login").with(csrf()).param("username",account.getUsername()).param("password","password123").param("returnTo","/gyms")).andExpect(redirectedUrl("/profile")).andReturn();
        var session=(MockHttpSession)result.getRequest().getSession();
        mvc.perform(get("/gyms").session(session)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Добре дошъл отново!")));
        mvc.perform(get("/profile").session(session)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("id=\"login-toast\""))));
    }
    @Test void loginAcceptsEmailIgnoringCaseAndUsesExistingAccount() throws Exception {
        String email = account.getEmail().toUpperCase(Locale.ROOT);
        var result = mvc.perform(post("/login").with(csrf())
                        .param("username", email).param("password", "password123"))
                .andExpect(redirectedUrl("/profile")).andReturn();
        var session = (MockHttpSession) result.getRequest().getSession(false);
        var context = (org.springframework.security.core.context.SecurityContext)
                session.getAttribute("SPRING_SECURITY_CONTEXT");
        assertThat(context.getAuthentication().getName()).isEqualTo(account.getUsername());

        mvc.perform(post("/login").with(csrf()).param("username", email).param("password", "incorrect"))
                .andExpect(redirectedUrl("/login?error"));
        mvc.perform(get("/login").param("error", "")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Невалидни данни за вход")));
    }

    @Test void protectedSavedRequestWinsOverLoginOrigin() throws Exception {
        account.setWelcomeShown(false);users.saveAndFlush(account);
        var initial=mvc.perform(get("/progress")).andExpect(status().is3xxRedirection()).andReturn();
        var session=(MockHttpSession)initial.getRequest().getSession();
        mvc.perform(get("/login").session(session).param("returnTo","/gyms")).andExpect(status().isOk());
        mvc.perform(post("/login").session(session).with(csrf()).param("username",account.getUsername()).param("password","password123").param("returnTo","/gyms")).andExpect(redirectedUrl("/progress?continue"));
    }
    @Test void loginWithoutOriginOpensProfile() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username",account.getUsername()).param("password","password123")).andExpect(redirectedUrl("/profile"));
    }
    @Test void profileAndProgressShareOneWeightWithoutDuplicateOrLostNotes() throws Exception {
        authenticate();completeProfile();
        var today=records.findByUserAndRecordedAt(account,LocalDate.now()).orElseThrow();today.setNote("Запази бележката");records.saveAndFlush(today);
        var profile=userService.getCurrentUserProfileUpdateRequest();profile.setWeight(new BigDecimal("81"));userService.updateCurrentUserProfile(profile);
        assertThat(records.findAllByUserOrderByRecordedAtDesc(account)).hasSize(1);
        assertThat(records.findById(today.getId()).orElseThrow().getNote()).isEqualTo("Запази бележката");
        ProgressFormRequest form=new ProgressFormRequest();form.setRecordedAt(LocalDate.now());form.setWeight(new BigDecimal("82"));progress.update(today.getId(),form);
        assertThat(userService.getCurrentUserProfileUpdateRequest().getWeight()).isEqualByComparingTo("82");
        data.saveWeight(account,new BigDecimal("82"));assertThat(records.findAllByUserOrderByRecordedAtDesc(account)).hasSize(1);
    }
    @Test void missingPersonalDataIsSavedAndNextPlanOnlyAsksForActivity() throws Exception {
        var t=template();
        var result=mvc.perform(post("/meal-plans/"+t.getId()+"/copy").with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("sex","MALE").param("birthDate","1995-05-12").param("height","180").param("weight","80").param("activity","MODERATE")).andExpect(status().isOk()).andExpect(model().attributeExists("preview")).andReturn();
        assertThat(users.findById(account.getId()).orElseThrow().getBirthDate()).isEqualTo(LocalDate.of(1995,5,12));
        assertThat(data.weight(account)).isEqualByComparingTo("80");
        mvc.perform(get("/meal-plans/"+t.getId()+"/personalize").with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("name=\"birthDate\"")))).andExpect(content().string(org.hamcrest.Matchers.containsString("Промени")));
    }
    @Test void personalizedCopyKeepsOriginalNameAndInlineWeightUpsertsHistory() throws Exception {
        completeProfile();var t=template();
        mvc.perform(post("/meal-plans/"+t.getId()+"/weight").with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("weight","83").accept("application/json")).andExpect(status().isOk()).andExpect(jsonPath("$.weight").value(83));
        var result=mvc.perform(post("/meal-plans/"+t.getId()+"/copy").with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("activity","LOW")).andExpect(status().isOk()).andExpect(model().attributeExists("previewToken")).andReturn();
        String token=(String)result.getModelAndView().getModel().get("previewToken");
        mvc.perform(post("/meal-plans/"+t.getId()+"/copy").session((MockHttpSession)result.getRequest().getSession()).with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("activity","LOW").param("action","confirm").param("previewToken",token)).andExpect(status().is3xxRedirection());
        assertThat(personalMeals.findAllByUserOrderByCreatedAtDesc(account)).singleElement().extracting(UserMealPlan::getName).isEqualTo(t.getDisplayName());
        assertThat(records.findAllByUserOrderByRecordedAtDesc(account)).hasSize(1);
        assertThat(data.weight(account)).isEqualByComparingTo("83");
    }
    @Test void progressAndCatalogRenderSimplifiedViews() throws Exception {
        for(int i=0;i<7;i++){
            ProgressRecord weight=new ProgressRecord();weight.setUser(account);weight.setWeight(new BigDecimal("80"));weight.setRecordedAt(LocalDate.now().minusDays(i));records.save(weight);
            BodyMeasurement measurement=new BodyMeasurement();measurement.setUser(account);measurement.setType("waist");measurement.setRecordedAt(LocalDate.now().minusDays(i));measurement.setValue(new BigDecimal("90"));bodyMeasurements.save(measurement);
        }
        String html=mvc.perform(get("/progress").with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(java.util.regex.Pattern.compile("class=\"history-row\"").matcher(html).results().count()).isEqualTo(7);
        assertThat(java.util.regex.Pattern.compile("class=\"history-row history-row-body\"").matcher(html).results().count()).isEqualTo(7);
        assertThat(html).contains("data-show-more","history-table","Тегло (кг)","Стойност (см)");
        mvc.perform(get("/progress").with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("measurement-type-select"))).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("lift-chart"))));
        mvc.perform(get("/meal-plans").with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("meal-search"))));
    }

    @Autowired ProgramTemplateRepository programTemplates;
    @Autowired UserProgramRepository userPrograms;

    @Test void shortProfileFormPreservesAccountDetailsAndRejectsInvalidData() throws Exception {
        account.setPublicProfile(true);account.setDisplayName("Иван");users.saveAndFlush(account);
        mvc.perform(post("/profile/personal-data").with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("sex","MALE").param("birthDate","1995-05-12").param("height","180").param("weight","79").param("username","changed")).andExpect(redirectedUrl("/profile?updated"));
        User saved=users.findById(account.getId()).orElseThrow();
        assertThat(saved.getUsername()).isEqualTo(account.getUsername());assertThat(saved.isPublicProfile()).isTrue();assertThat(saved.getDisplayName()).isEqualTo("Иван");
        mvc.perform(post("/profile/personal-data").with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("birthDate",LocalDate.now().plusDays(1).toString()).param("weight","999")).andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("profileUpdateRequest","birthDate","weight"));
        assertThat(data.weight(account)).isEqualByComparingTo("79");
    }
    @Test void existingAddProgramActionKeepsItsCopyAndReadOnlyExerciseCards() throws Exception {
        ProgramTemplate t=new ProgramTemplate();t.setName("Готова програма");t.setDescription("Техника");t.setTrainingDays(3);t.setGoal(FitnessGoal.MUSCLE);t.setLocation(TrainingLocation.GYM);t.setExperience(ExperienceLevel.BEGINNER);programTemplates.saveAndFlush(t);
        mvc.perform(get("/programs/"+t.getId()).with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/programs/"+t.getId()+"/copy\"")));
        mvc.perform(post("/programs/"+t.getId()+"/copy").with(user(account.getUsername()).roles("MEMBER")).with(csrf())).andExpect(status().is3xxRedirection());
        UserProgram personal=userPrograms.findAllByUserOrderByCreatedAtDesc(account).getFirst();
        assertThat(personal.getName()).isEqualTo(t.getDisplayName());assertThat(personal.getItems()).isNotEmpty();
        mvc.perform(get("/programs/my/"+personal.getId()).with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("exercise-prescription"))).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Редактирай упражнението")))).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("/calendar"))));
    }
    @Test void newFlywayMigrationPreservesExistingUserRows(@org.junit.jupiter.api.io.TempDir java.nio.file.Path directory) throws Exception {
        String url="jdbc:h2:mem:ui-migration-"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1";
        try(var connection=java.sql.DriverManager.getConnection(url,"sa","");var statement=connection.createStatement()) {
            statement.execute("CREATE TABLE users (id INT PRIMARY KEY, username VARCHAR(30))");statement.execute("INSERT INTO users VALUES (1,'existing')");
        }
        try(var source=new org.springframework.core.io.ClassPathResource("db/migration/V29__profile_personal_data.sql").getInputStream()) {
            java.nio.file.Files.copy(source,directory.resolve("V29__profile_personal_data.sql"));
        }
        try(var source=new org.springframework.core.io.ClassPathResource("db/migration/V30__first_login_welcome.sql").getInputStream()) {
            java.nio.file.Files.copy(source,directory.resolve("V30__first_login_welcome.sql"));
        }
        var flyway=org.flywaydb.core.Flyway.configure().dataSource(url,"sa","").locations("filesystem:"+directory.toAbsolutePath()).baselineOnMigrate(true).baselineVersion("28").load();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);
        try(var connection=java.sql.DriverManager.getConnection(url,"sa","");var statement=connection.createStatement();var result=statement.executeQuery("SELECT username,sex,birth_date,height,welcome_shown FROM users WHERE id=1")) {
            assertThat(result.next()).isTrue();assertThat(result.getString("username")).isEqualTo("existing");assertThat(result.getString("sex")).isNull();assertThat(result.getDate("birth_date")).isNull();assertThat(result.getBoolean("welcome_shown")).isTrue();
        }
    }

    @Test void firstLoginIsPersistedAndOnlyRedirectsNewAccountsOnce() throws Exception {
        account.setWelcomeShown(false);users.saveAndFlush(account);
        var first=mvc.perform(post("/login").with(csrf()).param("username",account.getUsername()).param("password","password123").param("returnTo","/")).andExpect(redirectedUrl("/profile")).andReturn();
        assertThat(users.findById(account.getId()).orElseThrow().isWelcomeShown()).isTrue();
        var session=(MockHttpSession)first.getRequest().getSession();
        mvc.perform(get("/profile").session(session)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Попълни данните си, за да създаваме хранителни режими специално за теб.")));
        mvc.perform(get("/profile").session(session)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Добре дошъл във FitPulse!"))));
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(post("/login").with(csrf()).param("username",account.getUsername()).param("password","password123").param("returnTo","/gyms")).andExpect(redirectedUrl("/profile"));
    }
    @Test void completedProfileHidesPersonalCardAndEditFormKeepsValues() throws Exception {
        completeProfile();
        mvc.perform(get("/profile").with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("personal-data-panel"))));
        mvc.perform(get("/profile/edit").with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andExpect(model().attributeExists("profileUpdateRequest")).andExpect(content().string(org.hamcrest.Matchers.containsString("180")));
        mvc.perform(get("/programs").with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("data-filter=\"location\""))).andExpect(content().string(org.hamcrest.Matchers.containsString("program-filters.js"))).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Име или описание"))));
    }

    @Autowired ProgressDashboardService dashboard;
    @Test void periodControlsStatisticsAndBodyHistoryHasStableDateTieOrder() throws Exception {
        ProgressRecord old=new ProgressRecord();old.setUser(account);old.setRecordedAt(LocalDate.now().minusDays(60));old.setWeight(new BigDecimal("90"));records.saveAndFlush(old);
        ProgressRecord latest=new ProgressRecord();latest.setUser(account);latest.setRecordedAt(LocalDate.now());latest.setWeight(new BigDecimal("80"));records.saveAndFlush(latest);
        BodyMeasurement first=new BodyMeasurement();first.setUser(account);first.setType("waist");first.setRecordedAt(LocalDate.now());first.setValue(new BigDecimal("90"));bodyMeasurements.saveAndFlush(first);
        BodyMeasurement second=new BodyMeasurement();second.setUser(account);second.setType("chest");second.setRecordedAt(LocalDate.now());second.setValue(new BigDecimal("100"));bodyMeasurements.saveAndFlush(second);
        mvc.perform(get("/progress/data").param("period","30").with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andExpect(jsonPath("$.current").value(80)).andExpect(jsonPath("$.change").value(0)).andExpect(jsonPath("$.weights.length()").value(1));
        mvc.perform(get("/progress/data").param("period","90").with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andExpect(jsonPath("$.change").value(-10)).andExpect(jsonPath("$.weights.length()").value(2));
        authenticate();assertThat(dashboard.history()).extracting(ProgressDashboardService.Measurement::id).containsExactly(first.getId(),second.getId());
        assertThat(records.findAllByUserOrderByRecordedAtDesc(account)).hasSize(2);
    }

    @Test void returningGreetingUsesProfileSexOrNeutralText() throws Exception {
        account.setSex("FEMALE");users.saveAndFlush(account);
        mvc.perform(post("/login").with(csrf()).param("username",account.getUsername()).param("password","password123")).andExpect(redirectedUrl("/profile")).andExpect(request().sessionAttribute("FITPULSE_LOGIN_TOAST","Добре дошла отново!"));
        account.setSex(null);users.saveAndFlush(account);
        mvc.perform(post("/login").with(csrf()).param("username",account.getUsername()).param("password","password123")).andExpect(redirectedUrl("/profile")).andExpect(request().sessionAttribute("FITPULSE_LOGIN_TOAST","Здравей отново!"));
    }

    @Test void profileWeightAddsTodayAndKeepsEarlierMeasurementsAndDailyMetadata() throws Exception {
        authenticate();
        ProgressFormRequest old=new ProgressFormRequest();old.setRecordedAt(LocalDate.now().minusDays(1));old.setWeight(new BigDecimal("77"));old.setNote("Earlier measurement");progress.create(old);
        assertThat(userService.getCurrentUserProfileUpdateRequest().getWeight()).isEqualByComparingTo("77");
        var previous=records.findByUserAndRecordedAt(account,old.getRecordedAt()).orElseThrow();
        mvc.perform(post("/profile/edit").with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("username",account.getUsername()).param("email",account.getEmail()).param("weight","78")).andExpect(redirectedUrl("/profile?updated"));
        assertThat(records.findAllByUserOrderByRecordedAtDesc(account)).hasSize(2);
        assertThat(previous.getWeight()).isEqualByComparingTo("77");assertThat(previous.getNote()).isEqualTo("Earlier measurement");
        var today=records.findByUserAndRecordedAt(account,LocalDate.now()).orElseThrow();var todayId=today.getId();today.setNote("Keep today's note");today.setBodyFatPercentage(new BigDecimal("15"));records.saveAndFlush(today);
        authenticate();var form=userService.getCurrentUserProfileUpdateRequest();form.setWeight(new BigDecimal("79"));userService.updateCurrentUserProfile(form);userService.updateCurrentUserProfile(form);
        assertThat(records.findAllByUserOrderByRecordedAtDesc(account)).hasSize(2);
        var updated=records.findByUserAndRecordedAt(account,LocalDate.now()).orElseThrow();assertThat(updated.getId()).isEqualTo(todayId);assertThat(updated.getWeight()).isEqualByComparingTo("79");assertThat(updated.getNote()).isEqualTo("Keep today's note");assertThat(updated.getBodyFatPercentage()).isEqualByComparingTo("15");
    }
    @Test void historicalEditsAndDeletesRecomputeCurrentWeightByMeasurementDate() {
        authenticate();
        for(int days=3;days>=1;days--){ProgressFormRequest form=new ProgressFormRequest();form.setRecordedAt(LocalDate.now().minusDays(days));form.setWeight(BigDecimal.valueOf(79-days));progress.create(form);}
        var oldest=records.findByUserAndRecordedAt(account,LocalDate.now().minusDays(3)).orElseThrow();
        var middle=records.findByUserAndRecordedAt(account,LocalDate.now().minusDays(2)).orElseThrow();
        var latest=records.findByUserAndRecordedAt(account,LocalDate.now().minusDays(1)).orElseThrow();
        assertThat(userService.getCurrentUserProfileUpdateRequest().getWeight()).isEqualByComparingTo("78");
        ProgressFormRequest edit=new ProgressFormRequest();edit.setRecordedAt(LocalDate.now());edit.setWeight(new BigDecimal("80"));progress.update(oldest.getId(),edit);
        assertThat(userService.getCurrentUserProfileUpdateRequest().getWeight()).isEqualByComparingTo("80");
        edit.setRecordedAt(LocalDate.now().minusDays(4));progress.update(oldest.getId(),edit);
        assertThat(userService.getCurrentUserProfileUpdateRequest().getWeight()).isEqualByComparingTo("78");
        progress.delete(latest.getId());assertThat(userService.getCurrentUserProfileUpdateRequest().getWeight()).isEqualByComparingTo("77");
        progress.delete(middle.getId());assertThat(userService.getCurrentUserProfileUpdateRequest().getWeight()).isEqualByComparingTo("80");
        progress.delete(oldest.getId());assertThat(userService.getCurrentUserProfileUpdateRequest().getWeight()).isNull();
    }
    @Test void mealCalculationUsesSharedHistoryInsteadOfStalePostedWeight() throws Exception {
        completeProfile();var t=template();authenticate();
        var today=records.findByUserAndRecordedAt(account,LocalDate.now()).orElseThrow();
        ProgressFormRequest change=new ProgressFormRequest();change.setRecordedAt(LocalDate.now());change.setWeight(new BigDecimal("84"));progress.update(today.getId(),change);
        var result=mvc.perform(post("/meal-plans/"+t.getId()+"/copy").with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("weight","80").param("activity","LOW")).andExpect(status().isOk()).andExpect(model().attributeExists("preview")).andReturn();
        var resolved=(MealPersonalizationForm)result.getModelAndView().getModel().get("personalization");assertThat(resolved.getWeight()).isEqualByComparingTo("84");assertThat(data.weight(account)).isEqualByComparingTo("84");assertThat(records.findAllByUserOrderByRecordedAtDesc(account)).hasSize(1);
        String token=(String)result.getModelAndView().getModel().get("previewToken");
        authenticate();change.setWeight(new BigDecimal("85"));progress.update(today.getId(),change);
        mvc.perform(post("/meal-plans/"+t.getId()+"/copy").session((MockHttpSession)result.getRequest().getSession()).with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("weight","84").param("activity","LOW").param("action","confirm").param("previewToken",token)).andExpect(status().isOk()).andExpect(model().attributeExists("error"));
        assertThat(personalMeals.findAllByUserOrderByCreatedAtDesc(account)).isEmpty();assertThat(data.weight(account)).isEqualByComparingTo("85");
        var fresh=mvc.perform(post("/meal-plans/"+t.getId()+"/copy").with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("activity","LOW")).andExpect(model().attributeExists("preview")).andReturn();
        String freshToken=(String)fresh.getModelAndView().getModel().get("previewToken");
        authenticate();progress.delete(today.getId());
        mvc.perform(post("/meal-plans/"+t.getId()+"/copy").session((MockHttpSession)fresh.getRequest().getSession()).with(user(account.getUsername()).roles("MEMBER")).with(csrf()).param("weight","85").param("activity","LOW").param("action","confirm").param("previewToken",freshToken)).andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("personalization","weight"));
        assertThat(data.weight(account)).isNull();assertThat(personalMeals.findAllByUserOrderByCreatedAtDesc(account)).isEmpty();

    }

    @Autowired GymSuggestionRepository navSuggestions;
    @Autowired SupportMessageRepository navMessages;
    @Autowired GymDirectoryService navGyms;
    @Test void navigationShortcutsRespectGuestMemberAndAdminRoles() throws Exception {
        String guest=mvc.perform(get("/")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(guest).doesNotContain("data-profile-shortcut","data-admin-shortcut");
        String member=mvc.perform(get("/").with(user(account.getUsername()).roles("MEMBER"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(member).contains("data-profile-shortcut","fitpulse-menu-toggle").doesNotContain("data-admin-shortcut","nav-notification-dot");
        String admin=mvc.perform(get("/").with(user(account.getUsername()).roles("ADMIN"))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(admin).contains("data-admin-shortcut","data-profile-shortcut","fitpulse-menu-toggle").doesNotContain("nav-notification-dot");
        assertThat(admin.indexOf("data-admin-shortcut")).isLessThan(admin.indexOf("data-profile-shortcut"));
        assertThat(admin.indexOf("data-profile-shortcut")).isLessThan(admin.indexOf("fitpulse-menu-toggle"));
    }
    @Test void adminDotPersistsOnDashboardUntilSuggestionIsProcessed() throws Exception {
        GymSuggestion suggestion=new GymSuggestion();suggestion.setName("Test suggestion");suggestion.setCityName("Test city");suggestion.setAddress("Test address");suggestion.setDetails("Test details");suggestion.setStatus(ContentStatus.PENDING);navSuggestions.saveAndFlush(suggestion);
        mvc.perform(get("/").with(user(account.getUsername()).roles("ADMIN"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("nav-notification-dot")));
        mvc.perform(get("/admin").with(user(account.getUsername()).roles("ADMIN"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("nav-notification-dot")));
        assertThat(suggestion.getStatus()).isEqualTo(ContentStatus.PENDING);
        navGyms.suggestionStatus(suggestion.getId(),ContentStatus.REJECTED,"Reviewed in test");
        mvc.perform(get("/").with(user(account.getUsername()).roles("ADMIN"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("nav-notification-dot"))));
    }
    @Test void openingLastUnreadMessageClearsDotOnTheRenderedDetailsPage() throws Exception {
        var message=navMessages.saveAndFlush(new SupportMessage(SupportCategory.QUESTION,"Test question","Test description","test@example.com",null));
        mvc.perform(get("/admin").with(user(account.getUsername()).roles("ADMIN"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("nav-notification-dot")));
        assertThat(message.getStatus()).isEqualTo(SupportStatus.NEW);
        mvc.perform(get("/admin/messages/"+message.getId()).with(user(account.getUsername()).roles("ADMIN"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("nav-notification-dot"))));
        assertThat(navMessages.findById(message.getId()).orElseThrow().getStatus()).isEqualTo(SupportStatus.IN_PROGRESS);
    }
    @Test void navIndicatorIncludesReviewedCommentAndOpenReportStatusesOnlyForAdmins() {
        var advice=new com.fitpulse.controller.NavigationModelAdvice(null);
        var gyms=org.mockito.Mockito.mock(GymDirectoryService.class);var support=org.mockito.Mockito.mock(SupportService.class);
        org.springframework.test.util.ReflectionTestUtils.setField(advice,"gyms",gyms);org.springframework.test.util.ReflectionTestUtils.setField(advice,"support",support);
        var admin=UsernamePasswordAuthenticationToken.authenticated("admin",null,List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
        var member=UsernamePasswordAuthenticationToken.authenticated("member",null,List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_MEMBER")));
        assertThat(advice.adminTaskIndicator(member).getAsBoolean()).isFalse();org.mockito.Mockito.verifyNoInteractions(gyms,support);
        org.mockito.Mockito.when(gyms.newCommentCount()).thenReturn(1L);assertThat(advice.adminTaskIndicator(admin).getAsBoolean()).isTrue();
        org.mockito.Mockito.when(gyms.newCommentCount()).thenReturn(0L);org.mockito.Mockito.when(gyms.reportCount()).thenReturn(1L);assertThat(advice.adminTaskIndicator(admin).getAsBoolean()).isTrue();
        org.mockito.Mockito.when(gyms.reportCount()).thenReturn(0L);assertThat(advice.adminTaskIndicator(admin).getAsBoolean()).isFalse();
    }
}
