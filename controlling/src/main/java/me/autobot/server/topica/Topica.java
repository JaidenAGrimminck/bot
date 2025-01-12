package me.autobot.server.topica;

import fi.iki.elonen.NanoWSD;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Topica, a topic based server that
 * */
public class Topica {

    public static class Topic<T> {
        public static enum Access {
            internal,
            readonly,
            all
        }

        public static class Address {
            private static final int len = 4;
            private byte[] code;

            /**
             * Extracts an address from a payload.
             * @param payload The payload to extract the address from.
             * @param start The start of the address in the payload.
             * */
            public static Address extract(int[] payload, int start) {
                byte[] code = new byte[len];

                for (int i = 0; i < len; i++) code[i] = (byte) payload[start + i];

                return new Address(code);
            }

            public Address(byte ...code) {
                if (code.length != len) throw new IllegalArgumentException("Address code must be four bytes.");

                this.code = code;
            }

            /**
             * Compares two addresses
             * @param other The other address
             * */
            public boolean compare(Address other) {
                int c = 0;

                //doing this convoluted way to make sure that we compare bytes, not potentially pointers.

                for (int i = 0; i < code.length; i++) if (code[i] == other.code[i]) c++;

                return c == len;
            }
        }

        private T value;
        private String name;

        private Address address;

        public Topic(Address addr, String name, T value) {
            this.name = name;
            this.value = value;
            this.address = addr;
        }

        public T get() {
            return value;
        }

        public String getAsString() {
            return value.toString();
        }

        public void set(T value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Address getAddress() {
            return address;
        }
    }

    private static Topica instance;

    private ArrayList<Topic<?>> topics = new ArrayList<>();

    public static Topica getInstance() {
        return instance;
    }

    public static void start() {
        instance = new Topica();
    }

    public Topica() {

    }

    public void addTopic(Topic<?> topic) {
        topics.add(topic);
    }

    /**
     * Requests a topic from the server.
     * @param payload The payload of the request.
     * @param message The message of the request.
     * */
    public void request(int[] payload, NanoWSD.WebSocketFrame message) {
        // byte 0 is the operation
        // byte 1,2,3,4 is the address

        int operation = payload[0];

        Topic.Address addr = Topic.Address.extract(payload, 1);

        switch (operation) {
            case 0x01: // REGISTER
                int type = payload[5];

                addTopicFromIntegerType(addr, type);
                break;

        }
    }

    protected void addTopicFromIntegerType(Topic.Address addr, int type) {

    }
}
