package me.autobot.server;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import me.autobot.lib.hardware.ws.WSSensorConnection;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.tools.RunnableWithArgs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TimerTask;

import static me.autobot.lib.math.Mathf.allPos;

/**
 * A websocket client class that is used to communicate with the client.
 * */
public class WSClient extends NanoWSD.WebSocket {
    /**
     * ClientType is an enum representing the type of client that is connected to the server.
     * It can be a speaker, listener, or passive.
     * */
    public static enum ClientType {
        /**
         * Connection is supposed to only be used for sending data.
         * */
        Speaker,
        /**
         * Connection is supposed to only be used for reading data.
         * */
        Listener,
        /**
         * Connection can be used for both sending and receiving data.
         * */
        Passive //passive, as in can be either speaker or listener
    }

    /**
     * An enum representing the error codes that can be sent to the client.
     * */
    enum Error {
        /**
         * 0xDF - An internal error occurred.
         * */
        InternalError(0xDF, "There was an internal error that occurred while executing this task"),
        /**
         * 0xE0 - Timeout in initiation.
         * */
        Timeout(0xE0, "Timeout, either the initiation was not completed or the client is not responding."),
        /**
         * 0xE1 - Invalid payload.
         * */
        InvalidPayload(0xE1, "Invalid payload"),
        /**
         * 0xE2 - Invalid payload length.
         * */
        InvalidPayloadLength(0xE2, "Invalid payload length"),
        /**
         * 0xE3 - Invalid argument used.
         * */
        InvalidArgument(0xE3, "Invalid argument used"),
        /**
         * 0xE4 - Invalid passive type.
         * */
        InvalidPassiveType(0xE6, "Invalid passive type, must be denoted by 0x01 or 0x02 for speaker or listener respectively"),
        /**
         * 0xE5 - Sensor not found.
         * */
        SensorNotFound(0xE8, "Sensor was not found within the system.");

        /**
         * The byte used to denote an error.
         * */
        public static byte C = (byte) 0xEE;

        private int code;
        private String description;

        /**
         * Creates a new error with a code and a description.
         * @param code The code of the error.
         * @param description The description of the error.
         * */
        private Error(int code, String description) {
            this.code = code;
            this.description = description;
        }

        /**
         * Gets the code of the error.
         * @return The code of the error.
         * */
        public int getCode() {
            return code;
        }

        /**
         * Gets the description of the error.
         * @return The description of the error.
         * */
        public String getDescription() {
            return description;
        }
    }

    private ClientType type;

    private boolean activated;
    private int created;

    private static final boolean verbose = false;

    private static HashMap<Integer, Runnable> callables = new HashMap<>();

    /**
     * List of all the routes that the clients can use.
     * */
    protected static ArrayList<WSClientRoute> routes = new ArrayList<>();

    /**
     * List of all the sensor connections that the clients can use.
     * */
    protected static ArrayList<WSSensorConnection> sensorConnections = new ArrayList<>();

    /**
     * Creates a new WSClient with a handshake.
     * @param handshake The handshake to create the client with.
     * */
    public WSClient(NanoHTTPD.IHTTPSession handshake) {
        super(handshake);
    }

    /**
     * Returns an error string with the error code and description.
     * @param reason The error to get the string of.
     * @return The error string.
     * */
    private String getErrorString(Error reason) {
        return Error.C + " ERROR CODE " + (reason.getCode()) + ": " + reason.getDescription();
    }

    /**
     * Forcefully closes the connection with a reason.
     * @param reason The reason to close the connection.
     * */
    private void forceClose(Error reason) {
        try {
            close(NanoWSD.WebSocketFrame.CloseCode.InvalidFramePayloadData, getErrorString(reason), false);

            //make sure that this gets dum
        } catch (IOException e) {
            // handle
        }
    }

    /**
     * Notifies the client of an error.
     * @param reason The reason to notify the client of.
     * */
    private void notifyError(Error reason) {
        try {
            send(getErrorString(reason));
        } catch (IOException e) {
            // handle
        }
    }

    /**
     * Register a callable with an address (will be called by the client if specified).
     * @param address The address of the callable.
     * @param runnable The runnable to run when the callable is called.
     * */
    public static void registerCallable(int address, Runnable runnable) {
        callables.put(address, runnable);
    }

    /**
     * Register a sensor connection with the client.
     * @param connection The connection to register.
     * */
    public static void registerSensorConnection(WSSensorConnection connection) {
        sensorConnections.add(connection);
    }

    /**
     * Called when the client is opened.
     * */
    @Override
    protected void onOpen() {
        activated = false;
        created = (int) (System.currentTimeMillis());

        System.out.println("Client connected");

        TimerTask close = new TimerTask() {
            @Override
            public void run() {
                if (!activated) {
                    forceClose(Error.Timeout);
                }
            }
        };

        // 5 seconds timeout
        new java.util.Timer().schedule(close, 5000);

        TimerTask heartbeat = new TimerTask() {
            @Override
            public void run() {
                try {
                    ping(new byte[] { (byte) 0xFF, (byte) 0x00 });
                } catch (IOException e) {
                    // handle
                }
            }
        };

        // 1 second heartbeat
        new java.util.Timer().scheduleAtFixedRate(heartbeat, 1000, 1000);
    }

    /**
     * Called when the client is closed.
     * @param code The code of the close.
     * @param reason The reason of the close.
     * @param initiatedByRemote Whether the close was initiated by the remote client.
     * */
    @Override
    protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        //make sure this is removed from memory
        System.out.println("Client disconnected (" + (initiatedByRemote ? "remotely" : "by us") + ")! " + code.toString() + ": " + reason);
    }

    /**
     * Called when a message is received.
     * @param message The message that was received.
     * */
    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {
        if (message.getBinaryPayload()[0] == (byte) 0xFF && message.getBinaryPayload()[1] == (byte) 0x01 && activated) {
            onPong(message); //pong!
            return;
        }

        if (!activated) {
            byte[] payload = message.getBinaryPayload();

            // payload check
            // [0] -> 0xFF (start initiation)
            // [1] -> 0x01 / 0x02 / 0x03 (speaker / listener / passive)
            // [2] -> 0x00 (end initiation)

            if (payload.length != 3) { forceClose(Error.InvalidPayloadLength); return; }
            if (payload[0] != (byte) 0xFF) { forceClose(Error.InvalidPayload); return; }
            if (payload[2] != (byte) 0x00) { forceClose(Error.InvalidPayload); return; }

            activated = true;

            switch (payload[1]) {
                case (byte) 0x01:
                    type = ClientType.Speaker;
                    break;
                case (byte) 0x02:
                    type = ClientType.Listener;
                    break;
                case (byte) 0x03:
                    type = ClientType.Passive;
                    break;
                default:
                    activated = false;
                    forceClose(Error.InvalidPayload);
                    return;
            }

            //send back the confirmation
            try {
                send(new byte[] { (byte) 0xFF, (byte) 0xFF });
                System.out.println("Client successfully activated as " + type.toString());
            } catch (IOException e) {
                notifyError(Error.InternalError);
                e.printStackTrace();
            }

            return;
        }

        byte[] rawPayload = message.getBinaryPayload().clone();

        int[] payload = new int[rawPayload.length];

        //verbose message
        if (verbose) System.out.print("[");

        //if the payload <0, there's an issue while parsing, convert to 0 to 255 range
        for (int i = 0; i < payload.length; i++) {
            payload[i] = rawPayload[i]; //convert the payload to a list of int[] to mitigate signed integers

            if (payload[i] < 0) {
                payload[i] = allPos(payload[i]);
            }

            //verbose message
            if (verbose) System.out.print(payload[i] + (i == payload.length - 1 ? "" : " "));
        }

        //end verbose message
        if (verbose) System.out.println("]");

        //get if it's a speaker
        boolean isSpeaker = (type == ClientType.Speaker);

        //if a passive, then the message must start with either 0x01 or 0x02 for speaker or listener respectively
        if (type == ClientType.Passive) {
            if (payload.length < 1) { notifyError(Error.InvalidPayloadLength); return; }

            switch(payload[0]) {
                case (byte) 0x01:
                    isSpeaker = true;
                case (byte) 0x02:
                    break;
                default:
                    notifyError(Error.InvalidPassiveType);
                    return;
            }

            payload = Arrays.copyOfRange(payload, 1, payload.length);
        }

        //loop through all of the wsclientroutes and check if the route matches
        //if it does, then call the onMessage method
        //if not, then call the handleSpeaker or handleListener method
        for (WSClientRoute route : routes) {
            if (route.getRoutePrefix().length > payload.length) continue;
            if (isSpeaker && route.getType() == ClientType.Listener) continue;
            if (!isSpeaker && route.getType() == ClientType.Speaker) continue;
            if (!Arrays.equals(route.getRoutePrefix(), Arrays.copyOfRange(payload, 0, route.getRoutePrefix().length))) continue;

            route.onMessage(this, Arrays.copyOfRange(payload, route.getRoutePrefix().length, payload.length));
            return;
        }

        if (isSpeaker) {
            handleSpeaker(payload, message);
        } else {
            handleListener(payload, message);
        }
    }

    /**
     * Handles a speaker message.
     * @param payload The payload of the message.
     * @param message The message itself.
     * */
    private void handleSpeaker(int[] payload, NanoWSD.WebSocketFrame message) {
        //first byte is telling what type of speaker it is
        //[0] -> 0x01, 0x02 (set sensor data / run callable)

        if (payload.length < 1) { notifyError(Error.InvalidPayloadLength); return; }

        if (payload[0] == (byte) 0x01) {
            handleSensorUpdate(Arrays.copyOfRange(payload, 1, payload.length), message);
        } else if (payload[0] == (byte) 0x02) {
            handleCallable(Arrays.copyOfRange(payload, 1, payload.length), message);
        }
    }

    /**
     * Handles a sensor update message.
     * @param payload The payload of the message.
     * @param message The message itself.
     * */
    private void handleSensorUpdate(int[] payload, NanoWSD.WebSocketFrame message) {
        if (payload.length < 1) {
            notifyError(Error.InvalidPayloadLength);
            return;
        }

        int sensorConnectionAddress = payload[0];

        WSSensorConnection connection = sensorConnections.stream().filter(c -> c.getId() == sensorConnectionAddress).findFirst().orElse(null);

        if (connection == null) {
            notifyError(Error.SensorNotFound);
            return;
        }

        connection.onUpdate(Arrays.copyOfRange(payload, 1, payload.length));
    }

    /**
     * Handles a callable message.
     * @param payload The payload of the message.
     * @param message The message itself.
     * */
    private void handleCallable(int[] payload, NanoWSD.WebSocketFrame message) {
        //[0] -> address

        if (payload.length < 1) { notifyError(Error.InvalidPayloadLength); return; }

        int address = payload[0];

        Runnable runnable = callables.get(address);

        if (runnable == null) {
            notifyError(Error.SensorNotFound);
            return;
        }

        if (runnable instanceof RunnableWithArgs rwa) {
            int[] args = Arrays.copyOfRange(payload, 1, payload.length);

            rwa.run((Object) args);
            rwa.run(this);
        } else runnable.run();
    }

    /**
     * Handles a listener message.
     * @param payload The payload of the message.
     * @param message The message itself.
     * */
    private void handleListener(int[] payload, NanoWSD.WebSocketFrame message) {
        //first byte is telling what type of listener it is
        //[0] -> 0x01 (sensor data)

        if (payload.length < 1) { notifyError(Error.InvalidPayloadLength); return; }

        boolean isSensor = (payload[0] == (byte) 0x01);
        boolean subscribeToSensor = (payload[0] == (byte) 0x11);

        if (isSensor) {
            handleSensor(Arrays.copyOfRange(payload, 1, payload.length), message);
        } else if (subscribeToSensor) {
            handleSubscribe(Arrays.copyOfRange(payload, 1, payload.length), message);
        }
    }

    /**
     * Handles a subscribe message.
     * @param payload The payload of the message.
     * @param message The message itself.
     * */
    private void handleSubscribe(int[] payload, NanoWSD.WebSocketFrame message) {
        //[0] -> robot address
        //[1] -> sensor address
        //[2] -> 0x00 for unsubscribe, 0x01 for subscribe

        if (payload.length < 2) { notifyError(Error.InvalidPayloadLength); return; }

        int robotAddr = payload[0];
        int sensorAddress = payload[1];
        int subscribe = payload[2];

        Sensor sensor = Sensor.getSensor(sensorAddress, robotAddr);

        if (sensor == null) {
            notifyError(Error.SensorNotFound);
            return;
        }

        if (subscribe == 0x00) {
            sensor.unsubscribe(this);
        } else if (subscribe == 0x01) {
            System.out.println("[Robot " + robotAddr + "]: Subscribed to sensor " + sensorAddress);
            sensor.subscribe(this);
        } else {
            notifyError(Error.InvalidArgument);
        }
    }

    /**
     * Handles a sensor message.
     * @param payload The payload of the message.
     * @param message The message itself.
     * */
    private void handleSensor(int[] payload, NanoWSD.WebSocketFrame message) {
        //[0] -> address
        //[1] -> 0x00 for processed, 0x01 for raw

        if (payload.length < 3) { notifyError(Error.InvalidPayloadLength); return; }

        int robotAddr = payload[0];
        int address = payload[1];
        int processed = payload[2];

        Sensor sensor = Sensor.getSensor(address, robotAddr);

        if (sensor == null) {
            notifyError(Error.SensorNotFound);
            return;
        }

        double[] returnPayload;

        if (processed == 0x00) {
            returnPayload = sensor.getValues(); //processed
        } else if (processed == 0x01) {
            returnPayload = sensor.getSensorValues(); //raw
        } else {
            notifyError(Error.InvalidArgument);
            return;
        }

        int nDoubles = returnPayload.length;

        int preLength = 5;
        //encode doubles to byte list
        byte[] encodedDoubles = new byte[(nDoubles * Double.BYTES) + preLength];

        encodedDoubles[0] = (byte) 0xC0; //response
        encodedDoubles[1] = (byte) 0x01; //sensor data
        encodedDoubles[2] = (byte) sensor.getParentIdentification();
        encodedDoubles[3] = (byte) sensor.getIdentifier();
        encodedDoubles[4] = (byte) nDoubles;

        ByteBuffer bbuf = ByteBuffer.allocate(nDoubles * Double.BYTES);

        Arrays.stream(returnPayload).forEach(bbuf::putDouble);

        System.arraycopy(bbuf.array(), 0, encodedDoubles, preLength, nDoubles * Double.BYTES);

        try {
            send(encodedDoubles);
        } catch (Exception e) {
            notifyError(Error.InternalError);
            e.printStackTrace();
        }
    }

    /**
     * Sends sensor data to the client.
     * @param robotAddress The address of the robot.
     * @param sensorAddress The address of the sensor.
     * @param values The values to send.
     * */
    public void sendSensorData(byte robotAddress, byte sensorAddress, double[] values) {
        byte[] payload = new byte[Double.BYTES * values.length + 1];

        payload[0] = (byte) values.length;

        ByteBuffer bbuf = ByteBuffer.allocate(values.length * Double.BYTES);

        Arrays.stream(values).forEach(bbuf::putDouble);

        System.arraycopy(bbuf.array(), 0, payload, 1, values.length * Double.BYTES);

        sendValues((byte) 0x01, robotAddress, sensorAddress, payload);
    }

    /**
     * Sends values to the client.
     * @param type The type of the values.
     * @param address The first address (typically the robot address)
     * @param address2 The second address (typically the sensor address)
     * @param payload The payload to send.
     * */
    public void sendValues(byte type, byte address, byte address2, byte... payload) {
        final int infoPayloadLength = 4;
        byte[] encodedValues = new byte[infoPayloadLength + payload.length];

        encodedValues[0] = (byte) 0xC0;
        encodedValues[1] = type;
        encodedValues[2] = address;
        encodedValues[3] = address2;

        if (payload.length > 0) System.arraycopy(payload, 0, encodedValues, infoPayloadLength, payload.length);

        try {
            send(encodedValues);
        } catch (IOException e) {
            notifyError(Error.InternalError);
            e.printStackTrace();
        }
    }

    /**
     * Called when a pong is received.
     * @param pong The pong that was received.
     * */
    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {
        //pong!
    }

    /**
     * Called when an exception occurs.
     * @param exception The exception that occurred.
     * */
    @Override
    protected void onException(IOException exception) {

    }
}