package me.autobot.lib.server;

import me.autobot.lib.server.topica.Topica;

/**
 * Server for the robot.
 * */
public class Server {
    private static int ws_port;
    private static int rest_port = 8081;

    private static WSServer ws_server;
    private static RESTServer rest_server;

    /**
     * Starts the server on the specified ports.
     * @param _ws_port The port for the websocket server.
     * @param _rest_port The port for the REST server.
     * */
    public static void start(int _ws_port, int _rest_port) {
        try {
            ws_server = new WSServer(_ws_port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ws_port = _ws_port;

        try {
            rest_server = new RESTServer(_rest_port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        rest_port = _rest_port;

        Topica.topic("/topica/rest_port", Topica.INT_TYPE).update(rest_port);
    }

    /**
     * Starts the server on ports (ws) 8080 and (rest) 8081.
     * */
    public static void start() {
        start(8080, 8081);
    }

    /**
     * Gets the port of the websocket server.
     * @return The port of the websocket server.
     * */
    public static int getWsPort() {
        return ws_port;
    }

    /**
     * Gets the port of the REST server.
     * @return The port of the REST server.
     * */
    public static int getRestPort() {
        return rest_port;
    }
}
