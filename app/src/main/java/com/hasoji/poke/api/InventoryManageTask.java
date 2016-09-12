package com.hasoji.poke.api;

import android.util.Log;

import com.hasoji.poke.log.Logger;
import com.hasoji.poke.tasks.BaseTask;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.ItemBag;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.pokemon.EggPokemon;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import POGOProtos.Inventory.Item.ItemIdOuterClass;

/**
 * Created by A on 2016/9/12.
 */
public class InventoryManageTask implements BaseTask {
    private static Map<ItemIdOuterClass.ItemId, Integer> itemNoNeedMap = new HashMap();
    private PokemonClient client;
    private Inventories inventories;

    static {
        itemNoNeedMap.put(ItemIdOuterClass.ItemId.ITEM_POTION, Integer.valueOf(0));
        itemNoNeedMap.put(ItemIdOuterClass.ItemId.ITEM_SUPER_POTION, Integer.valueOf(0));
        itemNoNeedMap.put(ItemIdOuterClass.ItemId.ITEM_HYPER_POTION, Integer.valueOf(0));
        itemNoNeedMap.put(ItemIdOuterClass.ItemId.ITEM_MAX_POTION, Integer.valueOf(60));
        itemNoNeedMap.put(ItemIdOuterClass.ItemId.ITEM_REVIVE, Integer.valueOf(0));
        itemNoNeedMap.put(ItemIdOuterClass.ItemId.ITEM_POKE_BALL, Integer.valueOf(80));
        itemNoNeedMap.put(ItemIdOuterClass.ItemId.ITEM_GREAT_BALL, Integer.valueOf(80));
        itemNoNeedMap.put(ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL, Integer.valueOf(80));
        itemNoNeedMap.put(ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY, Integer.valueOf(50));
    }

    public InventoryManageTask(PokemonClient paramPokemonClient) {
        this.client = paramPokemonClient;
        init();
    }

    private Inventories getInventories() {
        if (inventories == null) {
            inventories = client.getGo().getInventories();
        }
        if (inventories != null) {
            try {
                inventories.updateInventories();
            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (RemoteServerException e) {
                e.printStackTrace();
            }
        }
        return inventories;
    }

    private ItemBag getItemBag() {
        if (getInventories() != null)
            return getInventories().getItemBag();
        return null;
    }

    private PokeBank getPokeBank() {
        if (getInventories() != null)
            return getInventories().getPokebank();
        return null;
    }

    public void checkItemBag() {
        Collection<Item> localCollection = getItemBag().getItems();
        if (localCollection != null) {
            Iterator<Item> localIterator = localCollection.iterator();
            if (localIterator != null)
                while (localIterator.hasNext()) {
                    Item localItem = localIterator.next();
                    if (itemNoNeedMap.containsKey(localItem.getItemId())) {
                        int i = ((Integer)itemNoNeedMap.get(localItem.getItemId())).intValue();
                        if (i == 0)
                            removeAllItem(localItem);
                        else if (localItem.getCount() > i)
                            removeItem(localItem.getItemId(), localItem.getCount() - i);
                    }
                }
        }
    }

    public Set<EggPokemon> getEggBag() {
        if (getInventories() != null)
            return getInventories().getHatchery().getEggs();
        return null;
    }

    public Collection<Item> getItems() {
        if (getItemBag() != null)
            return getItemBag().getItems();
        return null;
    }

    public int getItemsCount() {
        if (getItemBag() != null)
            return getItemBag().getItemsCount();
        return 0;
    }

    public List<Pokemon> getPokemons() {
        if (getPokeBank() != null) {
            Log.e("Pokemon", getPokeBank() + "");
            return getPokeBank().getPokemons();
        }
        return null;
    }

    public void init() {
        getInventories();
    }

    public void pauseTask() {
    }

    public void removeAllItem(Item paramItem) {
        removeItem(paramItem.getItemId(), paramItem.getCount());
    }

    public void removeItem(ItemIdOuterClass.ItemId paramItemId, int paramInt) {
        if (paramInt <= 0)
            return;
        try {
            getItemBag().removeItem(paramItemId, paramInt);
            Logger.getInstance().appendLog("Remove " + paramItemId + " x" + paramInt);
            return;
        } catch (RemoteServerException localRemoteServerException) {
            localRemoteServerException.printStackTrace();
            return;
        }
        catch (LoginFailedException localLoginFailedException) {
            localLoginFailedException.printStackTrace();
        }
    }

    public void removeItemIfNeed(ItemIdOuterClass.ItemId paramItemId, int paramInt) {
        if (itemNoNeedMap.containsKey(paramItemId))
            removeItem(paramItemId, paramInt);
    }

    public void removePokemon(long paramLong) {
        removePokemon(getPokeBank().getPokemonById(Long.valueOf(paramLong)));
    }

    public void removePokemon(Pokemon paramPokemon) {
        getPokeBank().removePokemon(paramPokemon);
    }

    public void resumeTask() {
    }

    public void stopTask() {
    }

    public void transferPokeBankBelowIV(double paramDouble, int paramInt) {
        updateItemBag();
        if (getPokeBank() == null)
            return;
        List<Pokemon> localList = getPokeBank().getPokemons();
        if (localList == null)
            return;
        Iterator localIterator = localList.iterator();
        while (localIterator.hasNext()) {
            Pokemon localPokemon = (Pokemon) localIterator.next();
            if ((localPokemon.getIvRatio() < paramDouble) && (localPokemon.getCp() < paramInt))
                try {
                    localPokemon.transferPokemon();
                    Logger.getInstance().appendLog("Transfer Pokemon " + localPokemon.getPokemonId() + " , CP = " + localPokemon.getCp() + " ( " + (int) (100.0D * localPokemon.getIvRatio()) + "% )");
                } catch (LoginFailedException localLoginFailedException2) {
                    localLoginFailedException2.printStackTrace();
                } catch (RemoteServerException localRemoteServerException2) {
                    localRemoteServerException2.printStackTrace();
                }
        }
        try {
            getInventories().updateInventories();
            return;
        } catch (LoginFailedException localLoginFailedException1) {
            localLoginFailedException1.printStackTrace();
            return;
        } catch (RemoteServerException localRemoteServerException1) {
            localRemoteServerException1.printStackTrace();
        }
    }

    public void updateItemBag() {
        try {
            getInventories().updateInventories();
            return;
        } catch (LoginFailedException localLoginFailedException) {
            localLoginFailedException.printStackTrace();
            return;
        }
        catch (RemoteServerException localRemoteServerException) {
            localRemoteServerException.printStackTrace();
        }
    }
}
