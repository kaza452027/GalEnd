package com.gal.galend.domain;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity @Table(name="email_otp")
public class EmailOtp {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false, length=36)
    private String userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private EmailOtpType type;

    @Column(nullable=false, length=6)
    private String code;

    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;

    @Column(name="used_at")
    private LocalDateTime usedAt;

    @Column(nullable=false)
    private int attempts;

    @Column(name="sent_at", nullable=false)
    private LocalDateTime sentAt;

    @Column(name="created_at", nullable=false, columnDefinition="timestamp default current_timestamp")
    private java.sql.Timestamp createdAt;

    // getters/setters
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getUserId(){return userId;} public void setUserId(String v){this.userId=v;}
    public EmailOtpType getType(){return type;} public void setType(EmailOtpType t){this.type=t;}
    public String getCode(){return code;} public void setCode(String c){this.code=c;}
    public LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(LocalDateTime t){this.expiresAt=t;}
    public LocalDateTime getUsedAt(){return usedAt;} public void setUsedAt(LocalDateTime t){this.usedAt=t;}
    public int getAttempts(){return attempts;} public void setAttempts(int a){this.attempts=a;}
    public LocalDateTime getSentAt(){return sentAt;} public void setSentAt(LocalDateTime t){this.sentAt=t;}
    public java.sql.Timestamp getCreatedAt(){return createdAt;} public void setCreatedAt(java.sql.Timestamp t){this.createdAt=t;}
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Timestamp.valueOf(LocalDateTime.now());
        }
        // 如果 attempts 是 Integer，顺手兜底：
        // if (attempts == null) attempts = 0;
    }
}
