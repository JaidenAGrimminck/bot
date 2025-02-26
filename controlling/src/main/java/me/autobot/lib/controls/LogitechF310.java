package me.autobot.lib.controls;

import me.autobot.server.topica.Topica;

public class LogitechF310 implements Gamepad {

    /**
     * TOPICS USED:
     * - [basePath]/timestamp
     * - [basePath]/leftX
     * - [basePath]/leftY
     * - [basePath]/rightX
     * - [basePath]/rightY
     *
     * - TODO: Consolidate the topics into one/two topics? But allow public parsing.
     * */

    private final String basePath;

    private Topica.Database.Topic timestamp;
    private Topica.Database.Topic leftX;
    private Topica.Database.Topic leftY;
    private Topica.Database.Topic rightX;
    private Topica.Database.Topic rightY;

    public LogitechF310(String basePath) {
        this.basePath = basePath;


        if (Topica.getDatabase().hasTopic(basePath + "/timestamp")) {
            timestamp = Topica.getDatabase().getTopic(basePath + "/timestamp");
            // logitech is prob created, so we can get the other topics
            leftX = Topica.getDatabase().getTopic(basePath + "/leftX");
            leftY = Topica.getDatabase().getTopic(basePath + "/leftY");
            rightX = Topica.getDatabase().getTopic(basePath + "/rightX");
            rightY = Topica.getDatabase().getTopic(basePath + "/rightY");
        } else {
            // we'll have to create the topics
            timestamp = new Topica.Database.Topic(basePath + "/timestamp", 0d);
            leftX = new Topica.Database.Topic(basePath + "/leftX", 0d);
            leftY = new Topica.Database.Topic(basePath + "/leftY", 0d);
            rightX = new Topica.Database.Topic(basePath + "/rightX", 0d);
            rightY = new Topica.Database.Topic(basePath + "/rightY", 0d);
        }
    }

    @Override
    public double getLeftX() {
        return leftX.getAsDouble();
    }

    @Override
    public double getLeftY() {
        return leftY.getAsDouble();
    }

    @Override
    public double getRightX() {
        return rightX.getAsDouble();
    }

    @Override
    public double getRightY() {
        return rightY.getAsDouble();
    }
}
