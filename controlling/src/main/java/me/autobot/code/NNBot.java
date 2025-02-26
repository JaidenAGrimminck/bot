package me.autobot.code;

import me.autobot.code.mechanisms.LIDAR;
import me.autobot.lib.os.OSDetector;
import me.autobot.lib.robot.PlayableRobot;
import me.autobot.lib.robot.Robot;
import me.autobot.lib.robot.drivebase.ArcadeDrive;
import me.autobot.lib.robot.motors.HoverboardWheel;
import me.autobot.lib.tools.RunnableWithArgs;
import me.autobot.server.WSClient;

import java.nio.ByteBuffer;

/**
 * Neural network bot.
 * */
@PlayableRobot(name = "Neural Network Bot")
public class NNBot extends Robot {

    private HoverboardWheel topLeft;
    private HoverboardWheel topRight;
    private HoverboardWheel bottomLeft;

    private HoverboardWheel bottomRight;

    private ArcadeDrive arcadeDrive;

    private LIDAR lidar;

    double speed = 0;
    double rotation = 0;


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
                MainBot.Config.TOP_LEFT_MOTOR_DIRECTION, MainBot.Config.TOP_LEFT_MOTOR
        );

        bottomLeft.connectToSerial(topLeftCommPort,
                MainBot.Config.BACK_LEFT_MOTOR_DIRECTION, MainBot.Config.BACK_LEFT_MOTOR
        );

        //right side is on a different port.
        System.out.println("Attempting to connect to right side...");

        topRight.connectToSerial(topRightCommPort,
                MainBot.Config.TOP_RIGHT_MOTOR_DIRECTION, MainBot.Config.TOP_RIGHT_MOTOR
        );

        bottomRight.connectToSerial(topRightCommPort,
                MainBot.Config.BACK_RIGHT_MOTOR_DIRECTION, MainBot.Config.BACK_RIGHT_MOTOR
        );

        System.out.println("Connected to serial ports!");

        arcadeDrive = new ArcadeDrive(topLeft, topRight, bottomLeft, bottomRight);

        WSClient.registerCallable(0x09, new RunnableWithArgs() {
            @Override
            public void run(Object... args) {
                int[] data = (int[]) args[0];

                // first 8 bytes are the rotation double
                byte[] rotationBytes = new byte[8];
                for (int i = 0; i < 8; i++) {
                    rotationBytes[i] = (byte) data[i];
                }

                // next 8 bytes are the speed double
                byte[] speedBytes = new byte[8];
                for (int i = 0; i < 8; i++) {
                    speedBytes[i] = (byte) data[i + 8];
                }

                rotation = ByteBuffer.wrap(rotationBytes).getDouble();
                speed = ByteBuffer.wrap(speedBytes).getDouble();
            }
        });

        lidar = new LIDAR();

        System.out.println("Attached move port at 0x09...");

        System.out.println("Setup complete!");
    }

    /**
     * Runs the robot.
     * */
    @Override
    protected void loop() {
        if (speed > 0.1) {
            arcadeDrive.drive(speed, rotation, 0.06);
        } else {
            arcadeDrive.drive(0, 0, 0);
        }
    }

    /**
     * Stops the robot.
     * */
    @Override
    protected void stop() {
        //set all motors to 0
        topLeft.setSpeed(0);
        topRight.setSpeed(0);
        bottomLeft.setSpeed(0);
        bottomRight.setSpeed(0);
    }
}
