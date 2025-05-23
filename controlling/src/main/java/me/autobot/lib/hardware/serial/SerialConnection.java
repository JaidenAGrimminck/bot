package me.autobot.lib.hardware.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import me.autobot.lib.hardware.Connection;

/**
 * Serial connection class.
 * */
public class SerialConnection extends Connection {
    /**
     * Verbose level for serial errors.
     * 0=none, 1=notify there's an error, 2=print stack trace
     * */
    public static int serialVerboseLevel = 2;

    private static boolean connectionDisabled = false;

    /**
     * Disables all serial connections.
     * */
    public static void disableConnections() {
        connectionDisabled = true;
    }

    private int baudRate;
    private String commPort;

    private SerialPort port;


    /**
     * Creates a new serial connection with the given baud rate and comm port.
     * @param baudRate The baud rate of the serial connection.
     * @param commPort The comm port of the serial connection.
     * */
    public SerialConnection(int baudRate, String commPort) {
        this.baudRate = baudRate;
        this.commPort = commPort;

        if (!connectionDisabled) {
            port = SerialPort.getCommPort(commPort);

            port.setComPortParameters(this.baudRate, Byte.SIZE, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                port.closePort();
            }));

            if (!open()) {
                throw new IllegalStateException("Could not open serial port " + commPort);
            }

            Thread otherThread = new Thread(() -> {
                port.addDataListener(new SerialListener());
            });

            otherThread.start();
        }
    }

    /**
     * Internal class used to listen for serial events.
     * */
    class SerialListener implements SerialPortDataListener {

        /**
         * Gets the events that this listener is listening for.
         * @return The events that this listener is listening for.
         * */
        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        /**
         * Handles the serial event.
         * @param event The event to handle.
         * */
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
                byte[] data = event.getReceivedData();
                onSerialData(data);
            }
        }
    }

    /**
     * Handles the serial data.
     * @param data The data to handle.
     * */
    protected void onSerialData(byte[] data) {
        // Override this method to handle serial data
    }

    /***
     * Writes the given data to the serial port.
     * @param data The data to write to the serial port.
     */
    protected void write(byte[] data) {
        if (connectionDisabled) {
            return;
        }

        port.writeBytes(data, data.length);
    }

    /**
     * Writes the given data to the serial port.
     * @param data The data to write to the serial port.
     * */
    public void write(int[] data) {
        byte[] bytes = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            bytes[i] = (byte) data[i];
        }

        write(bytes);
    }

    /**
     * Checks if the serial port is open.
     * @return True if the port is open, false otherwise.
     * */
    public boolean open() {
        if (connectionDisabled) {
            return false;
        }

        return port.openPort();
    }

    /**
     * Gets the comm port of the serial connection.
     * @return The comm port of the serial connection.
     * */
    public String getCommPort() {
        return commPort;
    }

    /**
     * Gets the baud rate of the serial connection.
     * @return The baud rate of the serial connection.
     */
    public int getBaudRate() {
        return baudRate;
    }
}
