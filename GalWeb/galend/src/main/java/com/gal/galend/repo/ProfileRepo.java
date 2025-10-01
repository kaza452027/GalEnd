package com.gal.galend.repo;

import com.gal.galend.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepo extends JpaRepository<Profile, String> { }
