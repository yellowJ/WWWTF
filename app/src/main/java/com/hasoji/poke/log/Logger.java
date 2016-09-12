package com.hasoji.poke.log;

/**
 * Created by A on 2016/9/12.
 */
public class Logger {
    private StringBuffer logBuffer;
    private LogChangeListener logChangeListener;

    public static Logger getInstance() {
        return SingleInstance.sInstance;
    }

    public void appendLog(String paramString) {
        appendLog(paramString, false);
    }

    public void appendLog(String paramString, boolean paramBoolean) {
        String str = paramString + "\n";
        if (this.logBuffer == null)
            this.logBuffer = new StringBuffer();
        if (this.logBuffer.length() > 1048576)
            this.logBuffer.delete(0, this.logBuffer.length());
        this.logBuffer.append(str);
        notifyLogAppend(str);
        if (paramBoolean)
            notifyLogChange();
    }

    public String getLog() {
        if (this.logBuffer == null)
            return "";
        return this.logBuffer.toString();
    }

    public void notifyLogAppend(String paramString) {
        if (this.logChangeListener != null)
            this.logChangeListener.onLogAppend(paramString);
    }

    public void notifyLogChange() {
        if (this.logChangeListener != null)
            this.logChangeListener.onLogChanged();
    }

    public void setLogChangeListener(LogChangeListener paramLogChangeListener) {
        this.logChangeListener = paramLogChangeListener;
    }

    public static abstract interface LogChangeListener {
        public abstract void onLogAppend(String paramString);

        public abstract void onLogChanged();
    }

    private Logger() {}

    private static class SingleInstance {
        private static Logger sInstance = new Logger();
    }
}
