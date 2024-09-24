package me.autobot.code;

import me.autobot.lib.math.Geometry;
import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.coordinates.Vector3d;
import me.autobot.lib.math.map.Map2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.math.rotation.Rotation3d;
import me.autobot.lib.robot.Motor;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.robot.sensors.CollisionSensor;
import me.autobot.lib.robot.sensors.UltrasonicSensor;
import me.autobot.sim.Simulation;
import me.autobot.sim.graphics.SimCanvas;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

//tank drive robot
public class Robot {

    private static boolean totalSimulation = false;
    private static ArrayList<Robot> robots = new ArrayList<>();

    public static ArrayList<Robot> getRobots() {
        return robots;
    }
    private final Vector2d robotSize = new Vector2d(40, 60);

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

    private final Vector2d startingPosition = new Vector2d(130, 1870);
    private Vector2d position = startingPosition.clone();

    private final Rotation2d startingRotation = new Rotation2d(2 * Math.PI / 2);
    private Rotation2d rotation = startingRotation.clone();

    private boolean hasCrashed = false;
    private byte identification;

    private HashMap<String, Integer> flags = new HashMap<>();

    public Robot() {
        identification = (byte) robots.size();

        robots.add(this);

        topLeftMotor = new Motor(0x1);
        topRightMotor = new Motor(0x02);
        bottomLeftMotor = new Motor(0x03);
        bottomRightMotor = new Motor(0x04);

        bottomRightMotor.invert();
        topRightMotor.invert();

        //,0001 is to prevent a bug lmao idk how to fix it and i don't want to spend the hours of time to fix it
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

        //used primarily for simulation purposes
        collisionSensor = new CollisionSensor(0xC7);

        //just incase simulation, we can do this to ensure that multiple robots can be created for ai etc
        getSensors().forEach(sensor -> {
            sensor.setParent(this);
        });
    }

    public byte getIdentification() {
        return identification;
    }

    public static void startSimulation() {
        totalSimulation = true;
        for (Robot robot : robots) {
            robot.switchSensorsToSimulation();
        }
    }

    protected void switchSensorsToSimulation() {
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

    public void move(double a, double b) {
        if (hasCrashed) return;
        position = position.add(new Vector2d(a, b));
        onMove();
    }

    public void rotate(double theta) {
        if (hasCrashed) return;
        rotation = rotation.rotateBy(Rotation2d.fromRadians(theta));
        onMove();
    }

    void onMove() {
        //SimCanvas.debugStr = "rpos#" + position.toString();

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

                SimCanvas.obstaclesMap.addObstaclePoint(ray);
            }
        }
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

    public Vector2d getRobotSize() {
        return robotSize;
    }

    public boolean inCollision() {
        if (totalSimulation) {
            // get obstacles
            ArrayList<Box2d> obstacles = (ArrayList<Box2d>) Simulation.getInstance().environment.obstacles.clone();

            // get robot position
            Vector2d pos = getPosition();
            // get robot rotation
            Rotation2d rot = getRotation();

            //sort the box2d points by distance to the robot
            obstacles.sort((o1, o2) -> {
                double d1 = o1.getPosition().distance(pos.toInt2());
                double d2 = o2.getPosition().distance(pos.toInt2());
                return Double.compare(d1, d2);
            });

            Vector2d topLeft = new Vector2d(-robotSize.getX() / 2, -robotSize.getY() / 2);
            Vector2d topRight =  new Vector2d(robotSize.getX() / 2, -robotSize.getY() / 2);
            Vector2d bottomLeft = new Vector2d(-robotSize.getX() / 2, robotSize.getY() / 2);
            Vector2d bottomRight = new Vector2d(robotSize.getX() / 2, robotSize.getY() / 2);

            //rotate the points
            topLeft = topLeft.rotate(rot);
            topRight = topRight.rotate(rot);
            bottomLeft = bottomLeft.rotate(rot);
            bottomRight = bottomRight.rotate(rot);

            Vector2d[] list = new Vector2d[] {topLeft, topRight, bottomLeft, bottomRight};

            //add the position to the points
            for (int i = 0; i < list.length; i++) {
                list[i] = pos.add(list[i]);
            }


            for (Box2d obstacle : obstacles) {
                if (Geometry.twoPolygonsIntersecting(list, obstacle.getVertices())) {
                    hasCrashed = true;
                    return true;
                }
            }
        }

        return false;
    }

    public void setFlag(String key, int value) {
        flags.put(key, value);
    }

    public int getFlag(String key) {
        return flags.get(key);
    }

    public void reset() {
        hasCrashed = false;
        position = startingPosition.clone();
        rotation = startingRotation.clone();
    }
}
