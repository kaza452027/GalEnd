package com.gal.galend.repo;

import com.gal.galend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, String> {
    Optional<User> findByEmailIgnoreCase(String email);
}
