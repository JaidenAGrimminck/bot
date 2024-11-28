package me.autobot.code.mechanisms;

import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.objects.Rectangle;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.robot.sensors.LidarSensor;
import me.autobot.lib.systems.mechanisms.Mechanism;

import java.util.ArrayList;

/**
 * LIDAR Mechanism that can construct a terrain map from LIDAR data.
 * */
public class LIDAR extends Mechanism {
    ArrayList<Rectangle> rectangles = new ArrayList<>();

    ArrayList<Vector2d> points = new ArrayList<>();

    private LidarSensor lidarSensor;

    /**
     * Initializes the LIDAR mechanism.
     * */
    @Override
    public void init() {
        lidarSensor = new LidarSensor(0xDD);

        lidarSensor.setParent(parent);

        lidarSensor.connectToSerial("");
    }

    /**
     * Updates the LIDAR mechanism.
     * */
    @Override
    protected void update() {
        Rotation2d rotation = lidarSensor.getRotation();
        Unit distance = lidarSensor.getDistance();

        Vector2d robotPos = parent.getPosition();

        Vector2d point = Vector2d.rotateValue(distance.getValue(Unit.Type.CENTIMETER), rotation);

        points.add(point);
    }
}
