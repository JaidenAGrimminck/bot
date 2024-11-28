package me.autobot.lib.hardware.ros;

import me.autobot.lib.hardware.Connection;

/**
 * Object representing a ROS subscription.
 * */
public class ROSSubscription extends Connection {
    private String topic;

    /**
     * Creates a new ROS subscription with the given topic.
     * @param topic The topic to subscribe to.
     * */
    public ROSSubscription(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
}
