package me.autobot.code;

import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.coordinates.Vector3d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.math.rotation.Rotation3d;
import me.autobot.lib.odometry.SimpleOdometry2d;
import me.autobot.lib.robot.Motor;
import me.autobot.lib.robot.Robot;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.robot.sensors.CollisionSensor;
import me.autobot.lib.robot.sensors.UltrasonicSensor;
import me.autobot.sim.graphics.SimCanvas;

import java.util.ArrayList;

public class SimRobot extends Robot {

    private Motor topLeftMotor;
    private Motor topRightMotor;
    private Motor bottomLeftMotor;
    private Motor bottomRightMotor;

    private UltrasonicSensor frontSensor;
    private UltrasonicSensor backSensor;

    private UltrasonicSensor leftSensor;
    private UltrasonicSensor rightSensor;

    private UltrasonicSensor topLeftSensor;
    private UltrasonicSensor topRightSensor;
    private UltrasonicSensor bottomLeftSensor;
    private UltrasonicSensor bottomRightSensor;

    private CollisionSensor collisionSensor;

    @Override
    protected void setup() {
        setRobotSize(new Vector2d(40, 60));
        setOdometry(
                new SimpleOdometry2d()
                        .setStartPosition(new Vector2d(130, 1870))
                        .setStartRotation(new Rotation2d(2 * Math.PI / 2))
                        .reset()
        );

        topLeftMotor = new Motor(0x1);
        topRightMotor = new Motor(0x02);
        bottomLeftMotor = new Motor(0x03);
        bottomRightMotor = new Motor(0x04);

        bottomRightMotor.invert();
        topRightMotor.invert();

        frontSensor = new UltrasonicSensor(0x01);
        frontSensor.attachRelativePosition(new Vector3d(0d, 30d, 0d), Rotation3d.fromDegrees(90, 90));
        backSensor = new UltrasonicSensor(0x02);
        backSensor.attachRelativePosition(new Vector3d(0d, -30d, 0d), Rotation3d.fromDegrees(270, 90));
        leftSensor = new UltrasonicSensor(0x03);
        leftSensor.attachRelativePosition(new Vector3d(-20d, 0d, 0d), Rotation3d.fromDegrees(180, 90));
        rightSensor = new UltrasonicSensor(0x04);
        rightSensor.attachRelativePosition(new Vector3d(20d, 0d, 0d), Rotation3d.fromDegrees(0, 90));

        topLeftSensor = new UltrasonicSensor(0x05);
        topLeftSensor.attachRelativePosition(new Vector3d(-20d, 30d, 0d), Rotation3d.fromDegrees(135, 90));
        topRightSensor = new UltrasonicSensor(0x06);
        topRightSensor.attachRelativePosition(new Vector3d(20d, 30d, 0d), Rotation3d.fromDegrees(45, 90));
        bottomLeftSensor = new UltrasonicSensor(0x07);
        bottomLeftSensor.attachRelativePosition(new Vector3d(-20d, -30d, 0d), Rotation3d.fromDegrees(225, 90));
        bottomRightSensor = new UltrasonicSensor(0x08);
        bottomRightSensor.attachRelativePosition(new Vector3d(20d, -30d, 0d), Rotation3d.fromDegrees(315, 90));

        //used primarily for simulation purposes
        collisionSensor = new CollisionSensor(0xC7);

        //just incase simulation, we can do this to ensure that multiple robots can be created for ai etc
        getDevices().forEach(device -> device.setParent(this));
    }

    @Override
    protected void onMove() {
        ArrayList<Sensor> sensors = getSensors();

        for (Sensor sensor : sensors) {
            if (sensor instanceof UltrasonicSensor) {
                UltrasonicSensor us = (UltrasonicSensor) sensor;
                Unit d = us.getDistance();

                if (d.getValue(Unit.Type.CENTIMETER) > UltrasonicSensor.maxDistance.getValue(Unit.Type.CENTIMETER)) continue;

                Vector2d ray = Vector2d
                        .fromPolar(
                                d.getValue(Unit.Type.CENTIMETER),
                                Rotation2d.fromRadians(us.getRelativeRotation().getThetaRadians() + getRotation().getTheta())
                        )
                        .add(
                                us.getEstimatedAbsPosition()
                        );

                SimCanvas.obstaclesMap.addObstaclePoint(ray);
            }
        }
    }
}
