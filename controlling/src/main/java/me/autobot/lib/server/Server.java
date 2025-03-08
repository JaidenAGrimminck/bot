package me.autobot.lib.server;

/**
 * Server for the robot.
 * */
public class Server {
    private static int ws_port;
    private static int rest_port;

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
    }

    /**
     * Starts the server on ports (ws) 8080 and (rest) 8081.
     * */
    public static void start() {
        start(8080, 8081);
    }
}
