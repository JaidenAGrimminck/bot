package me.autobot.server;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.tools.RunnableWithArgs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TimerTask;

public class WSClient extends NanoWSD.WebSocket {
    enum ClientType {
        Speaker,
        Listener,
        Passive //passive, as in can be either speaker or listener
    }

    enum Error {
        InternalError(0xDF, "There was an internal error that occurred while executing this task"),
        Timeout(0xE0, "Timeout, either the initiation was not completed or the client is not responding."),
        InvalidPayload(0xE1, "Invalid payload"),
        InvalidPayloadLength(0xE2, "Invalid payload length"),
        InvalidArgument(0xE3, "Invalid argument used"),
        InvalidPassiveType(0xE6, "Invalid passive type, must be denoted by 0x01 or 0x02 for speaker or listener respectively"),
        SensorNotFound(0xE8, "Sensor was not found within the system.");

        public static int C = 0xEE;

        private int code;
        private String description;

        private Error(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    private ClientType type;

    private boolean activated;
    private int created;

    private static HashMap<Integer, Runnable> callables = new HashMap<>();

    public WSClient(NanoHTTPD.IHTTPSession handshake) {
        super(handshake);
    }

    private String getErrorString(Error reason) {
        return Error.C + " ERROR CODE " + (reason.getCode()) + ": " + reason.getDescription();
    }

    private void forceClose(Error reason) {
        try {
            close(NanoWSD.WebSocketFrame.CloseCode.InvalidFramePayloadData, getErrorString(reason), false);

            //make sure that this gets dum
        } catch (IOException e) {
            // handle
        }
    }

    private void notifyError(Error reason) {
        try {
            send(getErrorString(reason));
        } catch (IOException e) {
            // handle
        }
    }

    public static void registerCallable(int address, Runnable runnable) {
        callables.put(address, runnable);
    }

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
    }

    @Override
    protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        //make sure this is removed from memory
    }

    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {
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
                send(new byte[] { (byte) 0xFF, (byte) 0x00 });
                System.out.println("Client successfully activated as " + type.toString());
            } catch (IOException e) {
                notifyError(Error.InternalError);
                e.printStackTrace();
            }

            return;
        }

        byte[] payload = message.getBinaryPayload();

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

        if (isSpeaker) {
            handleSpeaker(payload, message);
        } else {
            handleListener(payload, message);
        }
    }

    private void handleSpeaker(byte[] payload, NanoWSD.WebSocketFrame message) {
        //first byte is telling what type of speaker it is
        //[0] -> 0x01, 0x02 (set sensor data / run callable)

        if (payload.length < 1) { notifyError(Error.InvalidPayloadLength); return; }

        if (payload[0] == (byte) 0x02) {
            handleCallable(Arrays.copyOfRange(payload, 1, payload.length), message);
        }
    }

    private void handleCallable(byte[] payload, NanoWSD.WebSocketFrame message) {
        //[0] -> address

        if (payload.length < 1) { notifyError(Error.InvalidPayloadLength); return; }

        int address = payload[0];

        Runnable runnable = callables.get(address);

        if (runnable == null) {
            notifyError(Error.SensorNotFound);
            return;
        }

        if (runnable instanceof RunnableWithArgs rwa) {
            byte[] args = Arrays.copyOfRange(payload, 1, payload.length);

            rwa.run((Object) args);
        } else runnable.run();
    }

    private void handleListener(byte[] payload, NanoWSD.WebSocketFrame message) {
        //first byte is telling what type of listener it is
        //[0] -> 0x01 (sensor data)

        if (payload.length < 1) { notifyError(Error.InvalidPayloadLength); return; }

        boolean isSensor = (payload[0] == (byte) 0x01);

        if (isSensor) {
            handleSensor(Arrays.copyOfRange(payload, 1, payload.length), message);
        }
    }

    private void handleSensor(byte[] payload, NanoWSD.WebSocketFrame message) {
        //[0] -> address
        //[1] -> 0x00 for processed, 0x01 for raw

        if (payload.length < 2) { notifyError(Error.InvalidPayloadLength); return; }

        int address = payload[0];
        int processed = payload[1];

        Sensor sensor = Sensor.getSensor(address);

        if (sensor == null) {
            notifyError(Error.SensorNotFound);
            return;
        }

        double[] returnPayload;

        if (processed == 0x00) {
            returnPayload = sensor.getValues();
        } else if (processed == 0x01) {
            returnPayload = sensor.getSensorValues();
        } else {
            notifyError(Error.InvalidArgument);
            return;
        }

        int nDoubles = returnPayload.length;

        int preLength = 3;
        //encode doubles to byte list
        byte[] encodedDoubles = new byte[(nDoubles * Double.BYTES) + preLength];

        encodedDoubles[0] = (byte) 0xC0; //response
        encodedDoubles[1] = (byte) 0x01; //sensor data
        encodedDoubles[2] = (byte) nDoubles;

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

    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {
        pong.setUnmasked();
        try {
            sendFrame(pong);
        } catch (IOException e) {
            // handle
        }
    }

    @Override
    protected void onException(IOException exception) {

    }
}