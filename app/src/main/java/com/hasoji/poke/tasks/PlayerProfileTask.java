package com.hasoji.poke.tasks;

import android.util.Log;

import com.hasoji.poke.api.PokemonClient;
import com.pokegoapi.api.inventory.Stats;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.exceptions.InvalidCurrencyException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import POGOProtos.Data.PlayerDataOuterClass;

/**
 * Created by A on 2016/9/12.
 */
public class PlayerProfileTask implements BaseTask {
    private PokemonClient client;
    private int level;
    private PlayerProfile playerProfile;
    private Runnable updatePlayerProfileRunnable = new Runnable() {
        public void run() {
            PlayerProfileTask.this.updatePlayerProfile();
            PlayerProfileTask.this.client.postDelay(PlayerProfileTask.this.updatePlayerProfileRunnable, 30000);
        }
    };

    public PlayerProfileTask(PokemonClient paramPokemonClient) {
        this.client = paramPokemonClient;
        init();
    }

    private PlayerDataOuterClass.PlayerData getPlayerData() {
        if (playerProfile != null)
            return playerProfile.getPlayerData();
        return null;
    }

    private void updatePlayerProfile() {
        try {
            if (this.playerProfile == null)
                this.playerProfile = this.client.getGo().getPlayerProfile();
            this.playerProfile.updateProfile();
            Stats localStats = this.playerProfile.getStats();
            if (localStats != null) {
                int i = localStats.getLevel();
                if ((this.level != 0) && (this.level != i)) {
                    this.level = i;
                    this.playerProfile.acceptLevelUpRewards(i);
                }
                long l1 = localStats.getExperience();
                long l2 = localStats.getNextLevelXp();
                long l3 = localStats.getPrevLevelXp();
                Log.e("Pokemon", getPlayerData().getUsername() + " Lv." + i + " ( " + (l1 - l3) + " , " + (l2 - l3) + " ) " + " " + 100L * (l1 - l3) / (l2 - l3) + "%");
            }
            return;
        } catch (RemoteServerException localRemoteServerException) {
            localRemoteServerException.printStackTrace();
        } catch (LoginFailedException localLoginFailedException) {
            localLoginFailedException.printStackTrace();
        }
    }

    public int getMaxItemStorage() {
        if (getPlayerData() != null)
            return getPlayerData().getMaxItemStorage();
        return 0;
    }

    public int getMaxPokemonStorage() {
        if (getPlayerData() != null)
            return getPlayerData().getMaxPokemonStorage();
        return 0;
    }

    public PlayerProfile getPlayerProfile() {
        return this.playerProfile;
    }

    public int getPlayerStardust() {
        if (getPlayerProfile() != null) {
            return getPlayerProfile().getCurrency(PlayerProfile.Currency.STARDUST);
        }
        return 0;
    }

    public void init() {
        getPlayerProfile();
        getPlayerData();
    }

    public void pauseTask() {
        this.client.removeRunnable(this.updatePlayerProfileRunnable);
    }

    public void refreshPlayerProfile() {
        this.client.removeRunnable(this.updatePlayerProfileRunnable);
        this.client.post(this.updatePlayerProfileRunnable);
    }

    public void resumeTask() {
        this.client.post(this.updatePlayerProfileRunnable);
    }

    public void stopTask() {
        pauseTask();
        this.playerProfile = null;
    }
}
