package me.autobot.serial;

public class Serial {

    public static Serial connect(int port) {
        return new Serial(port);
    }

    private final int port;

    private Serial(int port) {
        this.port = port;
    }
    //todo: import jnode
}
