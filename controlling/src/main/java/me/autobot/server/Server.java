package me.autobot.sim.server;

import fi.iki.elonen.NanoHTTPD;

public class SimServer extends NanoHTTPD {

    private static SimServer instance;

    public static void startServer(int port) {
        if (instance == null) {
            instance = new SimServer(port);
        }
    }

    public SimServer(int port) {
        super(port);
    }


}
