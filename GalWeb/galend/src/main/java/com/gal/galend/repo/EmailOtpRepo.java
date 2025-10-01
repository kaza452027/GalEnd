package com.gal.galend.repo;

import com.gal.galend.domain.EmailOtp;
import com.gal.galend.domain.EmailOtpType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

public interface EmailOtpRepo extends JpaRepository<EmailOtp, Long> {

    @Query("""
    select o from EmailOtp o
     where o.userId=:uid and o.type=:type
       and (o.usedAt is null) and (o.expiresAt >= :now)
     order by o.id desc
  """)
    List<EmailOtp> findActive(@Param("uid") String userId,
                              @Param("type") EmailOtpType type,
                              @Param("now") LocalDateTime now);

    @Modifying
    @Query("update EmailOtp o set o.usedAt=:ts where o.id=:id")
    int markUsed(@Param("id") Long id, @Param("ts") LocalDateTime usedAt);

    @Modifying
    @Query("""
       update EmailOtp o
          set o.usedAt = :now
        where o.userId = :userId and o.type = :type
          and o.expiresAt > :now and o.usedAt is null
    """)
    int invalidateActive(@Param("userId") String userId,
                         @Param("type") EmailOtpType type,
                         @Param("now") LocalDateTime now);
}
