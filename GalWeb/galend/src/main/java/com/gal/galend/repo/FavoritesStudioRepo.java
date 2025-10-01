package com.gal.galend.repo;

import com.gal.galend.domain.FavoritesStudio;
import com.gal.galend.domain.FavoritesStudioPK;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FavoritesStudioRepo extends JpaRepository<FavoritesStudio, FavoritesStudioPK> {
    @Query("select f.id.studioName from FavoritesStudio f where f.id.userId = :uid")
    List<String> findNames(@Param("uid") String uid);
}
