package com.fitpulse.controller;

import com.fitpulse.model.dto.GymSuggestionForm;
import com.fitpulse.model.dto.ReviewForm;
import com.fitpulse.model.entity.Gym;
import com.fitpulse.service.GymDirectoryService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
public class GymDirectoryController {
    private static final Map<String, String> PLACE_IDS = Map.ofEntries(
            Map.entry("fitness-flex-ruse", "ChIJsdlNI-9hrkARq0LNbzWxlCg"),
            Map.entry("max-gym-ruse", "ChIJ-6LXRZVgrkARtlxlBIz-WnM"),
            Map.entry("ego-fitness-spa-ruse", "ChIJIwN4bM9grkARhQLzr6bjJ6Y"),
            Map.entry("fitness-sila-borisova-ruse", "ChIJqzeE779grkARlyLbJcTp7cw"),
            Map.entry("fitness-sila-vazrazhdane-ruse", "ChIJW7XppTVnrkARqfqH244I1Ac"),
            Map.entry("cosmo-gym-ruse", "ChIJn-7HKithrkARrZncows27zA"),
            Map.entry("atlet-fitness-ruse", "ChIJ1WthJ3FhrkARwZJQ0B1BjI0"),
            Map.entry("fitness-tri-set-ruse", "ChIJG9yWi0lnrkAR-zBudMTWhuU"),
            Map.entry("big-dogs-gym-ruse", "ChIJNcciYsRgrkARlCIitJnfvZk"),
            Map.entry("fitness-olimpia-ruse", "ChIJrYDxlSderkARjjQ-bU96444"),
            Map.entry("fitness-titan-ruse", "ChIJPxkaAcRgrkARvZ-M6HzvAb0"),
            Map.entry("rhea-sport-beauty-ruse", "ChIJOcElElRnrkARsQtOf7a5rmo")
    );
    private static final Map<String, String> MAP_LINKS = Map.of(
            "move-gym-ruse", "https://www.google.com/maps?cid=1263603533349454735",
            "onyx-sport-center-ruse", "https://maps.app.goo.gl/71oE3rfJ1KyDMH217"
    );
    // The two hotel gyms share their operator's map listing with the hotel.
    private static final Map<String, String> HOTEL_MAP_LINKS = Map.of(
            "adeo-sport-spa-ruse", "https://goo.gl/maps/WaX3749mtuvjviTd9",
            "millennia-24-7-ruse", "https://www.google.com/maps?cid=17229037103883905181"
    );
    private final GymDirectoryService service;

    public GymDirectoryController(GymDirectoryService s) {
        service = s;
    }

    @GetMapping("/gyms")
    public String list(@RequestParam(required = false) String city, @RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page, Model m) {
        var result = service.search(city, q, page);
        m.addAttribute("gyms", result);
        m.addAttribute("cities", service.activeCities());
        m.addAttribute("city", city);
        m.addAttribute("q", q);
        return "gyms/list";
    }

    private String mapsUrl(Gym gym) {
        String placeId = PLACE_IDS.get(gym.getSlug());
        if (placeId == null) return MAP_LINKS.getOrDefault(gym.getSlug(), HOTEL_MAP_LINKS.get(gym.getSlug()));
        String query = gym.getName() + ", " + gym.getAddress() + ", " + gym.getCity().getName();
        return "https://www.google.com/maps/search/?api=1&query="
                + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&query_place_id=" + placeId;
    }

    @GetMapping("/gyms/{slug}")
    public String details(@PathVariable String slug, Model m, Authentication a) {
        return detailsModel(service.published(slug), m, a);
    }

    private String detailsModel(Gym g, Model m, Authentication a) {
        boolean member = a != null && a.isAuthenticated() && !(a instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
        var own = member ? service.ownReview(g) : null;
        m.addAttribute("gym", g);
        m.addAttribute("reviews", service.reviews(g));
        m.addAttribute("mapsUrl", mapsUrl(g));
        m.addAttribute("hotelMap", HOTEL_MAP_LINKS.containsKey(g.getSlug()));
        m.addAttribute("ownReview", own);
        m.addAttribute("gymPhotos", g.getPhotos().stream().filter(p -> !com.fitpulse.service.GymContent.text(p.getImageUrl()).isBlank() && com.fitpulse.service.GymContent.imageUrl(p.getImageUrl())).toList());
        m.addAttribute("websiteUsable", !com.fitpulse.service.GymContent.text(g.getWebsiteUrl()).isBlank() && com.fitpulse.service.GymContent.webUrl(g.getWebsiteUrl()));
        if (!m.containsAttribute("reviewForm")) {
            ReviewForm form = new ReviewForm();
            if (own != null) {
                form.setComment(own.getComment());
            }
            m.addAttribute("reviewForm", form);
        }
        m.addAttribute("favorite", member && service.favorite(g));
        return "gyms/details";
    }

    @PostMapping("/gyms/{id}/favorite")
    public String favorite(@PathVariable Long id) {
        var gym = service.publishedById(id);
        service.toggleFavorite(id);
        return "redirect:/gyms/" + gym.getSlug();
    }

    @PostMapping("/gyms/{id}/review")
    public String review(@PathVariable Long id, @Valid @ModelAttribute ReviewForm reviewForm, BindingResult errors, Model model, Authentication authentication, RedirectAttributes ra) {
        var gym = service.publishedById(id);
        if (errors.hasErrors()) return detailsModel(gym, model, authentication);
        var saved = service.review(id, reviewForm);
        ra.addFlashAttribute("success", saved.getStatus() == com.fitpulse.model.enums.ContentStatus.PENDING
                ? "Коментарът е запазен и очаква преглед от администратор." : "Коментарът е запазен.");
        return "redirect:/gyms/" + gym.getSlug() + "#reviews";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteComment(@PathVariable Long id, RedirectAttributes ra) {
        String slug = service.deleteComment(id);
        ra.addFlashAttribute("success", "Коментарът е изтрит.");
        return "redirect:/gyms/" + slug + "#reviews";
    }

    @PostMapping("/reviews/{id}/report")
    public String report(@PathVariable Long id, @RequestParam String reason, RedirectAttributes ra) {
        String slug = service.report(id, reason);
        ra.addFlashAttribute("success", "Сигналът е изпратен за преглед от администратор.");
        return "redirect:/gyms/" + slug + "#reviews";
    }

    @GetMapping("/suggest-gym")
    public String suggest(Model m) {
        if (!m.containsAttribute("gymSuggestionForm")) m.addAttribute("gymSuggestionForm", new GymSuggestionForm());
        return "gyms/suggest";
    }

    @PostMapping("/suggest-gym")
    public String submit(@Valid @ModelAttribute GymSuggestionForm gymSuggestionForm, BindingResult errors, RedirectAttributes ra) {
        if (errors.hasErrors()) return "gyms/suggest";
        service.suggest(gymSuggestionForm);
        ra.addFlashAttribute("success", "Предложението е изпратено за одобрение.");
        return "redirect:/suggest-gym";
    }
}
