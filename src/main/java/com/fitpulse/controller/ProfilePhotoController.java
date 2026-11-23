package com.fitpulse.controller;

import com.fitpulse.service.ProfilePhotos;
import com.fitpulse.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

@Controller
@RequestMapping("/profile/photo")
public class ProfilePhotoController {
    private final ProfilePhotos photos;
    private final UserService users;

    public ProfilePhotoController(ProfilePhotos photos, UserService users) {
        this.photos = photos;
        this.users = users;
    }

    @GetMapping
    public ResponseEntity<Resource> read() throws IOException {
        try {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).cacheControl(CacheControl.noStore())
                    .body(photos.read(users.getCurrentUser().getId()));
        } catch (NoSuchFileException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public String save(@RequestParam MultipartFile photo, RedirectAttributes flash) {
        try {
            photos.save(users.getCurrentUser().getId(), photo);
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("photoError", "profile.photo.invalid");
        } catch (IOException e) {
            flash.addFlashAttribute("photoError", "profile.photo.failed");
        }
        return "redirect:/profile/edit";
    }

    @PostMapping("/remove")
    public String remove(RedirectAttributes flash) {
        try {
            photos.remove(users.getCurrentUser().getId());
        } catch (IOException e) {
            flash.addFlashAttribute("photoError", "profile.photo.failed");
        }
        return "redirect:/profile/edit";
    }
}