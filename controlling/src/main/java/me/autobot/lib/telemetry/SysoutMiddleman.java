package me.autobot.lib.telemetry;

import me.autobot.lib.tools.RunnableWithArgs;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Captures all messages sent to System.out and System.err and sends them to the telemetry server.
 * */
public class SysoutMiddleman {
    /**
     * Message type for a message sent to System.out // System.err.
     * */
    public static class Message {
        private String message;
        private int type;

        /**
         * Creates a new message.
         * @param message The message.
         * @param type The type of message.
         * */
        public Message(String message, int type) {
            this.message = message;
            this.type = type;
        }

        /**
         * Gets the message.
         * @return The message.
         * */
        public String getMessage() {
            return message;
        }

        /**
         * Gets the type of message.
         * @return The type of message.
         * */
        public int getType() {
            return type;
        }
    }

    /**
     * Starts the middleman.
     * */
    public static void start() {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        System.setOut(new PrintStream(new Sysout(originalOut, 0)));
        System.setErr(new PrintStream(new Sysout(originalErr, 1)));
    }

    /**
     * Array of all previous messages.
     * todo: make this way more memory efficient.
     * */
    private static ArrayList<Message> messages = new ArrayList<>();

    /**
     * Sysout messages.
     * */
    public static class Sysout extends OutputStream {
        /**
         * Listener for messages.
         * */
        public static interface Listener {
            /**
             * Called when a message is received.
             * @param message The message received.
             * */
            public void onMessage(Message message);
        }

        private static final int maxMessages = 1000;

        private static Sysout error;
        private static Sysout out;

        /**
         * Gets the error sysout.
         * @return The error sysout.
         * */
        public static Sysout getError() {
            return error;
        }

        /**
         * Gets the output sysout.
         * @return The output sysout.
         * */
        public static Sysout getOut() {
            return out;
        }

        private final PrintStream original;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final int type;

        private ArrayList<Listener> li = new ArrayList<>();

        /**
         * Creates a new Sysout object.
         * @param original The original PrintStream.
         * @param type The type of message. 0 for System.out, 1 for System.err.
         * */
        public Sysout(PrintStream original, int type) {
            this.original = original;
            this.type = type;

            if (type == 0) {
                out = this;
            } else {
                error = this;
            }
        }

        /**
         * Writes a byte to the output stream.
         * @param b The byte to write.
         * */
        @Override
        public void write(int b) {
            buffer.write(b);
            original.write(b);

            if (b == '\n') {
                String message = buffer.toString();
                buffer.reset();

                messages.add(new Message(message, type));

                for (Listener listener : this.li) {
                    listener.onMessage(new Message(message, type));
                }

                while (messages.size() > maxMessages) {
                    messages.remove(0);
                }
            }
        }

        /**
         * Gets the type of message.
         * @return The type of message.
         * */
        public int getType() {
            return type;
        }


        /**
         * Adds a listener to the sysout.
         * @param listener The listener to add.
         * */
        public void addListener(Listener listener) {
            li.add(listener);
        }

        /**
         * Removes a listener from the sysout.
         * @param listener The listener to remove.
         * */
        public void removeListener(Listener listener) {
            li.remove(listener);
        }
    }

    /**
     * Gets all messages.
     * @return All messages.
     * */
    public static ArrayList<Message> getMessages() {
        return messages;
    }

}
