package com.hasoji.poke.utils;

/**
 * Created by A on 2016/9/12.
 */
public class DistanceUtil {
    private static final double EARTH_RADIUS = 6378137.0D;

    public static double getDistance(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4) {
        double d1 = rad(paramDouble1);
        double d2 = rad(paramDouble3);
        double d3 = d1 - d2;
        double d4 = rad(paramDouble2) - rad(paramDouble4);
        return Math.round(10000.0D * (6378137.0D * (2.0D * Math.asin(Math.sqrt(Math.pow(Math.sin(d3 / 2.0D), 2.0D) + Math.cos(d1) * Math.cos(d2) * Math.pow(Math.sin(d4 / 2.0D), 2.0D)))))) / 10000L;
    }

    private static double rad(double paramDouble) {
        return 3.141592653589793D * paramDouble / 180.0D;
    }
}
