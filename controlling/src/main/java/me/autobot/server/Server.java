package me.autobot.server;

import fi.iki.elonen.NanoHTTPD;

public class Server extends NanoHTTPD {

    private static Server instance;

    public static void startServer(int port) {
        if (instance == null) {
            instance = new Server(port);
        }
    }

    public Server(int port) {
        super(port);
    }


}
