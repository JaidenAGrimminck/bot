package me.autobot.server;

import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import me.autobot.lib.robot.PlayableRobot;
import me.autobot.lib.robot.Robot;
import me.autobot.lib.tools.RunnableWithArgs;
import me.autobot.server.topica.Topica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * REST server for the robot.
 * */
public class RESTServer extends NanoHTTPD {
    private static RESTServer instance;

    /**
     * Gets the instance of the REST server.
     * @return The instance of the REST server.
     * */
    public static RESTServer getInstance() {
        return instance;
    }

    /**
     * A route for the REST server.
     * */
    public static class Route {
        /**
         * Runs the route.
         * @return The response to the route.
         * */
        public Response run() {
            return null;
        }
    }

    private HashMap<String, Route> routes = new HashMap<>();

    /**
     * Adds a route to the server.
     * @param path The path of the route.
     * @param route The route to add.
     * */
    public void addRoute(String path, Route route) {
        routes.put(path, route);
    }


    /**
     * Starts the server on the specified port.
     * @param port The port to start the server on.
     * @throws IOException If the server cannot be started.
     * */
    public RESTServer(int port) throws IOException {
        super(port);

        instance = this;

        addRoutes();

        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    /**
     * Adds private default routes to the server.
     * */
    private void addRoutes() {
        addRoute("/api/v1/robots", new Route() {
            @Override
            public Response run() {
                ArrayList<String> names = new ArrayList<>();
                for (Class<? extends Robot> robot : Robot.getRobotClasses()) {
                    PlayableRobot playableRobot = robot.getAnnotation(PlayableRobot.class);
                    if (playableRobot != null) {
                        names.add(playableRobot.name());
                    }
                }

                Gson gson = new Gson();

                //return json response
                return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(names));
            }
        });

        addRoute("/api/v2/topics", new Route() {
            @Override
            public Response run() {
                String[] realTopics = Topica.getDatabase().getRealTopics();
                String[] defaultTopics = Topica.getDatabase().getDefaultTopics();

                ArrayList<String> topics = new ArrayList<>();

                for (int i = 0; i < Math.max(realTopics.length, defaultTopics.length); i++) {
                    if (i < realTopics.length) {
                        topics.add(realTopics[i]);
                    }

                    if (i < defaultTopics.length) {
                        topics.add(defaultTopics[i]);
                    }
                }

                Gson gson = new Gson();

                return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(topics));
            }
        });
    }

    /**
     * On request, serve the appropriate route.
     * @param session The session to serve.
     * @return The response to the request.
     * */
    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Route route = routes.get(uri);

        if (route != null) {
            return route.run();
        } else {
            return newFixedLengthResponse("404");
        }
    }
}
