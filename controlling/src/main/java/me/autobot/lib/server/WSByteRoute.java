package me.autobot.lib.server;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotation for websocket client routes.
 * */
@Retention(RetentionPolicy.RUNTIME)
public @interface WSByteRoute {
    /**
     * The type of the client.
     * @see WSClient.ClientType
     * @return The type of the client.
     * */
    public WSClient.ClientType type() default WSClient.ClientType.Passive;

    /**
     * The prefix of the route.
     * @return The prefix of the route.
     * */
    public int[] prefix() default { 0xFF, 0xFF };
}
