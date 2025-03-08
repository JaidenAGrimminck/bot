package me.autobot.lib.server;

/**
 * A class representing a route for the WSClient.
 * This class can be used to handle messages from the WSClient.
 * */
public class WSClientRoute {
    /**
     * What the message prefix should be (should be unique).
     * @return The message prefix.
     * */
    public int[] getRoutePrefix() {
        //get the @WSByteRoute annotation
        WSByteRoute route = this.getClass().getAnnotation(WSByteRoute.class);

        if (route == null) {
            return new int[] {0xFF, 0xFF, 0xFF};
        }

        return route.prefix();
    }

    /**
     * Gets the type of the route (whether that be passive, speaker, or listener).
     * @return The type of the route.
     * */
    public WSClient.ClientType getType() {
        //get the @WSClientType annotation
        WSByteRoute type = this.getClass().getAnnotation(WSByteRoute.class);

        if (type == null) {
            return WSClient.ClientType.Passive;
        }

        return type.type();
    }

    /**
     * Adds the route to the WSClient.
     * */
    protected void addToWSClient() {
        WSClient.ClientType type = this.getType();

        assert type != WSClient.ClientType.Passive : "Cannot add a passive route to the WSClient.";

        WSClient.routes.add(this);
    }

    /**
     * Removes the route from the WSClient.
     * */
    public void removeFromWSClient() {
        WSClient.routes.remove(this);
    }

    /**
     * Creates a new WSClientRoute and adds it to the WSClient.
     * */
    public WSClientRoute() {
        this.addToWSClient();
    }

    /**
     * Called when a message is received from the WSClient.
     * @param client The client that sent the message.
     * @param message The message that was sent.
     * */
    public void onMessage(WSClient client, int[] message) {
        //override this method to handle messages
    }
}
