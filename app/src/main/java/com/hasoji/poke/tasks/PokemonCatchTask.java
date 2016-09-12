package com.hasoji.poke.tasks;

import com.hasoji.poke.api.PokemonClient;
import com.hasoji.poke.log.Logger;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.settings.CatchOptions;
import com.pokegoapi.exceptions.EncounterFailedException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.NoSuchItemException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.Iterator;
import java.util.List;

import POGOProtos.Data.PokemonDataOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass;

/**
 * Created by A on 2016/9/12.
 */
public abstract class PokemonCatchTask implements BaseTask {
    private CatchFarawayPokemonRunnable catchFarawayPokemonRunnable = new CatchFarawayPokemonRunnable();
    private PokemonClient client;
    private Runnable searchNearbyPokemonRunnable = new Runnable() {
        public void run() {
            if (PokemonCatchTask.this.catchFarawayPokemonRunnable.isRunning)
                return;
            try {
                List localList = client.getGo().getMap().getCatchablePokemon();
                if (localList.size() > 0) {
                    Logger.getInstance().appendLog("Pokemon in area : " + localList.size());
                    Iterator localIterator = localList.iterator();
                    while (localIterator.hasNext()) {
                        CatchablePokemon localCatchablePokemon = (CatchablePokemon)localIterator.next();
                        PokemonCatchTask.this.catchPokemon(localCatchablePokemon, false);
                        Thread.sleep(1000L);
                    }
                } else {
                    Logger.getInstance().appendLog("no Pokemon in area, search again.");
                    PokemonCatchTask.this.client.postDelay(PokemonCatchTask.this.searchNearbyPokemonRunnable, 5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public PokemonCatchTask(PokemonClient paramPokemonClient) {
        this.client = paramPokemonClient;
    }

    private void catchPokemon(CatchablePokemon paramCatchablePokemon, boolean paramBoolean)
            throws RemoteServerException, NoSuchItemException, LoginFailedException, EncounterFailedException {
        EncounterResult localEncounterResult = paramCatchablePokemon.encounterPokemon();
        if (localEncounterResult.wasSuccessful()) {
            this.client.resetLocation();
            PokemonDataOuterClass.PokemonData localPokemonData = localEncounterResult.getPokemonData();
            double d = (localPokemonData.getIndividualAttack() + localPokemonData.getIndividualDefense() + localPokemonData.getIndividualStamina()) / 45.0D;
            String str = (int)(100.0D * d) + "%";
            Logger.getInstance().appendLog("Attempt to catch " + paramCatchablePokemon.getPokemonId() + " IV:" + str + " , CP:" + localPokemonData.getCp());
            int i = 0;
            if ((paramBoolean) || (d > 0.85D) || (localPokemonData.getCp() > 1900))
                i = 1;
            while (true) {
                if (i != 0)
                    paramCatchablePokemon.useItem(ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY);
                CatchResult localCatchResult = paramCatchablePokemon.catchPokemon();
                Logger.getInstance().appendLog("Attempt to catch : " + paramCatchablePokemon.getPokemonId() + " with result " + localCatchResult.getStatus());
                if ((localCatchResult.getStatus() != CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_SUCCESS) && (localCatchResult.getStatus() != CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_FLEE) && (localCatchResult.getStatus() != CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_ERROR))
                    break;
                if (localCatchResult.getStatus() == CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_SUCCESS) {
                    onPokemonCatched(paramCatchablePokemon.getPokemonId());
                    return;
                }
                try {
                    Thread.sleep(600L);
                } catch (InterruptedException localInterruptedException) {
                    localInterruptedException.printStackTrace();
                }
            }
        } else {
            Logger.getInstance().appendLog(localEncounterResult.getStatus().toString());
        }
    }

    public void catchFarawayPokemon(double paramDouble1, double paramDouble2, String paramString) {
        this.client.removeRunnable(this.catchFarawayPokemonRunnable);
        this.client.removeRunnable(this.searchNearbyPokemonRunnable);
        this.catchFarawayPokemonRunnable.lat = paramDouble1;
        this.catchFarawayPokemonRunnable.lng = paramDouble2;
        this.catchFarawayPokemonRunnable.pokemonId = paramString;
        this.catchFarawayPokemonRunnable.retryCount = 0;
        this.client.post(this.catchFarawayPokemonRunnable);
    }

    public boolean isInterruptWithOtherTask() {
        return this.catchFarawayPokemonRunnable.isRunning;
    }

    public abstract void onPokemonCatched(PokemonIdOuterClass.PokemonId paramPokemonId);

    public void pauseTask() {
    }

    public void resumeTask() {
    }

    public void searchNearbyPokemon() {
        this.client.resetLocation();
        this.client.removeRunnable(this.catchFarawayPokemonRunnable);
        this.client.removeRunnable(this.searchNearbyPokemonRunnable);
        this.client.post(this.searchNearbyPokemonRunnable);
    }

    public void stopTask() {
    }

    private class CatchFarawayPokemonRunnable implements Runnable {
        boolean isRunning;
        double lat;
        double lng;
        String pokemonId = "";
        int retryCount = 0;

        private CatchFarawayPokemonRunnable() {
        }

        public void run() {
            this.isRunning = true;
            try {
                PokemonCatchTask.this.client.getGo().setLocation(this.lat, this.lng, 0.0D);
                List<CatchablePokemon> localList = PokemonCatchTask.this.client.getGo().getMap().getCatchablePokemon();
                int i = 0;
                for (CatchablePokemon cp : localList) {
                    if (!cp.getPokemonId().toString().toLowerCase().startsWith(this.pokemonId.toLowerCase())) {
                        Logger.getInstance().appendLog("try catch Pokemon-[ " + cp.getPokemonId() + " ] in (" + this.lat + ", " + this.lng + ").");
                        PokemonCatchTask.this.catchPokemon(cp, true);
                        i = 1;
                    }
                }
                if ((i == 0) && (this.retryCount < 10)) {
                    this.retryCount = (1 + this.retryCount);
                    Logger.getInstance().appendLog("no Pokemon-[ " + this.pokemonId + " ] in [ " + PokemonCatchTask.this.client.getGo().getLatitude() + ", " + PokemonCatchTask.this.client.getGo().getLongitude() + " ], search again.");
                    PokemonCatchTask.this.client.postDelay(PokemonCatchTask.this.catchFarawayPokemonRunnable, 3000);
                    return;
                }
                PokemonCatchTask.this.client.resetLocation();
                this.isRunning = false;
                PokemonCatchTask.this.client.postDelay(new Runnable() {
                    public void run() {
                        PokemonCatchTask.this.searchNearbyPokemon();
                    }
                }, 3000);
                return;
            } catch (Exception localException) {
                PokemonCatchTask.this.client.resetLocation();
                localException.printStackTrace();
                this.isRunning = false;
            }
        }
    }
}
