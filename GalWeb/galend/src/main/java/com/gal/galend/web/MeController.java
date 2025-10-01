package com.gal.galend.web;

import com.gal.galend.domain.Profile;
import com.gal.galend.repo.ProfileRepo;
import com.gal.galend.repo.UserRepo;
import com.gal.galend.web.dto.ApiError;
import com.gal.galend.web.dto.ProfileReq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
public class MeController {
    private final UserRepo users; private final ProfileRepo profiles;
    public MeController(UserRepo u, ProfileRepo p){ this.users=u; this.profiles=p; }

    @GetMapping("/ping") public String ping(){ return "pong"; }

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal){
        if(principal==null) return ResponseEntity.status(401).body(new ApiError("未登录"));
        var uid = principal.getName(); // 在 JwtCookieFilter 里 subject=uid
        var u = users.findById(uid).orElse(null);
        var p = profiles.findById(uid).orElseGet(() -> {
            var np = new Profile();
            np.setUserId(uid);
            np.setDisplayName("User");
            np.setBio("");
            np.setAvatarUrl("");
            return profiles.save(np); // 立刻持久化，避免每次都走默认
        });
        if(u==null) return ResponseEntity.status(401).body(new ApiError("会话无效"));
        var profile = Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "displayName", p.getDisplayName(),
                "bio", p.getBio(),
                "avatarUrl", p.getAvatarUrl(),
                "emailVerified", u.isEmailVerified()
        );
        return ResponseEntity.ok(Map.of("profile", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> save(@RequestBody ProfileReq req, Principal principal){
        if(principal==null) return ResponseEntity.status(401).body(new ApiError("未登录"));
        var uid = principal.getName();
        var p = profiles.findById(uid).orElseGet(() -> {
            var np = new Profile();
            np.setUserId(uid);
            np.setDisplayName("User");
            np.setBio("");
            np.setAvatarUrl("");
            return profiles.save(np); // 立刻持久化，避免每次都走默认
        });
        p.setDisplayName(req.displayName()!=null? req.displayName() : "User");
        p.setBio(req.bio());
        p.setAvatarUrl(req.avatarUrl());
        profiles.save(p);
        return ResponseEntity.ok().build();
    }
}
