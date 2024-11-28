package me.autobot.lib.hardware;

/**
 * A connection to some device.
 * */
public class Connection {
    /**
     * Writes the given data to the connection.
     * @param data The data to write.
     */
    protected void write(byte[] data) {
        // Override this method to write data to the connection
    }
}
