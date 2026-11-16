package com.fitpulse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Multipart resolution can fail before a controller is selected.
 */
@ControllerAdvice
public class SupportUploadExceptionAdvice {
    @ExceptionHandler(MultipartException.class)
    public String uploadError(MultipartException error, HttpServletRequest request, RedirectAttributes flash) {
        if (!request.getServletPath().equals("/support")) throw error;
        flash.addFlashAttribute("supportError", error instanceof MaxUploadSizeExceededException
                ? "Снимката може да е до 5 MB. Избери по-малък файл и попълни формата отново."
                : "Прикаченият файл не може да се прочете. Опитай отново с една снимка JPG, PNG или WebP.");
        return "redirect:/support";
    }
}
