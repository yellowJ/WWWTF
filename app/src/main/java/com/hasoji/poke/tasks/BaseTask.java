package com.hasoji.poke.tasks;

/**
 * Created by A on 2016/9/12.
 */
public interface BaseTask {
    public abstract void pauseTask();

    public abstract void resumeTask();

    public abstract void stopTask();
}
