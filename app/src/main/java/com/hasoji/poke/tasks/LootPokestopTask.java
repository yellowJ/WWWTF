package com.hasoji.poke.tasks;

import com.hasoji.poke.api.PokemonClient;
import com.hasoji.poke.log.Logger;
import com.hasoji.poke.utils.DistanceUtil;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import POGOProtos.Inventory.Item.ItemAwardOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Responses.FortSearchResponseOuterClass;

/**
 * Created by A on 2016/9/12.
 */
public abstract class LootPokestopTask implements BaseTask {
    private PokemonClient client;
    private Runnable lootRunnable = new Runnable() {
        public void run() {
            try {
                if (!LootPokestopTask.this.client.canLootPokestop()) {
                    LootPokestopTask.this.client.postDelay(LootPokestopTask.this.lootRunnable, 3000);
                    return;
                }
                if (LootPokestopTask.this.pokestopList.isEmpty()) {
                    MapObjects localMapObjects = LootPokestopTask.this.client.getGo().getMap().getMapObjects();
                    LootPokestopTask.this.pokestopList.addAll(localMapObjects.getPokestops());
                    Collections.sort(LootPokestopTask.this.pokestopList, new Comparator<Pokestop>() {
                        @Override
                        public int compare(Pokestop o1, Pokestop o2) {
                            return (int)(DistanceUtil.getDistance(o1.getLatitude(), o1.getLongitude(), LootPokestopTask.this.client.getGo().getLatitude(), LootPokestopTask.this.client.getGo().getLongitude()) - DistanceUtil.getDistance(o2.getLatitude(), o2.getLongitude(), LootPokestopTask.this.client.getGo().getLatitude(), LootPokestopTask.this.client.getGo().getLongitude()));
                        }
                    });
                }
                if (!LootPokestopTask.this.pokestopList.isEmpty()) {
                    int i = 0;
                    Iterator localIterator1 = LootPokestopTask.this.pokestopList.iterator();
                    while (localIterator1.hasNext()) {
                        Pokestop localPokestop = (Pokestop)localIterator1.next();
                        if (localPokestop.canLoot(true)) {
                            i = 1;
                            if (localPokestop.canLoot()) {
                                Logger.getInstance().appendLog("Loot Pokestop-" + localPokestop.getDetails().getName() + " (" + localPokestop.getLatitude() + ", " + localPokestop.getLongitude() + ")");
                                PokestopLootResult localPokestopLootResult = localPokestop.loot();
                                if (localPokestopLootResult.getResult() == FortSearchResponseOuterClass.FortSearchResponse.Result.INVENTORY_FULL) {
                                    LootPokestopTask.this.onPokestopLootFinish(null);
                                    return;
                                }
                                if (localPokestopLootResult.getResult() == FortSearchResponseOuterClass.FortSearchResponse.Result.SUCCESS) {
                                    List localList = localPokestopLootResult.getItemsAwarded();
                                    HashMap localHashMap = new HashMap();
                                    Iterator localIterator2 = localList.iterator();
                                    while (localIterator2.hasNext()) {
                                        ItemIdOuterClass.ItemId localItemId = ((ItemAwardOuterClass.ItemAward)localIterator2.next()).getItemId();
                                        if (localHashMap.containsKey(localItemId))
                                            localHashMap.put(localItemId, Integer.valueOf(1 + ((Integer)localHashMap.get(localItemId)).intValue()));
                                        else
                                            localHashMap.put(localItemId, Integer.valueOf(1));
                                    }
                                    LootPokestopTask.this.onPokestopLootFinish(localHashMap);
                                }
                                try {
                                    Thread.sleep(1000L);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                LootPokestopTask.this.client.walkTo(localPokestop.getDetails().getName(), localPokestop.getLatitude(), localPokestop.getLongitude());
                                LootPokestopTask.this.client.postDelay(LootPokestopTask.this.lootRunnable, 5000);
                            }
                        }

                    }
                    if (i == 0) {
                        LootPokestopTask.this.pokestopList.clear();
                        LootPokestopTask.this.client.postDelay(LootPokestopTask.this.lootRunnable, 5000);
                    }
                } else {
                    Logger.getInstance().appendLog("Pokestop count == " + LootPokestopTask.this.pokestopList.size());
                    LootPokestopTask.this.client.postDelay(LootPokestopTask.this.lootRunnable, 5000);
                }
            } catch (Exception localException) {
                localException.printStackTrace();
                Logger.getInstance().appendLog(localException.getMessage());
                LootPokestopTask.this.client.postDelay(LootPokestopTask.this.lootRunnable, 5000);
                return;
            }
        }
    };
    private List<Pokestop> pokestopList = new ArrayList();

    public LootPokestopTask(PokemonClient paramPokemonClient) {
        this.client = paramPokemonClient;
    }

    public void lootNearbyPokesTop() {
        this.client.removeRunnable(this.lootRunnable);
        this.client.post(this.lootRunnable);
    }

    public abstract void onPokestopLootFinish(HashMap<ItemIdOuterClass.ItemId, Integer> paramHashMap);

    public void pauseTask() {
    }

    public void resumeTask() {
    }

    public void stopTask() {
    }
}
