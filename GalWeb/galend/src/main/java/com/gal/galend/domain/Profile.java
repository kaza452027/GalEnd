package com.gal.galend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="profiles")
@Getter @Setter @AllArgsConstructor @Builder
public class Profile {
    @Id
    @Column(name="user_id", length=36)
    private String userId;

    @Column(name="display_name", nullable=false, length=80)
    private String displayName;

    @Column(columnDefinition="text")
    private String bio;

    @Column(name="avatar_url", length=512)
    private String avatarUrl;


    @Column(name = "updated_at", nullable = false, updatable = true,
            insertable = false, columnDefinition = "timestamp default current_timestamp on update current_timestamp")
    private java.sql.Timestamp updatedAt;

    public Profile() {}

    // getters / setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public java.sql.Timestamp getUpdatedAt() { return updatedAt; }


}
