package com.gal.galend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="refresh_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false, length=36)
    private String userId;

    @Column(nullable=false, unique=true, length=255)
    private String token;

    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;

    @Column(name="revoked_at")
    private LocalDateTime revokedAt;
}
