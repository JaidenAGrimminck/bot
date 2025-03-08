package me.autobot.lib.tools;

import me.autobot.lib.server.WSClient;

/**
 * A runnable that can take arguments.
 * */
@Deprecated
public class RunnableWithArgs implements Runnable {

    /**
     * Creates a new RunnableWithArgs object.
     * @see Thread#run()
     * */
    public RunnableWithArgs() {

    }

    /**
     * Runs the runnable.
     * */
    public void run() {

    }

    /**
     * Runs the runnable with the given arguments.
     * @param args The arguments to pass to the runnable.
     * */
    public void run(Object... args) {

    }

    /**
     * Runs the runnable with the websocket client (very specific, I know).
     * @param client The client to pass to the runnable.
     * */
    public void run(WSClient client) {

    }

    /**
     * Runs the runnable with the websocket client (very specific, I know) and data.
     * @param client The client to pass to the runnable.
     * @param data The data to pass to the runnable.
     * */
    public void run(WSClient client, int[] data) {

    }
}
