package com.hasoji.poke;

import android.app.Application;
import android.content.Intent;

import com.hasoji.poke.api.PokemonClient;
import com.hasoji.poke.services.PokeTaskService;

/**
 * Created by A on 2016/9/12.
 */
public class PkgmApp extends Application {

    public static PkgmApp INSTANCE;

    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        PokemonClient.initLocation(this);
        startService(new Intent(this, PokeTaskService.class));
    }
}
