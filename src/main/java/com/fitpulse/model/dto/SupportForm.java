package com.fitpulse.model.dto;

import com.fitpulse.model.enums.SupportCategory;
import jakarta.validation.constraints.*;

public class SupportForm {
    @NotNull(message = "{ui.select.a.category.2}")
    private SupportCategory category;
    @NotBlank(message = "{ui.enter.a.subject}") @Size(max=150, message = "{ui.the.subject.can.contain.up.to.150}")
    private String subject;
    @NotBlank(message = "{ui.enter.a.description}") @Size(max=5000, message = "{ui.the.description.can.contain.up.to.5000}")
    private String description;
    @NotBlank(message = "{ui.enter.an.email.address.for.replies}") @Email(message = "{ui.enter.a.valid.email}")
    @Size(max=254, message = "{ui.the.email.address.is.too.long}")
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
