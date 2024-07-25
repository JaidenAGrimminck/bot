package me.autobot.server;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

import java.io.IOException;
import java.util.Arrays;
import java.util.TimerTask;

public class WSClient extends NanoWSD.WebSocket {
    enum ClientType {
        Speaker,
        Listener,
        Passive //passive, as in can be either speaker or listener
    }

    enum Error {
        Timeout(0xE0, "Timeout, either the initiation was not completed or the client is not responding."),
        InvalidPayload(0xE1, "Invalid payload"),
        InvalidPayloadLength(0xE2, "Invalid payload length"),
        InvalidPassiveType(0xE3, "Invalid passive type, must be denoted by 0x01 or 0x02 for speaker or listener respectively"),
        SensorNotFound(0xE4, "Sensor was not found within the system.");

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

    @Override
    protected void onOpen() {
        activated = false;
        created = (int) (System.currentTimeMillis());

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
        //[1] -> 0x01 / 0x02 / 0x03 (single channel / selective channels / all channels)
        //[2] -> 0x00 (if [2] = 0x03), number of channels if [1] = 0x02, channel if [1] = 0x01
        //only if [1] = 0x02, [3...n] -> channels to read

        if (payload.length < 3) { notifyError(Error.InvalidPayloadLength); return; }

        int address = payload[0];
        byte type = payload[1];



        if (type == 0x03) {
            //all channels

        }
    }

    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {

    }

    @Override
    protected void onException(IOException exception) {

    }
}