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
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

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
            timestamp = new Topica.Database.Topic(basePath + "/timestamp", 0L);
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

    /**
     * Get 
     * @return The timestamp, in ms.
     * */
    @Override
    public double getRightY() {
        return rightY.getAsDouble();
    }

    /**
     * Get when the gamepad was last updated
     * @return The timestamp, in ms.
     * */
    public long getLastUpdated() {
        return timestamp.getAsLong();
    }

    /**
     * Returns the base path of the gamepad topics.
     * @return Base path
     * */
    public String getPath() {
        return this.basePath;
    }
}
