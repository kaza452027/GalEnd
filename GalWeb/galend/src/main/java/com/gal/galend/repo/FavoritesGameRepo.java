package com.gal.galend.repo;

import com.gal.galend.domain.FavoritesGame;
import com.gal.galend.domain.FavoritesGamePK;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FavoritesGameRepo extends JpaRepository<FavoritesGame, FavoritesGamePK> {
    @Query("select f.id.gameId from FavoritesGame f where f.id.userId = :uid")
    List<String> findIds(@Param("uid") String uid);
}
