package com.hasoji.poke.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;

import com.hasoji.poke.PkgmApp;
import com.hasoji.poke.log.LocationLogger;
import com.hasoji.poke.log.Logger;
import com.hasoji.poke.tasks.HatchedEggsTask;
import com.hasoji.poke.tasks.LootPokestopTask;
import com.hasoji.poke.tasks.PlayerProfileTask;
import com.hasoji.poke.tasks.PokemonCatchTask;
import com.hasoji.poke.tasks.WalkTask;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.device.DeviceInfo;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.EggPokemon;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import okhttp3.OkHttpClient;

/**
 * Created by A on 2016/9/12.
 */
public class PokemonClient {
    private static double DEFAULT_LAT = 22.298165999999998D;
    private static double DEFAULT_LNG = 114.17401099999999D;
    private PokemonCatchTask catchTask;
    private double currentLat;
    private double currentLng;
    private PokemonGo go;
    private Handler handler;
    private HatchedEggsTask hatchedEggsTask;
    private InventoryManageTask inventoryManageTask;
    private OnLoginResultListener listener;
    private int loginType = 0;
    private LootPokestopTask lootTask;
    private String passwd;
    private PlayerProfileTask playerProfileTask;
    private HandlerThread thread;
    private String username;
    private WalkTask walkTask;

    public PokemonClient() {
        this.loginType = 0;
    }

    public PokemonClient(String paramString1, String paramString2) {
        this.username = paramString1;
        this.passwd = paramString2;
        this.loginType = 1;
    }

    private InventoryManageTask getInventoryManageTask() {
        if (this.inventoryManageTask == null)
            this.inventoryManageTask = new InventoryManageTask(this);
        return this.inventoryManageTask;
    }

    private PlayerProfileTask getPlayerProfileTask() {
        if (this.playerProfileTask == null)
            this.playerProfileTask = new PlayerProfileTask(this);
        return this.playerProfileTask;
    }

    public static void initLocation(Context paramContext) {
        SharedPreferences localSharedPreferences = paramContext.getSharedPreferences("cache_location", 0);
        String str1 = localSharedPreferences.getString("lat", null);
        String str2 = localSharedPreferences.getString("lng", null);
        if ((str1 != null) && (str2 != null)) {
            DEFAULT_LAT = Double.parseDouble(str1);
            DEFAULT_LNG = Double.parseDouble(str2);
        }
    }

    private void login() {
        Log.e("Pokemon", "login");
        if (this.go == null) {
            Log.e("Pokemon", "login go == null");
        }
        while (true) {
            OkHttpClient localOkHttpClient = new OkHttpClient();
            try {
                this.go = new PokemonGo(localOkHttpClient);
                DeviceInfo localDeviceInfo = new DeviceInfo();
                localDeviceInfo.setAndroidBoardName(Build.BRAND);
                localDeviceInfo.setAndroidBootloader(Build.BOOTLOADER);
                localDeviceInfo.setDeviceBrand(Build.BOARD);
                localDeviceInfo.setDeviceId(getAndroidId());
                localDeviceInfo.setDeviceModel(Build.MODEL);
                localDeviceInfo.setDeviceModelIdentifier(Build.PRODUCT);
                localDeviceInfo.setDeviceModelBoot("");
                localDeviceInfo.setHardwareManufacturer(Build.MANUFACTURER);
                localDeviceInfo.setHardwareModel(Build.MODEL);
                localDeviceInfo.setFirmwareBrand(Build.PRODUCT);
                localDeviceInfo.setFirmwareType(Build.TYPE);
                localDeviceInfo.setFirmwareTags(Build.TAGS);
                localDeviceInfo.setFirmwareFingerprint(Build.FINGERPRINT);
                this.go.setDeviceInfo(localDeviceInfo);
                resetLocation();
                go.login(new PtcCredentialProvider(localOkHttpClient, this.username, this.passwd));
                this.listener.onLoginFinish(null);
                getPlayerProfileTask().refreshPlayerProfile();
                startHatchedEggs();
                getInventoryManageTask().transferPokeBankBelowIV(0.85D, 1900);
                lootNearbyPokesTop();
                startSearchNearbyPokemon();
                return;
            }
            catch (Exception localException) {
                localException.printStackTrace();
                this.listener.onLoginFinish(localException);
            }
        }
    }

    private void reset() {
        this.inventoryManageTask.stopTask();
        this.inventoryManageTask = null;
        this.lootTask.stopTask();
        this.lootTask = null;
        this.walkTask.stopTask();
        this.walkTask = null;
        this.catchTask.stopTask();
        this.catchTask = null;
        this.go = null;
    }

    public static void saveCacheLocation(Context paramContext, double paramDouble1, double paramDouble2) {
        paramContext.getSharedPreferences("cache_location", 0).edit().putString("lat", paramDouble1 + "").putString("lng", paramDouble2 + "").apply();
    }

    public boolean canLootPokestop() {
        return canWalk();
    }

    public boolean canWalk() {
        if (this.catchTask == null);
        while (!this.catchTask.isInterruptWithOtherTask())
            return true;
        return false;
    }

    public void catchPokemonByLocation(Double paramDouble1, Double paramDouble2, String paramString) {
        if (this.catchTask == null)
            this.catchTask = new PokemonCatchTask(this) {
                public void onPokemonCatched(PokemonIdOuterClass.PokemonId paramAnonymousPokemonId) {
                    PokemonClient.this.getInventoryManageTask().transferPokeBankBelowIV(0.85D, 1900);
                }
            };
        this.catchTask.catchFarawayPokemon(paramDouble1.doubleValue(), paramDouble2.doubleValue(), paramString);
    }

    public String getAndroidId() {
        return Settings.Secure.getString(PkgmApp.INSTANCE.getContentResolver(), "android_id");
    }

    public Collection<EggPokemon> getEggBag() {
        return getInventoryManageTask().getEggBag();
    }

    public EggIncubator getEggIncubatorById(String paramString) {
        Iterator localIterator = this.go.getInventories().getIncubators().iterator();
        EggIncubator localEggIncubator = null;
        boolean bool;
        do {
            if (!localIterator.hasNext())
                break;
            localEggIncubator = (EggIncubator)localIterator.next();
            bool = paramString.equals(localEggIncubator.getId());
        } while (!bool);
        return localEggIncubator;
    }

    public PokemonGo getGo() {
        return this.go;
    }

    public Collection<Item> getItems() {
        return getInventoryManageTask().getItems();
    }

    public int getItemsCount() {
        return getInventoryManageTask().getItemsCount();
    }

    public int getMaxItemStorage() {
        return getPlayerProfileTask().getMaxItemStorage();
    }

    public int getMaxPokemonStorage() {
        return getPlayerProfileTask().getMaxPokemonStorage();
    }

    public PlayerProfile getPlayerProfile() {
        return this.playerProfileTask.getPlayerProfile();
    }

    public int getPlayerStardust() {
        return this.playerProfileTask.getPlayerStardust();
    }

    public List<Pokemon> getPokemons() {
        return getInventoryManageTask().getPokemons();
    }

    public String getTag() {
        if (this.loginType == 1)
            return this.username;
        return "google";
    }

    public void login(OnLoginResultListener paramOnLoginResultListener) {
        this.listener = paramOnLoginResultListener;
        this.handler.post(new Runnable() {
            public void run() {
                PokemonClient.this.login();
            }
        });
    }

    public void lootNearbyPokesTop() {
        if (this.lootTask == null)
            this.lootTask = new LootPokestopTask(this) {
                public void onPokestopLootFinish(HashMap<ItemIdOuterClass.ItemId, Integer> paramAnonymousHashMap) {
                    if (paramAnonymousHashMap == null) {
                        Logger.getInstance().appendLog("背包满了");
                        return;
                    }
                    Logger.getInstance().appendLog("Loot Pokestop Result : ");
                    Iterator localIterator = paramAnonymousHashMap.keySet().iterator();
                    while (localIterator.hasNext()) {
                        ItemIdOuterClass.ItemId localItemId = (ItemIdOuterClass.ItemId)localIterator.next();
                        int i = ((Integer)paramAnonymousHashMap.get(localItemId)).intValue();
                        Logger.getInstance().appendLog("\t\t\t\t" + localItemId + " x" + i);
                    }
                    PokemonClient.this.getInventoryManageTask().updateItemBag();
                    PokemonClient.this.getInventoryManageTask().checkItemBag();
                }
            };
        this.lootTask.lootNearbyPokesTop();
    }

    public void post(Runnable paramRunnable) {
        this.handler.postDelayed(paramRunnable, 2000L);
    }

    public void postDelay(Runnable paramRunnable, int paramInt) {
        this.handler.postDelayed(paramRunnable, paramInt);
    }

    public void removeItem(Item paramItem, int paramInt) {
        getInventoryManageTask().removeItem(paramItem.getItemId(), paramInt);
    }

    public void removeRunnable(Runnable paramRunnable) {
        if (this.handler != null)
            this.handler.removeCallbacks(paramRunnable);
    }

    public void resetLocation() {
        if ((this.currentLng == 0.0D) && (this.currentLat == 0.0D)) {
            setLocation(DEFAULT_LAT, DEFAULT_LNG);
            return;
        }
        setLocation(this.currentLat, this.currentLng);
    }

    public void setLocation(double paramDouble1, double paramDouble2) {
        this.currentLat = paramDouble1;
        this.currentLng = paramDouble2;
        this.go.setLocation(this.currentLat, this.currentLng, 0.0D);
        LocationLogger.getInstance().onLocationChanged(paramDouble1, paramDouble2);
        saveCacheLocation(PkgmApp.INSTANCE.getBaseContext(), this.currentLat, this.currentLng);
    }

    public void start(final Runnable paramRunnable) {
        if (this.thread == null) {
            this.thread = new HandlerThread(this.username, 10);
            this.thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread paramAnonymousThread, Throwable paramAnonymousThrowable) {
                    paramAnonymousThrowable.printStackTrace();
                    Log.e("Pokemon", "HandlerThread.uncaughtException , " + paramAnonymousThread.getName());
                    if (PokemonClient.this.username.equals(paramAnonymousThread.getName())) {
                        PokemonClient.this.reset();
                        if (paramRunnable != null)
                            paramRunnable.run();
                        paramAnonymousThrowable.printStackTrace();
                    }
                }
            });
            this.thread.start();
            this.handler = new Handler(this.thread.getLooper());
        }
    }

    public void startHatchedEggs() {
        if (this.hatchedEggsTask == null)
            this.hatchedEggsTask = new HatchedEggsTask(this);
        this.hatchedEggsTask.startCheckHatchedEggsTask();
    }

    public void startSearchNearbyPokemon() {
        if (this.catchTask == null)
            this.catchTask = new PokemonCatchTask(this) {
                public void onPokemonCatched(PokemonIdOuterClass.PokemonId paramAnonymousPokemonId) {
                    PokemonClient.this.getInventoryManageTask().transferPokeBankBelowIV(0.85D, 1900);
                }
            };
        this.catchTask.searchNearbyPokemon();
    }

    public void walkTo(String paramString, double paramDouble1, double paramDouble2) {
        if (this.walkTask == null)
            this.walkTask = new WalkTask(this);
        this.walkTask.walkToLocation(paramString, paramDouble1, paramDouble2);
    }

    public static abstract interface OnLoginResultListener {
        public abstract void onLoginFinish(Exception paramException);
    }
}
