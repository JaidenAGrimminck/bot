package me.autobot.server;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

import java.io.IOException;

public class WSServer extends NanoWSD {

    public static void main(String[] args) {
        try {
            new WSServer(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WSServer(int port) throws IOException {
        super(port);
        System.out.println("WSServer started on port " + port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new WSClient(handshake);
    }

}
