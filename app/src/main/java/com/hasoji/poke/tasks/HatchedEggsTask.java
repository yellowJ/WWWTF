package com.hasoji.poke.tasks;

import com.hasoji.poke.api.PokemonClient;
import com.hasoji.poke.log.Logger;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.pokemon.EggPokemon;
import com.pokegoapi.api.pokemon.HatchedEgg;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import POGOProtos.Networking.Responses.UseItemEggIncubatorResponseOuterClass;

/**
 * Created by A on 2016/9/12.
 */
public class HatchedEggsTask implements BaseTask {
    private Runnable checkHatchedEggsRunnable = new Runnable() {
        public void run() {
            try {
                HatchedEggsTask.this.checkHatchedEggs();
                HatchedEggsTask.this.client.postDelay(HatchedEggsTask.this.checkHatchedEggsRunnable, 100000);
                return;
            } catch (LoginFailedException localLoginFailedException) {
                localLoginFailedException.printStackTrace();
            } catch (RemoteServerException localRemoteServerException) {
                localRemoteServerException.printStackTrace();
            }
        }
    };
    private PokemonClient client;

    public HatchedEggsTask(PokemonClient paramPokemonClient) {
        this.client = paramPokemonClient;
    }

    private void checkHatchedEggs() throws LoginFailedException, RemoteServerException {
        Inventories localInventories = this.client.getGo().getInventories();
        localInventories.updateInventories(true);
        Iterator localIterator1 = localInventories.getHatchery().queryHatchedEggs().iterator();
        while (localIterator1.hasNext()) {
            HatchedEgg localHatchedEgg = (HatchedEgg) localIterator1.next();
            Pokemon localPokemon = localInventories.getPokebank().getPokemonById(localHatchedEgg.getId());
            String str = "+" + localHatchedEgg.getCandy() + " candy; +" + localHatchedEgg.getExperience() + " XP; +" + localHatchedEgg.getStardust() + " stardust";
            if (localPokemon == null)
                Logger.getInstance().appendLog("Hatched pokemon; " + str);
            else
                Logger.getInstance().appendLog(new StringBuilder().append("Hatched ").append(localPokemon.getPokemonId().name()).append(" with ").append(localPokemon.getCp()).append(" CP ").toString() + new StringBuilder().append("and ").append(localPokemon.getIvRatio()).append("% IV; ").append(str).toString());
        }
        List localList = localInventories.getIncubators();
        ArrayList<EggIncubator> localArrayList1 = new ArrayList();
        Iterator localIterator2 = localList.iterator();
        while (localIterator2.hasNext()) {
            EggIncubator localEggIncubator = (EggIncubator) localIterator2.next();
            if (!localEggIncubator.isInUse())
                localArrayList1.add(localEggIncubator);
        }
        if (localArrayList1.size() == 0)
            return;
        Set localSet = localInventories.getHatchery().getEggs();
        ArrayList<EggPokemon> localArrayList2 = new ArrayList();
        Iterator localIterator3 = localSet.iterator();
        while (localIterator3.hasNext()) {
            EggPokemon localEggPokemon2 = (EggPokemon) localIterator3.next();
            if (!localEggPokemon2.isIncubate())
                localArrayList2.add(localEggPokemon2);
        }
        if (localArrayList2.size() == 0)
            return;
        Collections.sort(localArrayList2, new Comparator<EggPokemon>() {
            @Override
            public int compare(EggPokemon o1, EggPokemon o2) {
                return (int) (o1.getEggKmWalkedTarget() - o2.getEggKmWalkedTarget());
            }
        });
        for (int i = 0; i < localArrayList2.size(); i++) {
            if (localArrayList1.size() > i) {
                EggPokemon localEggPokemon1 = localArrayList2.get(i);
                UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result localResult = localEggPokemon1.incubate(localArrayList1.get(i));
                if (localResult != UseItemEggIncubatorResponseOuterClass.UseItemEggIncubatorResponse.Result.SUCCESS) {
                    Logger.getInstance().appendLog("Failed to put egg in incubator; error: " + localResult);
                } else {
                    Logger.getInstance().appendLog("Put egg of " + localEggPokemon1.getEggKmWalkedTarget() + "km in unused incubator");
                }
            }
        }
    }

    public void pauseTask() {
    }

    public void resumeTask() {
    }

    public void startCheckHatchedEggsTask() {
        this.client.removeRunnable(this.checkHatchedEggsRunnable);
        this.client.post(this.checkHatchedEggsRunnable);
    }

    public void stopTask() {
    }
}
