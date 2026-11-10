package com.fitpulse.controller;

import com.fitpulse.exception.FitPulseException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FitPulseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleFitPulse(FitPulseException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error/custom-error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleTypeMismatch(Model model) {
        model.addAttribute("message", "Въведената стойност или идентификатор е невалиден.");
        return "error/custom-error";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDataIntegrityViolation(Model model) {
        model.addAttribute("message",
                "Записът е в употреба или вече съществува запис със същите данни. Обновете страницата и проверете въведеното.");
        return "error/custom-error";
    }
}
