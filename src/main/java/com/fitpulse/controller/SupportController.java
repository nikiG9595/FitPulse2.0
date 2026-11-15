package com.fitpulse.controller;

import com.fitpulse.model.dto.SupportForm;
import com.fitpulse.model.enums.SupportCategory;
import com.fitpulse.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;

@Controller
@RequestMapping("/support")
public class SupportController {
    private final SupportService support;
    private final SupportRateLimiter limiter;
    private final UserService users;
    private final Validator validator;
    public SupportController(SupportService support,SupportRateLimiter limiter,UserService users,Validator validator){
        this.support=support;this.limiter=limiter;this.users=users;this.validator=validator;
    }
    private boolean member(Authentication auth){return auth!=null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);}
    @InitBinder("supportForm")
    void bind(WebDataBinder binder){binder.setAllowedFields("category","subject","description","replyEmail");}
    @ModelAttribute("categories") SupportCategory[] categories(){return SupportCategory.values();}
    @ModelAttribute("supportForm") SupportForm form(Authentication auth){
        var form=new SupportForm();if(member(auth))form.setReplyEmail(users.getCurrentUser().getEmail());return form;
    }
    @GetMapping
    public String page(@RequestParam(required=false) String rateLimited,@RequestParam(required=false) String uploadError,
                       HttpServletResponse response,Model model){
        if(rateLimited!=null){response.setStatus(429);response.setHeader("Retry-After","900");model.addAttribute("supportError","Достигна лимита от 5 опита за 15 минути. Опитай по-късно.");}
        else if(uploadError!=null)model.addAttribute("supportError","Прикачването не е прието. Избери една снимка JPG, PNG или WebP до 5 MB и попълни формата отново.");
        return "support";
    }
    @PostMapping(consumes="multipart/form-data")
    public String send(@ModelAttribute("supportForm") SupportForm form,BindingResult errors,
                       MultipartHttpServletRequest request,HttpServletResponse response,Authentication auth,
                       Model model,RedirectAttributes flash) {
        // The upload filter has already applied the shared IP limit before parsing the request.
        boolean allowed=!member(auth) || limiter.allow("account:"+auth.getName());
        if(!allowed){response.setStatus(429);response.setHeader("Retry-After","900");errors.reject("rate","Достигна лимита от 5 опита за 15 минути. Опитай по-късно.");}
        if(member(auth))form.setReplyEmail(users.getCurrentUser().getEmail());
        for(var violation:validator.validate(form))errors.rejectValue(violation.getPropertyPath().toString(),"invalid",violation.getMessage());
        if(request.getFiles("photo").size()>1 || request.getMultiFileMap().keySet().stream().anyMatch(k->!k.equals("photo")))
            errors.reject("photo","Може да прикачиш само една снимка.");
        if(errors.hasErrors())return "support";
        try {support.submit(form,request.getFile("photo"));}
        catch(IllegalArgumentException e){errors.reject("photo",e.getMessage());return "support";}
        catch(IOException e){errors.reject("storage","Снимката не може да се обработи. Опитай с друг файл или без снимка.");return "support";}
        flash.addFlashAttribute("supportSuccess","Съобщението е изпратено успешно. Благодарим за обратната връзка!");
        return "redirect:/support";
    }
}
