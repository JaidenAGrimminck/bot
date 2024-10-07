package me.autobot.server;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface WSByteRoute {
    public WSClient.ClientType type() default WSClient.ClientType.Passive;
    public int[] prefix() default { 0xFF, 0xFF };
}
