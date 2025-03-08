package me.autobot.code.me;

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
import me.autobot.lib.tools.RunnableWithArgs;
import me.autobot.lib.server.WSClient;
import me.autobot.sim.graphics.SimCanvas;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * A robot used in the context of simulation!
 * */
public class SimRobot extends Robot {
    /**
     * Creates a new SimRobot
     * Not necessary needed, but it's here for the sake of completeness.
     * */
    public SimRobot() {
        super();
    }

    private Motor topLeftMotor;
    private Motor topRightMotor;
    private Motor bottomLeftMotor;
    private Motor bottomRightMotor;

    private UltrasonicSensor frontSensor;

    private UltrasonicSensor topLeftSensor;
    private UltrasonicSensor topRightSensor;
    private CollisionSensor collisionSensor;

    /**
     * Sets up the robot with 4 motors, 8 ultrasonic sensors, and 1 collision sensor.
     * Also sets up the robot for simulation purposes.
     * */
    @Override
    protected void setup() {
        setRobotSize(new Vector2d(40, 60));
        setOdometry(
                new SimpleOdometry2d()
                        .setStartPosition(new Vector2d(130, 1870))
                        .setStartRotation(new Rotation2d(2 * Math.PI / 2))
                        .reset()
        );

        topLeftMotor = new Motor(0x01, 0x01);
        topRightMotor = new Motor(0x02, 0x01);
        bottomLeftMotor = new Motor(0x03, 0x01);
        bottomRightMotor = new Motor(0x04, 0x01);

        bottomRightMotor.invert();
        topRightMotor.invert();

        frontSensor = new UltrasonicSensor(0x01);
        frontSensor.attachRelativePosition(new Vector3d(0d, 30d, 0d), Rotation3d.fromDegrees(90, 90));

        topLeftSensor = new UltrasonicSensor(0x03);
        topLeftSensor.attachRelativePosition(new Vector3d(-20d, 30d, 0d), Rotation3d.fromDegrees(135, 90));
        topRightSensor = new UltrasonicSensor(0x02);
        topRightSensor.attachRelativePosition(new Vector3d(20d, 30d, 0d), Rotation3d.fromDegrees(45, 90));

        //used primarily for simulation purposes
        collisionSensor = new CollisionSensor(0xC7);

        this.registerAllDevices();

        WSClient.registerCallable(0xD5, new RunnableWithArgs() {
            @Override
            public void run(Object... args) {
                //convert first 8 bytes to a double
                int[] data = (int[]) args[0];

                // convert the first 8 bytes of the data to a double
                ByteBuffer buffer = ByteBuffer.allocate(8);
                for (int i = 0; i < 8; i++) {
                    buffer.put((byte) data[i]);
                }
                buffer.flip();
                double x = buffer.getDouble(); //first double is x

                // convert the second 8 bytes of the data to a double
                buffer = ByteBuffer.allocate(8);
                for (int i = 8; i < 16; i++) {
                    buffer.put((byte) data[i]);
                }
                buffer.flip();
                double y = buffer.getDouble(); //first double is y


            }
        });
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
