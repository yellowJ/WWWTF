package com.hasoji.poke.api;

import android.util.Log;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by A on 2016/9/12.
 */
public class ApiHolder {
    private Runnable exceptionRunnable;
    private Map<String, PokemonClient> pkgMap = new HashMap();

    public static ApiHolder getInstance() {
        return SingleInstance.sInstance;
    }

    public PokemonClient googleLoginGoApi(String paramString)
            throws LoginFailedException, RemoteServerException {
        if (!this.pkgMap.containsKey(paramString)) {
            PokemonClient localPokemonClient = new PokemonClient();
            this.pkgMap.put(paramString, localPokemonClient);
            localPokemonClient.start(this.exceptionRunnable);
            return localPokemonClient;
        }
        return (PokemonClient)this.pkgMap.get(paramString);
    }

    public PokemonClient ptcLogin(final String paramString1, String paramString2, PokemonClient.OnLoginResultListener paramOnLoginResultListener) {
        if (!this.pkgMap.containsKey(paramString1)) {
            PokemonClient localPokemonClient = new PokemonClient(paramString1, paramString2);
            this.pkgMap.put(paramString1, localPokemonClient);
            localPokemonClient.start(new Runnable() {
                public void run() {
                    ApiHolder.this.pkgMap.remove(paramString1);
                    if (ApiHolder.this.exceptionRunnable != null)
                        ApiHolder.this.exceptionRunnable.run();
                }
            });
            localPokemonClient.login(paramOnLoginResultListener);
            Log.e("PokemonGoMo.ptcLogin", localPokemonClient.toString());
            return localPokemonClient;
        }
        return (PokemonClient)this.pkgMap.get(paramString1);
    }

    public void removeKey(String paramString) {
        this.pkgMap.remove(paramString);
    }

    public void setExceptionRunnable(Runnable paramRunnable) {
        this.exceptionRunnable = paramRunnable;
    }

    public PokemonClient sharePokemonClient(String paramString) {
        return (PokemonClient)this.pkgMap.get(paramString);
    }

    public PokemonClient shareTopClient() {
        if (this.pkgMap.size() > 0)
            return (PokemonClient)this.pkgMap.values().iterator().next();
        return null;
    }

    private ApiHolder() {

    }

    private static class SingleInstance {
        private static ApiHolder sInstance = new ApiHolder();
    }
}
