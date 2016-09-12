package com.hasoji.poke.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.hasoji.poke.api.ApiHolder;
import com.hasoji.poke.api.PokemonClient;

public class PokeTaskService extends Service {

    public static PokeTaskService INSTANCE;
    private Handler handler;

    public Handler getHandler() {
        return this.handler;
    }

    public void login(final String paramString1, final String paramString2, final PokemonClient.OnLoginResultListener paramOnLoginResultListener) {
        this.handler.post(new Runnable() {
            public void run() {
                ApiHolder.getInstance().setExceptionRunnable(new Runnable() {
                    public void run() {
                        PokeTaskService.this.handler.post(new Runnable() {
                            public void run() {
                                ApiHolder.getInstance().ptcLogin(paramString1, paramString2, paramOnLoginResultListener);
                            }
                        });
                    }
                });
                ApiHolder.getInstance().ptcLogin(paramString1, paramString2, paramOnLoginResultListener);
            }
        });
    }

    public IBinder onBind(Intent paramIntent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        super.onCreate();
        this.handler = new Handler();
        INSTANCE = this;
    }
}
