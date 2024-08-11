package me.autobot.code;

import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.coordinates.Vector3d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.math.rotation.Rotation3d;
import me.autobot.lib.robot.Motor;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.robot.UltrasonicSensor;

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
    
    private Vector2d position = new Vector2d(1100, 1050);
    private Rotation2d rotation = new Rotation2d(0);

    private boolean totalSimulation = false;

    public Robot() {
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
        rightSensor.attachRelativePosition(new Vector3d(20d, 0d, 0d));


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
    }

    public Vector2d getPosition() {
        return position;
    }

    public Rotation2d getRotation() {
        return rotation;
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
