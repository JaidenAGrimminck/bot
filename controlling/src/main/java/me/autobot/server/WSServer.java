package me.autobot.sim.server;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

import java.io.IOException;

public class SimSocket extends NanoWSD {

    public static void main(String[] args) {
        try {
            new SimSocket(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SimSocket(int port) throws IOException {
        super(port);
        System.out.println("WSServer started on port " + port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new SimClient(handshake);
    }

}
