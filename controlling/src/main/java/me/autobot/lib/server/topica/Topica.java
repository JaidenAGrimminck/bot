package me.autobot.lib.server.topica;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import kotlin.Pair;
import me.autobot.lib.math.Mathf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;


/**
 * Topica, a topic based server that
 * */
public class Topica extends NanoWSD.WebSocket {

    private static final int DEFAULT_PORT = 5443; // tc, in unicode.

    private static final byte GET_RESPONSE = 0b0001;
    private static final byte SET_RESPONSE = 0b0010;
    private static final byte SUBSCRIBE_RESPONSE = 0b0011;

    private static final byte RESPONSE_FLAG = 0b1000;

    private static final long PING_TIME = 500; //ms

    /**
     * Type code denoting a byte list.
     * */
    public static final byte BYTE_TYPE = 0x01;
    /**
     * Type code denoting a short.
     * */
    public static final byte SHORT_TYPE = 0x02;
    /**
     * Type code denoting an integer.
     * */
    public static final byte INT_TYPE = 0x03;
    /**
     * Type code denoting a long.
     * */
    public static final byte LONG_TYPE = 0x04;
    /**
     * Type code denoting a float.
     * */
    public static final byte FLOAT_TYPE = 0x05;
    /**
     * Type code denoting a double.
     * */
    public static final byte DOUBLE_TYPE = 0x06;
    /**
     * Type code denoting a string.
     * */
    public static final byte STRING_TYPE = 0x07;
    /**
     * Type code denoting a boolean.
     * */
    public static final byte BOOLEAN_TYPE = 0x08;
    /**
     * A custom type that has a custom encoding.
     * TODO: fix.
     * */
    public static final byte CUSTOM_TYPE = 0x09;

    private static Database database;
    private static Server server;

    private static int port = DEFAULT_PORT;

    /**
     * Assigns the port to a custom port.
     * @param port The port to start the server on.
     * */
    public static void port(int port) {
        Topica.port = port;
    }

    /**
     * Returns true or false depending on if the verbose setting is on.
     * @return If the Topica server is verbose, using /topica/verbose.
     * */
    protected static boolean isVerbose() {
        return database.hasTopic("/topica/verbose") && database.getTopic("/topica/verbose").getAsBoolean();
    }

    /**
     * Creates a new Topica database.
     * */
    public static void createDatabase() {
        if (database != null) {
            return;
        }

        database = new Database();
        /*
         * Verbose: for messages in the console when a new topic is made.
         * */
        new Database.Topic("/topica/verbose", true);
        /*
         * Strict: new topics cannot be created through the web interface, and must be created through the code.
         * */
        new Database.Topic("/topica/strict", false);
    }

    /**
     * Starts a new Topica server on the default port or the custom port.
     * */
    public static void start() {
        server = new Server(port);
        try {
            server.start();
            System.out.println("[TOPICA] Topica instance server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the database of the Topica server.
     * @return The database of the Topica server.
     * */
    public static Database getDatabase() {
        if (database == null) {
            createDatabase();
        }

        return database;
    }

    /**
     * Database for the Topica server, containing all of the different topics.
     * */
    public static class Database {
        /**
         * An interface allowing for custom encodeable topics.
         * */
        public static interface EncodeableTopic {
            /**
             * Empty class for when an encodable is not implemented.
             * */
            public static class Empty {
                /**
                 * Decodes the data into an empty object.
                 * @param data The data to decode.
                 * @return An empty object.
                 * */
                public static Empty decode(byte[] data) {
                    return new Empty();
                }

                /**
                 * Encodes the empty object.
                 * @return An empty byte array.
                 * */
                public static byte[] encode() {
                    return new byte[0];
                }

                /**
                 * Creates a new empty object.
                 * */
                public Empty() {

                }
            }

            /**
             * Decodes the data into an encodeable topic.
             * @param data The data to decode.
             * @return The decoded encodeable topic.
             * */
            public static EncodeableTopic decode(byte[] data) {
                return (EncodeableTopic) new Empty();
            }

            /**
             * Encodes the encodeable topic.
             * @return The encoded data.
             * */
            public default byte[] encode() {
                return new byte[0];
            }
        }

        /**
         * A topic of the database.
         * */
        public static class Topic {
            /**
             * Callback class for when a topic is updated.
             * */
            public static interface UpdateCallback {
                /**
                 * Called when a topic is updated.
                 *
                 * @param topic The topic that was updated.
                 */
                public void onUpdate(Topic topic);
            }

            private String path;
            private byte[] data;

            private byte type;

            private ArrayList<UpdateCallback> callbacks;

            /**
             * Creates a new topic.
             * @param path The path of the topic.
             *             This is the unique identifier of the topic.
             * @param data The data of the topic as a byte array.
             *             This is the value of the topic.
             * */
            public Topic(String path, byte type, byte[] data) {
                this.path = path;
                this.data = data;
                this.type = type;

                callbacks = new ArrayList<>();

                if (Topica.getDatabase() != null) {
                    Topica.getDatabase().addTopic(this);
                }
            }

            /**
             * Creates a dummy topic that is not added to the Database instance.
             * @param type The type of the data
             * @param data The raw byte data, can also pass `new byte[0]` and use `this#update()`.
             */
            public Topic(byte type, byte[] data) {
                this.path = "/tmp/" + UUID.randomUUID();
                this.data = data;
                this.type = type;

                callbacks = new ArrayList<>();
            }

            /**
             * Creates a new topic from a int.
             * @param path The path of the topic.
             * @param data The data of the topic as an int.
             */
            public Topic(String path, int data) {
                this(path, INT_TYPE, new byte[0]);

                ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
                buffer.putInt(data);
                this.data = buffer.array();
            }

            /**
             * Creates a new topic from a long.
             * @param path The path of the topic.
             * @param data The data of the topic as a long.
             */
            public Topic(String path, long data) {
                this(path, LONG_TYPE, new byte[0]);

                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                buffer.putLong(data);
                this.data = buffer.array();
            }

            /**
             * Creates a new topic from a float.
             * @param path The path of the topic.
             * @param data The data of the topic as a float.
             */
            public Topic(String path, float data) {
                this(path, FLOAT_TYPE, new byte[0]);

                ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
                buffer.putFloat(data);
                this.data = buffer.array();
            }

            /**
             * Creates a new topic from a double.
             * @param path The path of the topic.
             * @param data The data of the topic as a double.
             */
            public Topic(String path, double data) {
                this(path, DOUBLE_TYPE, new byte[0]);

                ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
                buffer.putDouble(data);
                this.data = buffer.array();
            }

            /**
             * Creates a new topic from a string.
             * @param path The path of the topic.
             * @param data The data of the topic as a string.
             */
            public Topic(String path, String data) {
                this(path, STRING_TYPE, new byte[0]);

                this.data = data.getBytes();
            }

            /**
             * Creates a new topic from a boolean.
             * @param path The path of the topic.
             * @param data The data of the topic as a boolean.
             */
            public Topic(String path, boolean data) {
                this.path = path;
                this.type = BOOLEAN_TYPE;
                this.data = new byte[] { (byte) (data ? 1 : 0) };

                callbacks = new ArrayList<>();

                if (Topica.getDatabase() != null) {
                    Topica.getDatabase().addTopic(this);
                }
            }

            /**
             * Creates a new topic from a custom encodeable topic.
             * @param path The path of the topic.
             * @param data The data of the topic as a custom encodeable topic.
             */
            public Topic(String path, EncodeableTopic data) {
                this(path, CUSTOM_TYPE, new byte[0]);

                this.data = data.encode();
            }

            /**
             * Adds a callback to the topic.
             * @param callback The callback to add.
             * */
            public void addCallback(UpdateCallback callback) {
                callbacks.add(callback);
            }

            /**
             * Removes a callback from the topic.
             * @param callback The callback to remove.
             * */
            public void removeCallback(UpdateCallback callback) {
                callbacks.remove(callback);
            }

            /**
             * Gets the path of the topic.
             * @return The path of the topic.
             */
            public String getPath() {
                return path;
            }

            /**
             * Gets the data of the topic as a byte array.
             * @return The data of the topic as a byte array.
             */
            public byte[] getData() {
                return data;
            }

            // for all types, assume that the data is the raw bytes of that type
            /***
             * Gets the data of the topic as an int.
             * @return The data of the topic as an int.
             */
            public int getAsInt() {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                return buffer.getInt();
            }

            /***
             * Gets the data of the topic as a long.
             * @return The data of the topic as a long.
             */
            public long getAsLong() {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                return buffer.getLong();
            }

            /**
             * Gets the data of the topic as a float.
             * @return The data of the topic as a float.
             * */
            public float getAsFloat() {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                return buffer.getFloat();
            }

            /**
             * Gets the data of the topic as a double.
             * @return The data of the topic as a double.
             * */
            public double getAsDouble() {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                return buffer.getDouble();
            }

            /**
             * Gets the data of the topic as a string.
             * @return The data of the topic as a string.
             * */
            public String getAsString() {
                return new String(data);
            }

            /**
             * Gets the data of the topic as a boolean.
             * @return The data of the topic as a boolean.
             * */
            public boolean getAsBoolean() {
                return data[0] == 1;
            }

            /**
             * Updates the data of the topic.
             * @param data The new data.
             * */
            public void update(int data) {
                ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
                buffer.putInt(data);
                this.data = buffer.array();

                runCallbacks();
            }

            /**
             * Updates the data of the topic.
             * @param data The new data.
             * */
            public void update(long data) {
                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                buffer.putLong(data);
                this.data = buffer.array();

                runCallbacks();
            }

            /**
             * Updates the data of the topic.
             * @param data The new data.
             * */
            public void update(float data) {
                ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
                buffer.putFloat(data);
                this.data = buffer.array();

                runCallbacks();
            }

            /**
             * Updates the data of the topic.
             * @param data The new data.
             * */
            public void update(double data) {
                ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
                buffer.putDouble(data);
                this.data = buffer.array();

                runCallbacks();
            }

            /**
             * Updates the data of the topic.
             * @param data The new data.
             */
            public void update(String data) {
                this.data = data.getBytes();

                runCallbacks();
            }

            /**
             * Updates the data of the topic.
             * @param data The new data.
             * */
            public void update(boolean data) {
                this.data = new byte[] { (byte) (data ? 1 : 0) };

                runCallbacks();
            }

            /**
             * Updates the data of the topic.
             * @param data The new data.
             * */
            public void update(byte[] data) {
                this.data = data;

                runCallbacks();
            }

            /**
             * Sends a notification to all callbacks that the topic has been updated.
             * */
            protected void runCallbacks() {
                for (UpdateCallback callback : callbacks) {
                    callback.onUpdate(this);
                }
            }

            /**
             * Gets the data of the topic as a custom encodeable topic.
             * @param <T> The type of the encodeable topic.
             *           Must implement {@link EncodeableTopic}.
             * @return The data of the topic as a custom encodeable topic.
             * */
            public <T extends EncodeableTopic> T getEncodeable() {
                EncodeableTopic topic = EncodeableTopic.decode(data);
                return (T) topic;
            }

            /**
             * Gets the type of the topic.
             * @return The type of the topic.
             * */
            public byte getType() {
                return type;
            }

            /**
             * Get the name/path of the topic.
             * @return The name/path of the topic.
             * */
            public String getName() {
                return path;
            }
        }

        private ArrayList<Topic> topics;

        /**
         * Creates a new topic database.
         * */
        public Database() {
            topics = new ArrayList<>();
        }

        /**
         * Get all (real) topics.
         * @return All real topics, that is, excluding any client-specific topics.
         * */
        public String[] getRealTopics() {
            String[] topicNames = new String[topics.size()];

            for (int i = 0; i < topics.size(); i++) {
                topicNames[i] = topics.get(i).path;
            }

            return topicNames;
        }


        /**
         * Get all default topics
         * @return All default topics, that is, only client-specific topics.
         * */
        public String[] getDefaultTopics() {
            String[] defaultNames = new String[2];

            defaultNames[0] = "/me/nickname";
            defaultNames[1] = "/me/id";

            return defaultNames;
        }

        /**
         * Adds a topic to the database.
         * @param topic The topic to add.
         * */
        protected void addTopic(Topic topic) {
            if (topic.path.startsWith("/tmp")) {
                return;
            }

            topics.add(topic);

            if (hasTopic("/topica/verbose") && getTopic("/topica/verbose").getAsBoolean()) {
                System.out.println("[TOPICA] Added new topic: " + topic.getPath());
            }
        }

        /**
         * Removes a topic from the database.
         * @param topic The topic to remove.
         * */
        public void removeTopic(Topic topic) {
            topics.remove(topic);
        }

        /**
         * Gets a topic from the database by the path.
         * @param path The path of the topic.
         * @return The topic with the given path.
         * */
        public Topic getTopic(String path) {
            for (Topic topic : topics) {
                if (topic.getPath().equals(path)) {
                    return topic;
                }
            }

            return null;
        }

        /**
         * Checks if the topic exists or not
         * @param path The path of the topic.
         * @return True if the topic exists, false otherwise.
         * */
        public boolean hasTopic(String path) {
            return getTopic(path) != null;
        }
    }

    private static ArrayList<String> usedNicknames = new ArrayList<>();


    /**
     * Generates a nickname
     * @return the new nickname
     * */
    protected static String generateNickname() {
        String[] first = new String[] {
                "Raring",
                "Gearing",
                "Flying",
                "Running",
                "Jumping",
                "Sleeping",
                "Racking",
                "Opening",
                "Closing",
                "Running",
                "Sitting",
                "Standing",
                "Moving",
                "Fleeing",
                "Chilling",
                "Heating",
                "Cooking",
                "Pinching",
                "Squeezing",
                "Stretching",
                "Patting",
                "Fluttering",
        };

        String[] second = new String[] {
                "Axolotl",
                "Racoon",
                "Dog",
                "Cat",
                "Lion",
                "Leopard",
                "Shark",
                "Fish",
                "Catfish",
                "Dolphin",
                "Whale",
                "Platypus",
                "Turtle",
                "Tortoise",
                "Pigeon",
                "Albatross",
                "Seagull"
        };

        String[] third = new String[] {
                "Pear",
                "String",
                "Apple",
                "Cheese",
                "Pasta",
                "Steak",
                "Orange",
                "Lemon",
                "Clementine",
                "Broccoli",
                "Aubergine",
                "Eggplant",
                "Bread",
                "Melon",
                "Mango",
                "Dragonfruit",
                "Lychee",
                "Durian"
        };

        String nick;
        int i = 0;

        do {
            nick = first[(int) Math.floor(Math.random() * first.length)];
            nick += second[(int) Math.floor(Math.random() * second.length)];
            nick += third[(int) Math.floor(Math.random() * third.length)];

            i++;
        } while (usedNicknames.contains(nick) && i < 4000);

        if (i == 4000) {
            return "NotCreativeEnough" + Math.floor(Math.random() * (usedNicknames.size() * 2 + 1));
        }

        return nick;
    }

    /**
     * The server for the Topica protocol.
     * */
    public static class Server extends NanoWSD {
        /**
         * Starts a new Topica server on the given port.
         * @param port The port to start the server on.
         * */
        public Server(int port) {
            super(port);
        }

        /**
         * Starts a new Topica server on the given hostname and port.
         * @param hostname The hostname to start the server on.
         * @param port The port to start the server on.
         * */
        public Server(String hostname, int port) {
            super(hostname, port);
        }

        /**
         * Opens a new WebSocket connection.
         * @param handshake The handshake request.
         *                  This is the request to open the WebSocket.
         * @return The new WebSocket connection.
         * */
        @Override
        protected WebSocket openWebSocket(IHTTPSession handshake) {
            return new Topica(handshake);
        }
    }


    // variables for the Topica class
    private ArrayList<Pair<Timer, TimerTask>> subscriptions;

    // code name to make it easier to understand which is what.
    private String nickname;

    private final UUID connectionID;

    private TimerTask pingTask;
    private Timer pingTimer;

    /**
     * Creates a new Topica WebSocket connection.
     * @param handshakeRequest The handshake request.
     * */
    public Topica(NanoHTTPD.IHTTPSession handshakeRequest) {
        super(handshakeRequest);

        subscriptions = new ArrayList<>();

        connectionID = UUID.randomUUID();
        nickname = generateNickname();
    }

    /**
     * Creates a new Topica WebSocket connection.
     * */
    @Override
    protected void onOpen() {
        String remoteHost = this.getHandshakeRequest().getRemoteHostName();
        String remotePort = this.getHandshakeRequest().getRemoteIpAddress();

        if (isVerbose()) {
            System.out.println("[TOPICA] Opened new Topica connection @ " + remoteHost + ":" + remotePort + " with " + this.nickname + " (" + this.connectionID.toString() + ").");
        }

        pingTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    ping(new byte[] {
                            0x00
                    });
                } catch (IOException e) {
                    if (isVerbose()) {
                        System.out.println("[TOPICA] Unable to ping!");
                    }
                }
            }
        };

        pingTimer = new Timer();
        pingTimer.schedule(pingTask, PING_TIME, PING_TIME); //ping every 500ms.
    }

    /**
     * Closes the Topica WebSocket connection.
     * @param code The close code.
     * @param reason The reason for closing.
     * @param initiatedByRemote Whether the connection was closed by the remote.
     * */
    @Override
    protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        //close all timers
        for (Pair<Timer, TimerTask> pair : subscriptions) {
            pair.component1().cancel();
            pair.component2().cancel();
        }

        pingTimer.cancel();
        pingTask.cancel();

        if (isVerbose()) {
            System.out.println("[TOPICA] Closed Topica connection with " + this.nickname + " (" + this.connectionID.toString() + ").");
        }
    }

    /**
     * Handles a message from the Topica WebSocket connection.
     * @param message The message from the WebSocket.
     *  */
    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {
        byte[] payload = message.getBinaryPayload();

        int[] data = Mathf.allPos(payload);

        //first, get the method using the first byte
        int firstByte = data[0];
        int secondByte = data[1];

        // get the four msb of the first byte
        int method = firstByte >> 4;

        // combine the four lsb of the first byte and the second byte
        int pathLength = (firstByte & 0b00001111) << 8 | secondByte;

        // get the path
        byte[] pathBytes = new byte[pathLength];
        for (int i = 0; i < pathLength; i++) {
            pathBytes[i] = (byte) data[i + 2];
        }

        // decode pathBytes to utf-8 string
        String path = new String(pathBytes);

        if (method == GET_RESPONSE) {
            sendTopicData(path);
        } else if (method == SET_RESPONSE) {
            int[] restOfPayload = new int[data.length - pathLength - 2];
            for (int i = 0; i < restOfPayload.length; i++) {
                restOfPayload[i] = data[i + pathLength + 2];
            }

            setTopicData(path, restOfPayload);
        } else if (method == SUBSCRIBE_RESPONSE) {
            int[] restOfPayload = new int[data.length - pathLength - 2];
            for (int i = 0; i < restOfPayload.length; i++) {
                restOfPayload[i] = data[i + pathLength + 2];
            }

            subscribeToTopic(path, restOfPayload);
        }
    }

    /**
     * Sends the data of a topic to the client.
     * @param topic The topic to send the data of.
     * */
    protected void sendTopicData(String topic) {
        byte[] pathLength = new byte[2];

        // 4 msb to signify the method
        pathLength[0] = (byte) (RESPONSE_FLAG << 4);

        // 12 lsb to signify the length of the path
        pathLength[0] |= (byte) ((topic.length() >> 8) & 0b00001111);
        pathLength[1] = (byte) (topic.length() & 0xFF);

        // encode the path
        byte[] encodedPath = topic.getBytes();

        if (!database.hasTopic(topic) && !topic.startsWith("/me/")) {
            // send an error message
            return;
        }

        // get the topic data
        Database.Topic dbTopic = null;

        if (topic.startsWith("/me/")) {
            dbTopic = new Database.Topic(STRING_TYPE, new byte[0]);
            if (topic.equalsIgnoreCase("/me/nickname")) {
                dbTopic.update(this.nickname);
            } else if (topic.equalsIgnoreCase("/me/id")) {
                dbTopic.update(this.connectionID.toString());
            }
        } else {
            dbTopic = database.getTopic(topic);
        }

        if (dbTopic == null) {
            return;
        }

        // get the data
        byte[] data = dbTopic.getData();

        byte type = dbTopic.getType();

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(data.length);

        byte[] payloadLength = buffer.array();

        // payload goes:
        /*
        * Path Length
        * Path
        * Data Type
        * Data Length
        * Data
        * */

        byte[] payload = new byte[2 + encodedPath.length + 1 + Integer.BYTES + data.length];

        // copy the path length
        System.arraycopy(pathLength, 0, payload, 0, 2);

        // copy the path
        System.arraycopy(encodedPath, 0, payload, 2, encodedPath.length);

        // copy the data type
        payload[2 + encodedPath.length] = type;

        // copy the data length
        System.arraycopy(payloadLength, 0, payload, 2 + encodedPath.length + 1, Integer.BYTES);

        // copy the data
        System.arraycopy(data, 0, payload, 2 + encodedPath.length + 1 + Integer.BYTES, data.length);

        try {
            send(payload);
        } catch (IOException ignored) {}
    }

    /**
     * Sets the data of a topic.
     * @param topic The topic to set the data of.
     * @param unusedPayload The payload containing the rest of the data request.s
     * */
    protected void setTopicData(String topic, int[] unusedPayload) {
        /**
         * In the unused payload:
         * Data Type: 1 byte
         * Data Length: 4 bytes
         * Data: n bytes
         * */

        byte type = (byte) unusedPayload[0];

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        for (int i = 0; i < Integer.BYTES; i++) {
            buffer.put(i, (byte) unusedPayload[i + 1]);
        }

        int dataLength = buffer.getInt();

        byte[] data = new byte[dataLength];
        for (int i = 0; i < dataLength; i++) {
            data[i] = (byte) unusedPayload[i + Integer.BYTES + 1];
        }

        if (!database.hasTopic(topic)) {
            if (database.hasTopic("/topica/strict") && database.getTopic("/topica/strict").getAsBoolean()) {
                // send an error message
                return;
            }

            new Database.Topic(topic, type, data);
        } else {
            Database.Topic dbTopic = database.getTopic(topic);
            dbTopic.update(data);
        }

    }

    /**
     * Subscribes the websocket to a topic.
     * @param topic The topic to subscribe to.
     * @param unusedPayload The payload containing the rest of the data request.
     * */
    protected void subscribeToTopic(String topic, int[] unusedPayload) {
        // the 4 bytes are the interval, in ms
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        for (int i = 0; i < 4; i++) {
            buffer.put(i, (byte) unusedPayload[i]);
        }

        int interval = buffer.getInt();

        if (interval < 10) {
            // send an error message
            if (isVerbose()) {
                System.out.println("[TOPICA] Cannot subscribe topic " + topic + " with interval " + interval);
                if (interval < 0) {
                    System.out.println("[TOPICA] *Tip: double check your endian! Java uses big-endian.*");
                }
            }
            return;
        }

        if (isVerbose()) {
            System.out.println("[TOPICA] Client subscribed to topic " + topic + "");
        }

        if (!database.hasTopic(topic)) {
            return;
        }

        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                sendTopicData(topic);
            }
        };

        timer.scheduleAtFixedRate(task, interval, interval);

        subscriptions.add(new Pair<>(timer, task));
    }

    /**
     * Called on the pong frame.
     * @param pong The pong frame.
     * */
    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {

    }

    /**
     * Called when an exception occurs.
     * @param exception The exception that occurred.
     * */
    @Override
    protected void onException(IOException exception) {
    }
}
