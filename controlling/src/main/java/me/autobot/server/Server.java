package me.autobot.server;

import fi.iki.elonen.NanoHTTPD;

/**
 * A nanoHTTPD server that can be used to serve a webpage.
 * Is used for the REST API. See the WSServer for the websocket server.
 * @see WSServer
 * */
public class Server extends NanoHTTPD {

    private static Server instance;

    /**
     * Starts the NanoHTTPD server on the given port.
     * @param port The port to start the server on.
     * */
    public static void startServer(int port) {
        if (instance == null) {
            instance = new Server(port);
        }
    }

    /**
     * Starts the NanoHTTPD server on the given port
     * @param port The port to start the server on.
     * */
    public Server(int port) {
        super(port);
    }
}
