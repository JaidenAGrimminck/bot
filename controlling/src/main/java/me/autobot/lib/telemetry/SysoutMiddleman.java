package me.autobot.lib.telemetry;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Captures all messages sent to System.out and System.err and sends them to the telemetry server.
 * */
public class SysoutMiddleman {
    /**
     * Starts the middleman.
     * */
    public static void start() {
        System.setOut(new PrintStream(new Sysout()));
        System.setErr(new PrintStream(new Sysout()));
    }

    private static String buffer = "";
    private static ArrayList<String> messages = new ArrayList<>();


    public static class Sysout extends OutputStream {
        @Override
        public void write(int b) {
            buffer += (char) b;
            if (buffer.endsWith("\n")) {
                messages.add(buffer);
                buffer = "";
            }
        }
    }


}
