package me.autobot.lib.logging;

public @interface Log {
    /** what the log is saved as */
    String as() default "";
}
