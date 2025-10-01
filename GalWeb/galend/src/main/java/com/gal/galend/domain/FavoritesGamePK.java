package com.gal.galend.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FavoritesGamePK implements Serializable {
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "game_id", length = 64, nullable = false)
    private String gameId;

    public FavoritesGamePK() {}
    public FavoritesGamePK(String userId, String gameId) { this.userId = userId; this.gameId = gameId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    @Override public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof FavoritesGamePK that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(gameId, that.gameId);
    }
    @Override public int hashCode(){ return Objects.hash(userId, gameId); }
}
