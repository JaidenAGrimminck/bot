package me.autobot.code;

import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.coordinates.Vector3d;
import me.autobot.lib.math.map.Map2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.math.rotation.Rotation3d;
import me.autobot.lib.robot.Motor;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.robot.UltrasonicSensor;
import me.autobot.sim.graphics.SimCanvas;

import java.lang.reflect.Field;
import java.util.ArrayList;

//tank drive robot
public class Robot {

    public static Robot instance;

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
    
    private Vector2d position = new Vector2d(1910, 860);
    private Rotation2d rotation = new Rotation2d(0);

    private Map2d obstaclesMap;

    private boolean totalSimulation = false;

    public Robot() {
        topLeftMotor = new Motor(0x1);
        topRightMotor = new Motor(0x02);
        bottomLeftMotor = new Motor(0x03);
        bottomRightMotor = new Motor(0x04);

        bottomRightMotor.invert();
        topRightMotor.invert();

        obstaclesMap = new Map2d();

        //,0001 is to prevent a bug lmao idk how to fix it and i don't want to spent the hours of time to fix it
        frontSensor = new UltrasonicSensor(0x01);
        frontSensor.attachRelativePosition(new Vector3d(0d, 30d, 0d), Rotation3d.fromDegrees(90.0001, 90));
        backSensor = new UltrasonicSensor(0x02);
        backSensor.attachRelativePosition(new Vector3d(0d, -30d, 0d), Rotation3d.fromDegrees(270.0001, 90));
        leftSensor = new UltrasonicSensor(0x03);
        leftSensor.attachRelativePosition(new Vector3d(-20d, 0d, 0d), Rotation3d.fromDegrees(180.0001, 90));
        rightSensor = new UltrasonicSensor(0x04);
        rightSensor.attachRelativePosition(new Vector3d(20d, 0d, 0d), Rotation3d.fromDegrees(0.0001, 90));

        topLeftSensor = new UltrasonicSensor(0x05);
        topLeftSensor.attachRelativePosition(new Vector3d(-20d, 30d, 0d), Rotation3d.fromDegrees(135.0001, 90));
        topRightSensor = new UltrasonicSensor(0x06);
        topRightSensor.attachRelativePosition(new Vector3d(20d, 30d, 0d), Rotation3d.fromDegrees(45.0001, 90));
        bottomLeftSensor = new UltrasonicSensor(0x07);
        bottomLeftSensor.attachRelativePosition(new Vector3d(-20d, -30d, 0d), Rotation3d.fromDegrees(225.0001, 90));
        bottomRightSensor = new UltrasonicSensor(0x08);
        bottomRightSensor.attachRelativePosition(new Vector3d(20d, -30d, 0d), Rotation3d.fromDegrees(315.0001, 90));

        //just incase simulation, we can do this to ensure that multiple robots can be created for ai etc
        getSensors().forEach(sensor -> sensor.setParent(this));
    }

    public void startSimulation() {
        totalSimulation = true;

        //get all fields
        for (Field field : this.getClass().getDeclaredFields()) {

            //check if it extends sensor or device
            if ((field.getType().getSuperclass() == null ? field.getType() : field.getType().getSuperclass()).equals(Sensor.class)) {
                try {
                    Sensor sensor = (Sensor) field.get(this);
                    sensor.enableSimulation();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void move(int a, int b) {
        position = position.add(new Vector2d(a, b));
        onMove();
    }

    public void rotate(double theta) {
        rotation = rotation.rotateBy(Rotation2d.fromRadians(theta));
        onMove();
    }

    void onMove() {
        SimCanvas.debugStr = "rpos#" + position.toString();

        ArrayList<Sensor> sensors = getSensors();

        for (Sensor sensor : sensors) {
            if (sensor instanceof UltrasonicSensor) {
                UltrasonicSensor us = (UltrasonicSensor) sensor;
                Unit d = us.getDistance();

                if (d.getValue(Unit.Type.CENTIMETER) > 250) continue;

                Vector2d ray = Vector2d
                        .fromPolar(
                                d.getValue(Unit.Type.CENTIMETER),
                                Rotation2d.fromRadians(us.getRelativeRotation().getThetaRadians() + getRotation().getTheta())
                        )
                        .add(
                                us.getEstimatedAbsPosition()
                        );

                obstaclesMap.addObstaclePoint(ray);
            }
        }
    }

    public Vector2d getPosition() {
        return position;
    }

    public Rotation2d getRotation() {
        return rotation;
    }

    public Map2d getMap() {
        return obstaclesMap;
    }

    public ArrayList<Sensor> getSensors() {
        ArrayList<Sensor> sensors = new ArrayList<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if ((field.getType().getSuperclass() == null ? field.getType() : field.getType().getSuperclass()).equals(Sensor.class)) {
                try {
                    sensors.add((Sensor) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return sensors;
    }

}
