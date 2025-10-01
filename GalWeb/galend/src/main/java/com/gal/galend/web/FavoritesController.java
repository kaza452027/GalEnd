package com.gal.galend.web;

import com.gal.galend.service.FavoritesService;
import com.gal.galend.web.dto.ApiError;
import com.gal.galend.web.dto.IdReq;
import com.gal.galend.web.dto.NameReq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController @RequestMapping("/favorites")
public class FavoritesController {
    private final FavoritesService svc;
    public FavoritesController(FavoritesService s){ this.svc = s; }

    private String uid(Principal p){ return p!=null? p.getName(): null; }

    @GetMapping("/games")
    public ResponseEntity<?> games(Principal p){
        var u = uid(p); if(u==null) return ResponseEntity.status(401).body(new ApiError("未登录"));
        return ResponseEntity.ok(svc.listGameIds(u));
    }

    @PostMapping("/games")
    public ResponseEntity<?> addGame(@RequestBody IdReq req, Principal p){
        var u = uid(p); if(u==null) return ResponseEntity.status(401).body(new ApiError("未登录"));
        svc.addGame(u, req.id()); return ResponseEntity.ok().build();
    }

    @DeleteMapping("/games/{id}")
    public ResponseEntity<?> delGame(@PathVariable String id, Principal p){
        var u = uid(p); if(u==null) return ResponseEntity.status(401).body(new ApiError("未登录"));
        svc.delGame(u, id); return ResponseEntity.ok().build();
    }

    @GetMapping("/studios")
    public ResponseEntity<?> studios(Principal p){
        var u = uid(p); if(u==null) return ResponseEntity.status(401).body(new ApiError("未登录"));
        return ResponseEntity.ok(svc.listStudioNames(u));
    }

    @PostMapping("/studios")
    public ResponseEntity<?> addStudio(@RequestBody NameReq req, Principal p){
        var u = uid(p); if(u==null) return ResponseEntity.status(401).body(new ApiError("未登录"));
        svc.addStudio(u, req.name()); return ResponseEntity.ok().build();
    }

    @DeleteMapping("/studios/{name}")
    public ResponseEntity<?> delStudio(@PathVariable String name, Principal p){
        var u = uid(p); if(u==null) return ResponseEntity.status(401).body(new ApiError("未登录"));
        svc.delStudio(u, name); return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> all(Principal p) {
        var u = p != null ? p.getName() : null;
        if (u == null) {
            // 未登录返回 401，前端会忽略并保持本地收藏
            return ResponseEntity.status(401).body(new ApiError("未登录"));
        }
        return ResponseEntity.ok(Map.of(
                "games",   svc.listGameIds(u),
                "studios", svc.listStudioNames(u)
        ));
    }

}
