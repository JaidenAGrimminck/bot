package me.autobot.lib.hardware.ws;

import me.autobot.lib.hardware.Connection;
import me.autobot.lib.server.WSClient;

import java.util.ArrayList;

/**
 * A sensor connection to a websocket.
 * This means that another program is sending data to this program through a websocket.
 * This is useful for sensors that are not directly connected to the robot, or require an external program to run.
 * */
public class WSSensorConnection extends Connection {

    /**
     * List of IDs that are already in use.
     * This is used to ensure that IDs are unique.
     * */
    private static final ArrayList<Integer> ids = new ArrayList<>();

    /**
     * Creates a new websocket sensor connection.
     * */
    public WSSensorConnection() {
        super();

        // first, check if id is already in use
        if (ids.contains(getId())) {
            // if it is, we'll throw an error
            throw new IllegalArgumentException("ID " + getId() + " is already in use. In WSSensorConnections, IDs must be unique.");
        }

        WSClient.registerSensorConnection(this);
    }

    /**
     * Called when the websocket connection is updated.
     * @param data The data from the websocket connection. Array of integers from 0 to 255.
     * */
    public void onUpdate(int[] data) {
        // read data from the websockets
    }

    /**
     * Gets the WS ID of the sensor connection.
     * @return The WS ID of the sensor connection
     * */
    public int getId() {
        return 0x01;
    }
}
