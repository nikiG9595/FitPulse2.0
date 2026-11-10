package com.fitpulse.controller;

import com.fitpulse.exception.FitPulseException;
import com.fitpulse.model.dto.ProgramItemForm;
import com.fitpulse.model.entity.ProgramTemplateItem;
import com.fitpulse.repository.ProgramTemplateItemRepository;
import com.fitpulse.service.WellnessService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminProgramItemController {
    @org.springframework.beans.factory.annotation.Autowired private com.fitpulse.service.ExerciseLibraryService library;
    private final WellnessService wellness;
    private final ProgramTemplateItemRepository items;
    public AdminProgramItemController(WellnessService wellness, ProgramTemplateItemRepository items) {
        this.wellness = wellness; this.items = items;
    }
    @Transactional(readOnly=true)
    @GetMapping("/admin/programs/{id}/items")
    public String list(@PathVariable Long id, Model model) {
        var template = wellness.template(id);
        template.getItems().size();
        model.addAttribute("template", template);
        return "admin/program-items";
    }
    @Transactional
    @PostMapping("/admin/programs/{id}/items")
    public String save(@PathVariable Long id, @RequestParam(required=false) Long itemId,
                       @Valid @ModelAttribute ProgramItemForm form, BindingResult errors, RedirectAttributes attributes) {
        var template = wellness.template(id);
        if (errors.hasErrors()) {
            attributes.addFlashAttribute("error", "Проверете фокуса, подредбата на сесията (1–7), упражнението, сериите и почивката (15–600 сек.).");
            return "redirect:/admin/programs/" + id + "/items";
        }
        var item = itemId == null ? new ProgramTemplateItem() : owned(id, itemId);
        item.setTemplate(template); item.setDayNumber(form.getDayNumber()); item.setExerciseName(form.getExerciseName());item.setExercise(library.resolve(form.getExerciseName()));
        item.setSetsCount(form.getSetsCount()); item.setRepetitions(form.getRepetitions());item.setSessionTitle(form.getSessionTitle());item.setRestSeconds(form.getRestSeconds());item.setInstructions(form.getInstructions());
        if(form.getSessionTitle()!=null&&!form.getSessionTitle().isBlank())template.getItems().stream().filter(i->i.getDayNumber()==form.getDayNumber()).forEach(i->i.setSessionTitle(form.getSessionTitle().strip()));
        if (itemId == null) item.setSortOrder(template.getItems().size()+1);
        items.save(item);
        return "redirect:/admin/programs/" + id + "/items";
    }
    @Transactional
    @PostMapping("/admin/programs/{id}/items/{itemId}/delete")
    public String delete(@PathVariable Long id, @PathVariable Long itemId) {
        items.delete(owned(id,itemId));
        return "redirect:/admin/programs/" + id + "/items";
    }
    private ProgramTemplateItem owned(Long templateId, Long itemId) {
        return items.findById(itemId).filter(i -> i.getTemplate().getId().equals(templateId))
                .orElseThrow(() -> new FitPulseException("Упражнението не е намерено."));
    }
}
