package com.fitpulse.model.entity;

import com.fitpulse.model.enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="support_messages")
public class SupportMessage {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private SupportCategory category;
    @Column(nullable=false,length=150) private String subject;
    @Column(nullable=false,columnDefinition="TEXT") private String description;
    @Column(nullable=false,length=254) private String replyEmail;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private SupportStatus status=SupportStatus.NEW;
    @Column(length=40) private String attachmentKey;
    @Column(nullable=false) private LocalDateTime createdAt;
    @Column(nullable=false) private LocalDateTime updatedAt;
    protected SupportMessage() {}
    public SupportMessage(SupportCategory category,String subject,String description,String email,String attachmentKey) {
        this.category=category;this.subject=subject;this.description=description;this.replyEmail=email;
        this.attachmentKey=attachmentKey;createdAt=LocalDateTime.now();updatedAt=createdAt;
    }
    public Long getId(){return id;}
    public SupportCategory getCategory(){return category;}
    public String getSubject(){return subject;}
    public String getDescription(){return description;}
    public String getReplyEmail(){return replyEmail;}
    public SupportStatus getStatus(){return status;}
    public String getAttachmentKey(){return attachmentKey;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
    public void setStatus(SupportStatus value){status=value;updatedAt=LocalDateTime.now();}
}
