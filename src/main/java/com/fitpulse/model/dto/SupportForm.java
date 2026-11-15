package com.fitpulse.model.dto;

import com.fitpulse.model.enums.SupportCategory;
import jakarta.validation.constraints.*;

public class SupportForm {
    @NotNull(message="Избери категория.")
    private SupportCategory category;
    @NotBlank(message="Въведи тема.") @Size(max=150, message="Темата може да е до 150 знака.")
    private String subject;
    @NotBlank(message="Въведи описание.") @Size(max=5000, message="Описанието може да е до 5000 знака.")
    private String description;
    @NotBlank(message="Въведи имейл за обратна връзка.") @Email(message="Въведи валиден имейл.")
    @Size(max=254, message="Имейлът е прекалено дълъг.")
    private String replyEmail;
    public SupportCategory getCategory() { return category; }
    public void setCategory(SupportCategory v) { category=v; }
    public String getSubject() { return subject; }
    public void setSubject(String v) { subject=v==null?null:v.strip(); }
    public String getDescription() { return description; }
    public void setDescription(String v) { description=v==null?null:v.strip(); }
    public String getReplyEmail() { return replyEmail; }
    public void setReplyEmail(String v) { replyEmail=v==null?null:v.strip(); }
}
