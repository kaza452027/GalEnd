package com.gal.galend.repo;

import com.gal.galend.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findByUserId(String userId);
}
