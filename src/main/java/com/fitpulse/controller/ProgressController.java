package com.fitpulse.controller;

import com.fitpulse.exception.ProgressServiceException;
import com.fitpulse.model.dto.progress.ProgressFormRequest;
import com.fitpulse.model.dto.progress.ProgressResponse;
import com.fitpulse.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/progress")
public class ProgressController {
    private final ProgressService progressService;
    @org.springframework.beans.factory.annotation.Autowired private com.fitpulse.service.ProgressDashboardService dashboard;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public String history(Model model) {
        prepareDashboard(model);
        return "progress/list";
    }

    private void prepareDashboard(Model model){
        model.addAttribute("progressRecords",progressService.getCurrentUserProgress());
        model.addAttribute("measurementHistory",dashboard.history());model.addAttribute("measurementTypes",com.fitpulse.service.ProgressDashboardService.TYPES);
        if(!model.containsAttribute("bodyMeasurementForm"))model.addAttribute("bodyMeasurementForm",new com.fitpulse.model.dto.progress.BodyMeasurementForm());
    }
    @GetMapping("/data") @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<com.fitpulse.service.ProgressDashboardService.Dashboard> data(@org.springframework.web.bind.annotation.RequestParam(defaultValue="30")String period){return org.springframework.http.ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore()).body(dashboard.data(period));}
    @GetMapping("/measurements/{id}/edit") public String editMeasurement(@PathVariable Long id,Model model){model.addAttribute("bodyMeasurementForm",dashboard.form(id));prepareDashboard(model);return "progress/list";}
    @PostMapping("/measurements/save") public String saveMeasurement(@Valid @ModelAttribute com.fitpulse.model.dto.progress.BodyMeasurementForm bodyMeasurementForm,BindingResult errors,Model model,RedirectAttributes flash){
        if(errors.hasErrors()){prepareDashboard(model);return "progress/list";}
        try{dashboard.save(bodyMeasurementForm);}catch(com.fitpulse.exception.FitPulseException e){model.addAttribute("measurementError",e.getMessage());prepareDashboard(model);return "progress/list";}
        flash.addFlashAttribute("success","Обиколката е запазена.");return "redirect:/progress#body-sizes";
    }
    @PostMapping("/measurements/{id}/delete") public String deleteMeasurement(@PathVariable Long id,RedirectAttributes flash){dashboard.delete(id);flash.addFlashAttribute("success","Измерването е изтрито.");return "redirect:/progress#body-sizes";}
    @GetMapping("/create")
    public String create(Model model) {
        ProgressFormRequest request = new ProgressFormRequest();
        request.setRecordedAt(LocalDate.now());
        model.addAttribute("progressFormRequest", request);
        return "progress/form";
    }

    @PostMapping("/create")
    public String doCreate(@Valid @ModelAttribute ProgressFormRequest progressFormRequest,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "progress/form";
        }
        try {
            progressService.create(progressFormRequest);
            redirectAttributes.addFlashAttribute("success", "Записът за прогрес е добавен успешно.");
            return "redirect:/progress";
        } catch (ProgressServiceException exception) {
            model.addAttribute("globalError", exception.getMessage());
            return "progress/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        ProgressResponse progress = progressService.getCurrentUserProgress(id);
        ProgressFormRequest request = new ProgressFormRequest();
        request.setWeight(progress.weight());
        request.setBodyFatPercentage(progress.bodyFatPercentage());
        request.setRecordedAt(progress.recordedAt());
        request.setNote(progress.note());
        model.addAttribute("progressFormRequest", request);
        model.addAttribute("progressId", id);
        return "progress/form";
    }

    @PostMapping("/{id}/edit")
    public String doEdit(@PathVariable UUID id,
                         @Valid @ModelAttribute ProgressFormRequest progressFormRequest,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("progressId", id);
            return "progress/form";
        }
        try {
            progressService.update(id, progressFormRequest);
            redirectAttributes.addFlashAttribute("success", "Записът за прогрес е обновен успешно.");
            return "redirect:/progress";
        } catch (ProgressServiceException exception) {
            model.addAttribute("progressId", id);
            model.addAttribute("globalError", exception.getMessage());
            return "progress/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            progressService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Записът за прогрес е изтрит успешно.");
        } catch (ProgressServiceException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/progress";
    }
}
