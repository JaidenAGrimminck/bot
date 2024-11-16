package me.autobot.code;

import me.autobot.lib.math.coordinates.Polar;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.robot.Robot;
import me.autobot.lib.robot.drivebase.ArcadeDrive;
import me.autobot.lib.robot.drivebase.TankDrive;
import me.autobot.lib.robot.motors.HoverboardWheel;
import me.autobot.lib.tools.RunnableWithArgs;
import me.autobot.server.WSClient;
import me.autobot.server.WSServer;

import java.nio.ByteBuffer;

/**
 * Main robot class for the robot.
 * */
public class MainBot extends Robot {
    private static class Config {
        public static final int TOP_LEFT_MOTOR = 0x09;
        public static final int BACK_LEFT_MOTOR = 0x0A;
        public static final int TOP_RIGHT_MOTOR = 0x09;
        public static final int BACK_RIGHT_MOTOR = 0x0A;

        public static final int TOP_LEFT_MOTOR_DIRECTION = 0x08;
        public static final int BACK_LEFT_MOTOR_DIRECTION = 0x0B;
        public static final int TOP_RIGHT_MOTOR_DIRECTION = 0x08;
        public static final int BACK_RIGHT_MOTOR_DIRECTION = 0x0B;
    }

    private final boolean manualControl = true;

    private HoverboardWheel topLeft;
    private HoverboardWheel topRight;
    private HoverboardWheel bottomLeft;

    private HoverboardWheel bottomRight;

    private TankDrive arcadeDrive;

    private Vector2d joystick = new Vector2d(0, 0);

    private double aiSpeed = 0;
    private double aiRotation = 0;

    private boolean joystickUpdated = false;

    /**
     * Creates a new MainBot.
     * */
    public MainBot() {
        super();
    }

    /**
     * Sets up the robot.
     * */
    @Override
    protected void setup() {
        //start websocket client
        WSServer.wsstart();

        //initialize the motors and set their max speeds to 0.5
        topLeft = new HoverboardWheel(0x01, 0x01);
        topLeft.setMaxSpeed(0.5);

        bottomLeft = new HoverboardWheel(0x02, 0x01);
        bottomLeft.setMaxSpeed(0.5);
        bottomLeft.invert();

        topRight = new HoverboardWheel(0x03, 0x02);
        topRight.setMaxSpeed(0.5);

        bottomRight = new HoverboardWheel(0x04, 0x02);
        bottomRight.setMaxSpeed(0.5);
        bottomRight.invert();

        //register devices
        registerAllDevices();

        //connect devices to serial.
        topLeft.connectToSerial("/dev/cu.usbserial-10",
                Config.TOP_LEFT_MOTOR_DIRECTION, Config.TOP_LEFT_MOTOR
        );

        bottomLeft.connectToSerial("/dev/cu.usbserial-10",
                Config.BACK_LEFT_MOTOR_DIRECTION, Config.BACK_LEFT_MOTOR
        );

        //right side is on a different port.
        topRight.connectToSerial("/dev/cu.usbserial-110",
                Config.TOP_RIGHT_MOTOR_DIRECTION, Config.TOP_RIGHT_MOTOR
        );

        bottomRight.connectToSerial("/dev/cu.usbserial-110",
                Config.BACK_RIGHT_MOTOR_DIRECTION, Config.BACK_RIGHT_MOTOR
        );

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

                joystick.setX(x);
                joystick.setY(y);

                joystickUpdated = true;
            }
        });

        WSClient.registerCallable(0xB6, new RunnableWithArgs() {
            @Override
            public void run(Object... args) {
                int[] data = (int[]) args[0];

                double firstDouble = 0;
                double secondDouble = 0;

                // convert the first 8 bytes of the data to a double
                ByteBuffer buffer = ByteBuffer.allocate(8);
                for (int i = 0; i < 8; i++) {
                    buffer.put((byte) data[i]);
                }
                buffer.flip();
                firstDouble = buffer.getDouble();

                // convert the second 8 bytes of the data to a double
                buffer = ByteBuffer.allocate(8);
                for (int i = 8; i < 16; i++) {
                    buffer.put((byte) data[i]);
                }
                buffer.flip();
                secondDouble = buffer.getDouble();

                aiSpeed = secondDouble;
                aiRotation = firstDouble;

                //System.out.println(aiSpeed + ", " + aiRotation);
            }
        });

        arcadeDrive = new TankDrive(topLeft, topRight, bottomLeft, bottomRight);
    }

    private boolean switch_flag = false;

    /**
     * Main loop of the robot.
     * */
    @Override
    protected void loop() {
        if (!clock().elapsed(2000)) return; //just give some time for the connections to start up before the robot starts moving.

        if (manualControl) {
            double multiplier = 100d / 255;

            if (joystickUpdated) {
                if (Math.abs(joystick.getY()) > 0.05 || Math.abs(joystick.getX()) > 0.05) {
                    //arcadeDrive.drive(joystick.getY(), joystick.getX(), multiplier);
                    arcadeDrive.drive(joystick.getX() * multiplier, joystick.getY() * multiplier);

                    //System.out.println("Driving at speeds " + joystick.getX() * multiplier + ", " + joystick.getY() * multiplier);
                } else {
                    topLeft.setSpeed(0);
                    bottomLeft.setSpeed(0);
                    topRight.setSpeed(0);
                    bottomRight.setSpeed(0);
                }
                joystickUpdated = false;
            }
        } else {
            //create a polar vector from the AI speed and rotation
            Polar polar = new Polar(aiSpeed, Rotation2d.fromRadians(aiRotation));

            //get movements as vector (to simulate a "controller")
            Vector2d movement = polar.toVector();

           // arcadeDrive.drive(movement.getY(), movement.getX(), 100d / 255);
        }
    }
}
