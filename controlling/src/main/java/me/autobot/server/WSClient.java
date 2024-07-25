package me.autobot.sim.server;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

import java.io.IOException;

public class SimClient extends NanoWSD.WebSocket {
    enum ClientType {
        Sensor,
    }

    public SimClient(NanoHTTPD.IHTTPSession handshake) {
        super(handshake);
    }

    @Override
    protected void onOpen() {

    }

    @Override
    protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {

    }

    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {
        try {
            send(message.getTextPayload() + " to you");
        } catch (IOException e) {
            // handle
        }
    }

    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {

    }

    @Override
    protected void onException(IOException exception) {

    }
}