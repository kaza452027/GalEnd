package com.gal.galend.domain;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "favorites_studio")
public class FavoritesStudio {
    @EmbeddedId
    private FavoritesStudioPK id;

    @Column(name = "created_at", insertable = false, updatable = false,
            columnDefinition = "timestamp default current_timestamp")
    private Timestamp createdAt;

    public FavoritesStudio() {}
    public FavoritesStudio(FavoritesStudioPK id){ this.id = id; }

    public FavoritesStudioPK getId() { return id; }
    public void setId(FavoritesStudioPK id) { this.id = id; }
    public Timestamp getCreatedAt() { return createdAt; }
}
