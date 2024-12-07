package me.autobot.lib.math;

/**
 * Used to keep track of time.
 * */
public class Clock {
    private long startTime;
    private long lastTriggeredTime;
    private boolean paused = false;
    private long pauseStartTime = 0;

    /**
     * Creates a new clock object.
     * */
    public Clock() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Check how long it has been since the clock was created.
     * @return The time elapsed in milliseconds.
     * */
    public long getTimeElapsed() {
        if (paused) {
            return pauseStartTime - startTime;
        }

        return System.currentTimeMillis() - startTime;
    }

    /**
     * Check if the given time has elapsed.
     * @param time The time to check.
     * @return Whether the time has elapsed.
     * */
    public boolean elapsed(long time) {
        return getTimeElapsed() >= time;
    }

    /**
     * Check if the given time has elapsed since the last time it was triggered.
     * @param time The time to check.
     * @return Whether the time has elapsed.
     * */
    public boolean elapsedSince(long time) {
        if (getTimeElapsed() - lastTriggeredTime >= time) {
            lastTriggeredTime = getTimeElapsed();
            return true;
        }

        return false;
    }

    /**
     * Sets if the timer is paused.
     * @param paused Whether the timer is paused.
     * */
    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            pauseStartTime = System.currentTimeMillis();
        } else {
            startTime += System.currentTimeMillis() - pauseStartTime;
            lastTriggeredTime += System.currentTimeMillis() - pauseStartTime;

            pauseStartTime = 0;
        }
    }
}
