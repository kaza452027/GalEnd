package com.gal.galend.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FavoritesStudioPK implements Serializable {
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "studio_name", length = 128, nullable = false)
    private String studioName;

    public FavoritesStudioPK() {}
    public FavoritesStudioPK(String userId, String studioName) { this.userId = userId; this.studioName = studioName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStudioName() { return studioName; }
    public void setStudioName(String studioName) { this.studioName = studioName; }

    @Override public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof FavoritesStudioPK that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(studioName, that.studioName);
    }
    @Override public int hashCode(){ return Objects.hash(userId, studioName); }
}
