package me.autobot.server;

public class WSClientRoute {
    public int[] getRoutePrefix() {
        //get the @WSByteRoute annotation
        WSByteRoute route = this.getClass().getAnnotation(WSByteRoute.class);

        if (route == null) {
            return new int[] {0xFF, 0xFF, 0xFF};
        }

        return route.prefix();
    }

    public WSClient.ClientType getType() {
        //get the @WSClientType annotation
        WSByteRoute type = this.getClass().getAnnotation(WSByteRoute.class);

        if (type == null) {
            return WSClient.ClientType.Passive;
        }

        return type.type();
    }

    protected void addToWSClient() {
        WSClient.ClientType type = this.getType();

        assert type != WSClient.ClientType.Passive : "Cannot add a passive route to the WSClient.";

        WSClient.routes.add(this);
    }

    public void removeFromWSClient() {
        WSClient.routes.remove(this);
    }

    public WSClientRoute() {
        this.addToWSClient();
    }

    public void onMessage(WSClient client, int[] message) {
        //override this method to handle messages
    }
}
