package com.fitpulse.controller;

import com.fitpulse.model.enums.*;
import com.fitpulse.service.*;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** All handlers are inside the existing ADMIN-only namespace in SecurityConfig. */
@Controller @RequestMapping("/admin/messages")
public class AdminSupportController {
    private final SupportService support;
    private final SupportImageStorage storage;
    public AdminSupportController(SupportService support,SupportImageStorage storage){this.support=support;this.storage=storage;}
    @ModelAttribute("categories") SupportCategory[] categories(){return SupportCategory.values();}
    @ModelAttribute("statuses") SupportStatus[] statuses(){return SupportStatus.values();}
    @GetMapping
    public String list(@RequestParam(required=false) SupportCategory category,@RequestParam(required=false) SupportStatus status,
                       @RequestParam(defaultValue="0") int page,Model model){
        model.addAttribute("messages",support.list(category,status,page));model.addAttribute("category",category);model.addAttribute("status",status);
        return "admin/messages";
    }
    @GetMapping("/{id}")
    public String details(@PathVariable Long id,Model model){model.addAttribute("message",support.get(id));return "admin/message-details";}
    @PostMapping("/{id}/status")
    public String status(@PathVariable Long id,@RequestParam SupportStatus status,RedirectAttributes flash){
        support.status(id,status);flash.addFlashAttribute("success","Статусът е обновен.");return "redirect:/admin/messages/"+id;
    }
    @GetMapping("/{id}/attachment")
    public ResponseEntity<Resource> attachment(@PathVariable Long id){
        var file=storage.read(support.get(id).getAttachmentKey());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noStore()).header("X-Content-Type-Options","nosniff")
                .header("Content-Security-Policy","default-src 'none'; sandbox")
                .header(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=\"support-image.png\"").body(file);
    }
}
