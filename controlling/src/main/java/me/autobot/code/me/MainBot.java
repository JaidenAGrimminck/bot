package me.autobot.code.me;

import me.autobot.lib.controls.Gamepad;
import me.autobot.lib.controls.LogitechF310;
import me.autobot.lib.controls.NintendoJoycon;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.os.OSDetector;
import me.autobot.lib.robot.PlayableRobot;
import me.autobot.lib.robot.Robot;
import me.autobot.lib.robot.drivebase.TankDrive;
import me.autobot.lib.robot.motors.HoverboardWheel;

/**
 * Main robot class for the robot.
 * */
@PlayableRobot(name = "Main Robot")
public class MainBot extends Robot {
    /**
     * Config class for the robot.
     * */
    protected static class Config {
        /**
         * Top left motor ID
         * */
        public static final int TOP_LEFT_MOTOR = 0x09;
        /**
         * Back left motor ID
         * */
        public static final int BACK_LEFT_MOTOR = 0x0A;
        /**
         * Top right motor ID
         * */
        public static final int TOP_RIGHT_MOTOR = 0x09;
        /**
         * Back right motor ID
         * */
        public static final int BACK_RIGHT_MOTOR = 0x0A;

        /**
         * Top left motor direction
         * */
        public static final int TOP_LEFT_MOTOR_DIRECTION = 0x08;
        /**
         * Back left motor direction
         * */
        public static final int BACK_LEFT_MOTOR_DIRECTION = 0x0B;
        /**
         * Top right motor direction
         * */
        public static final int TOP_RIGHT_MOTOR_DIRECTION = 0x08;
        /**
         * Back right motor direction
         * */
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

    private Gamepad gamepad;

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
        //initialize the motors and set their max speeds to 0.5
        topLeft = new HoverboardWheel(0x01, 0x01);
        topLeft.setMaxSpeed(0.5);

        bottomLeft = new HoverboardWheel(0x02, 0x01);
        bottomLeft.setMaxSpeed(0.5);

        topRight = new HoverboardWheel(0x03, 0x02);
        topRight.setMaxSpeed(0.5);
        topRight.invert();

        bottomRight = new HoverboardWheel(0x04, 0x02);
        bottomRight.setMaxSpeed(0.5);
        bottomRight.invert();

        //register devices
        registerAllDevices();

        //connect devices to serial.
        System.out.println("Connecting to serial ports...");

        String topLeftCommPort = "/dev/cu.usbserial-10";
        String topRightCommPort = "/dev/cu.usbserial-110";

        if (OSDetector.usingLinux()) { // if we're on the raspberry pis
            System.out.println("Detected Linux, changing comm ports...");
            topLeftCommPort = "/dev/ttyUSB0";
            topRightCommPort = "/dev/ttyUSB1";
        }

        System.out.println("Attempting to connect to left side...");
        topLeft.connectToSerial(topLeftCommPort,
                Config.TOP_LEFT_MOTOR_DIRECTION, Config.TOP_LEFT_MOTOR
        );

        bottomLeft.connectToSerial(topLeftCommPort,
                Config.BACK_LEFT_MOTOR_DIRECTION, Config.BACK_LEFT_MOTOR
        );

        //right side is on a different port.
        System.out.println("Attempting to connect to right side...");

        topRight.connectToSerial(topRightCommPort,
                Config.TOP_RIGHT_MOTOR_DIRECTION, Config.TOP_RIGHT_MOTOR
        );

        bottomRight.connectToSerial(topRightCommPort,
                Config.BACK_RIGHT_MOTOR_DIRECTION, Config.BACK_RIGHT_MOTOR
        );

        System.out.println("Connected to serial ports!");

        gamepad = new LogitechF310("/gamepad1");

        arcadeDrive = new TankDrive(topLeft, topRight, bottomLeft, bottomRight);

        System.out.println("Setup complete!");
    }

    private boolean switch_flag = false;

    /**
     * Main loop of the robot.
     * */
    @Override
    protected void loop() {
        if (!clock().elapsed(2000)) return; //just give some time for the connections to start up before the robot starts moving.

//        if (manualControl) {
        double multiplier = 100d / 255;

//        if (nintendoJoycon.a) {
//            topLeft.setSpeed(0);
//            bottomLeft.setSpeed(0);
//            topRight.setSpeed(0);
//            bottomRight.setSpeed(0);
//            return;
//        }

        if (Math.abs(gamepad.getLeftY()) > 0.05 || Math.abs(gamepad.getRightY()) > 0.05) {
            //arcadeDrive.drive(joystick.getY(), joystick.getX(), multiplier);
            double m = 0.4;
            arcadeDrive.drive(gamepad.getLeftY() * multiplier * m, gamepad.getRightY() * multiplier * m);

            //System.out.println("Driving at speeds=" + joystick.getX() * multiplier + ", " + joystick.getY() * multiplier);
        } else {
            topLeft.setSpeed(0);
            bottomLeft.setSpeed(0);
            topRight.setSpeed(0);
            bottomRight.setSpeed(0);
        }

//        } else {
//            //create a polar vector from the AI speed and rotation
//            Polar polar = new Polar(aiSpeed, Rotation2d.fromRadians(aiRotation));
//
//            //get movements as vector (to simulate a "controller")
//            Vector2d movement = polar.toVector();
//
//           // arcadeDrive.drive(movement.getY(), movement.getX(), 100d / 255);
//        }
    }

    @Override
    protected void stop() {
        topLeft.setSpeed(0);
        bottomLeft.setSpeed(0);
        topRight.setSpeed(0);
        bottomRight.setSpeed(0);
    }
}
