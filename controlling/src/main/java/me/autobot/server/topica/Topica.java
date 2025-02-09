package me.autobot.server.topica;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;


/**
 * Topica, a topic based server that
 * */
public class Topica extends NanoWSD.WebSocket {

    private static Database database;

    /**
     * Gets the database of the Topica server.
     * @return The database of the Topica server.
     * */
    public static Database getDatabase() {
        return database;
    }

    static {
        database = new Database();
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
            public static byte[] encode() {
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

            private ArrayList<UpdateCallback> callbacks;

            /**
             * Creates a new topic.
             * @param path The path of the topic.
             *             This is the unique identifier of the topic.
             * @param data The data of the topic as a byte array.
             *             This is the value of the topic.
             * */
            public Topic(String path, byte[] data) {
                this.path = path;
                this.data = data;

                callbacks = new ArrayList<>();

                if (Topica.getDatabase() != null) {
                    Topica.getDatabase().addTopic(this);
                }
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

        }

        private ArrayList<Topic> topics;

        /**
         * Creates a new topic database.
         * */
        public Database() {
            topics = new ArrayList<>();
        }

        /**
         * Adds a topic to the database.
         * @param topic The topic to add.
         * */
        public void addTopic(Topic topic) {
            topics.add(topic);
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

    /**
     * Creates a new Topica WebSocket connection.
     * @param handshakeRequest The handshake request.
     * */
    public Topica(NanoHTTPD.IHTTPSession handshakeRequest) {
        super(handshakeRequest);
    }

    /**
     * Creates a new Topica WebSocket connection.
     * */
    @Override
    protected void onOpen() {

    }

    /**
     * Closes the Topica WebSocket connection.
     * @param code The close code.
     * @param reason The reason for closing.
     * @param initiatedByRemote Whether the connection was closed by the remote.
     * */
    @Override
    protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {

    }

    /**
     * Handles a message from the Topica WebSocket connection.
     * @param message The message from the WebSocket.
     *  */
    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {

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
