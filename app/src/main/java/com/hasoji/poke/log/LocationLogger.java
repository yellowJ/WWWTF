package com.hasoji.poke.log;

/**
 * Created by A on 2016/9/12.
 */
public class LocationLogger {
    private LocationChangeListener locationChangeListener;

    public static LocationLogger getInstance() {
        return SingleInstance.sInstance;
    }

    public void onLocationChanged(double paramDouble1, double paramDouble2) {
        if (this.locationChangeListener != null)
            this.locationChangeListener.onLocationChanged(paramDouble1, paramDouble2);
    }

    public void onTargetDistanceChanged(String paramString, double paramDouble) {
        if (this.locationChangeListener != null)
            this.locationChangeListener.onTargetDistanceChanged(paramString, paramDouble);
    }

    public void setLocationChangeListener(LocationChangeListener paramLocationChangeListener) {
        this.locationChangeListener = paramLocationChangeListener;
    }

    public static abstract interface LocationChangeListener {
        public abstract void onLocationChanged(double paramDouble1, double paramDouble2);

        public abstract void onTargetDistanceChanged(String paramString, double paramDouble);
    }

    private LocationLogger() {}

    private static class SingleInstance {
        private static LocationLogger sInstance = new LocationLogger();
    }
}
