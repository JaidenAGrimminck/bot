package me.autobot.lib.os;

public class OSDetector {
    public static boolean usingMacOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("mac");
    }

    public static boolean usingWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    public static boolean usingLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("linux");
    }
}
