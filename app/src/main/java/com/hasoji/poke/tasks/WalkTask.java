package com.hasoji.poke.tasks;

import com.hasoji.poke.api.PokemonClient;
import com.hasoji.poke.log.LocationLogger;
import com.hasoji.poke.utils.DistanceUtil;
import com.pokegoapi.api.PokemonGo;

import java.util.Random;

/**
 * Created by A on 2016/9/12.
 */
public class WalkTask implements BaseTask {
    private PokemonClient client;
    private double targetLat;
    private double targetLng;
    private String targetName;
    private WalkRunnable walkRunnable = new WalkRunnable();

    public WalkTask(PokemonClient paramPokemonClient) {
        this.client = paramPokemonClient;
    }

    private void startWalk(double paramDouble1, double paramDouble2) {
        this.walkRunnable.perLat = paramDouble1;
        this.walkRunnable.perLng = paramDouble2;
        this.client.post(this.walkRunnable);
    }

    public void pauseTask() {
    }

    public void resumeTask() {
    }

    public void stopTask() {
    }

    public void walkToLocation(String paramString, double paramDouble1, double paramDouble2) {
        this.targetName = paramString;
        this.targetLat = paramDouble1;
        this.targetLng = paramDouble2;
        this.client.removeRunnable(this.walkRunnable);
        this.client.post(new Runnable() {
            public void run() {
                double d1 = WalkTask.this.client.getGo().getLatitude();
                double d2 = WalkTask.this.client.getGo().getLongitude();
                if ((d1 == WalkTask.this.targetLat) && (d2 == WalkTask.this.targetLng))
                    return;
                double d3 = DistanceUtil.getDistance(WalkTask.this.targetLat, WalkTask.this.targetLng, d1, d2);
                if (d3 < 1.0D) {
                    WalkTask.this.client.setLocation(WalkTask.this.targetLat, WalkTask.this.targetLng);
                    return;
                }
                double d4 = (WalkTask.this.targetLat - d1) / d3;
                double d5 = (WalkTask.this.targetLng - d2) / d3;
                WalkTask.this.startWalk(d4, d5);
            }
        });
    }

    class WalkRunnable implements Runnable {
        double perLat;
        double perLng;

        WalkRunnable() {
        }

        public void run() {
            double d1 = WalkTask.this.client.getGo().getLatitude();
            double d2 = WalkTask.this.client.getGo().getLongitude();
            if ((d1 == WalkTask.this.targetLat) && (d2 == WalkTask.this.targetLng))
                return;
            if (!WalkTask.this.client.canWalk()) {
                WalkTask.this.client.postDelay(WalkTask.this.walkRunnable, 1500);
                return;
            }
            double d3 = DistanceUtil.getDistance(WalkTask.this.targetLat, WalkTask.this.targetLng, d1, d2);
            LocationLogger.getInstance().onTargetDistanceChanged(WalkTask.this.targetName, d3);
            if (d3 < 1.0D) {
                WalkTask.this.client.setLocation(WalkTask.this.targetLat, WalkTask.this.targetLng);
                return;
            }
            PokemonGo localPokemonGo = WalkTask.this.client.getGo();
            double d4 = localPokemonGo.getLatitude() + this.perLat;
            double d5 = localPokemonGo.getLongitude() + this.perLng;
            WalkTask.this.client.setLocation(d4, d5);
            int i = new Random().nextInt(400);
            WalkTask.this.client.postDelay(WalkTask.this.walkRunnable, i + 500);
        }
    }
}
