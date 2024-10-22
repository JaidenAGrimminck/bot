package me.autobot.lib.logging;

/**
 * Annotation to mark fields and methods that should be logged.
 * */
public @interface Log {
    /**
     * What the log is saved as.
     * @return The name of the log.
     * */
    String as() default "";
}
