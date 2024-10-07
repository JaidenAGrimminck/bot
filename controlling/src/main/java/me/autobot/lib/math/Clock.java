package me.autobot.lib.math;

public class Clock {
    private long startTime;
    private long lastTriggeredTime;

    public Clock() {
        startTime = System.currentTimeMillis();
    }

    public long getTimeElapsed() {
        return System.currentTimeMillis() - startTime;
    }

    public boolean elapsed(long time) {
        return getTimeElapsed() >= time;
    }

    public boolean elapsedSince(long time) {
        if (getTimeElapsed() - lastTriggeredTime >= time) {
            lastTriggeredTime = getTimeElapsed();
            return true;
        }

        return false;
    }
}
