package com.gal.galend.service;

import com.gal.galend.domain.FavoritesGame;
import com.gal.galend.domain.FavoritesGamePK;
import com.gal.galend.domain.FavoritesStudio;
import com.gal.galend.domain.FavoritesStudioPK;
import com.gal.galend.repo.FavoritesGameRepo;
import com.gal.galend.repo.FavoritesStudioRepo;
import org.springframework.stereotype.Service;

@Service
public class FavoritesService {
    private final FavoritesGameRepo gameRepo;
    private final FavoritesStudioRepo studioRepo;

    public FavoritesService(FavoritesGameRepo gameRepo, FavoritesStudioRepo studioRepo){
        this.gameRepo = gameRepo; this.studioRepo = studioRepo;
    }

    public void addGame(String uid, String gid){
        var pk = new FavoritesGamePK(uid, gid);
        var e = new FavoritesGame(pk);
        gameRepo.save(e);
    }

    public void delGame(String uid, String gid){
        gameRepo.deleteById(new FavoritesGamePK(uid, gid));
    }

    public void addStudio(String uid, String name){
        var pk = new FavoritesStudioPK(uid, name);
        var e = new FavoritesStudio(pk);
        studioRepo.save(e);
    }

    public void delStudio(String uid, String name){
        studioRepo.deleteById(new FavoritesStudioPK(uid, name));
    }

    public java.util.List<String> listGameIds(String uid){ return gameRepo.findIds(uid); }
    public java.util.List<String> listStudioNames(String uid){ return studioRepo.findNames(uid); }
}


