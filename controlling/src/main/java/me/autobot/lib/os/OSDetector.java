package me.autobot.lib.os;

/**
 * Helper class for detecting the operating system of the user.
 */
public class OSDetector {

    /**
     * Creates a new OSDetector...but this shouldn't happen. Throws an error.
     * Please use the static methods instead.
     * */
    public OSDetector() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    /**
     * Returns true if the user is using a Mac OS.
     *
     * @return true if the user is using a Mac OS.
     */
    public static boolean usingMacOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("mac");
    }

    /**
     * Returns true if the user is using a Windows OS.
     *
     * @return true if the user is using a Windows OS.
     */
    public static boolean usingWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    /**
     * Returns true if the user is using a Linux OS.
     *
     * @return true if the user is using a Linux OS.
     */
    public static boolean usingLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("linux");
    }
}
