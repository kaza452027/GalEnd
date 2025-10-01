package com.gal.galend.repo;

import com.gal.galend.domain.EmailToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailTokenRepo extends JpaRepository<EmailToken, Long> {
    Optional<EmailToken> findByToken(String token);
}
