package com.gal.galend.domain;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "favorites_game")
public class FavoritesGame {
    @EmbeddedId
    private FavoritesGamePK id;

    @Column(name = "created_at", insertable = false, updatable = false,
            columnDefinition = "timestamp default current_timestamp")
    private Timestamp createdAt;

    public FavoritesGame() {}
    public FavoritesGame(FavoritesGamePK id){ this.id = id; }

    public FavoritesGamePK getId() { return id; }
    public void setId(FavoritesGamePK id) { this.id = id; }
    public Timestamp getCreatedAt() { return createdAt; }
}
