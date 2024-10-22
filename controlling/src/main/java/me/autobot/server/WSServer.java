package me.autobot.server;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

import java.io.IOException;

/**
 * A websocket server for the robot.
 * */
public class WSServer extends NanoWSD {

    private static WSServer instance;

    /**
     * The main method of the WSServer, only used for testing the server.
     * @param args The arguments of the main method.
     * */
    public static void main(String[] args) {
        try {
            new WSServer(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the WSServer on port 8080.
     * */
    public static void wsstart() {
        try {
            new WSServer(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the instance of the WSServer.
     * @return The instance of the WSServer.
     * */
    public static WSServer getInstance() {
        return instance;
    }

    /**
     * Starts the WSServer on the given port.
     * @param port The port to start the server on.
     * @throws IOException If the server cannot be started.
     * */
    public WSServer(int port) throws IOException {
        super(port);
        instance = this;
        System.out.println("WSServer started on port " + port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    /**
     * Opens a new WebSocket connection.
     * @param handshake The handshake of the WebSocket connection.
     * @return The WebSocket connection.
     * */
    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new WSClient(handshake);
    }

}
