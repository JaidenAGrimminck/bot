package me.autobot.code;

import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.robot.Motor;

//tank drive robot
public class Robot {
    private Motor topLeftMotor;
    private Motor topRightMotor;
    private Motor bottomLeftMotor;
    private Motor bottomRightMotor;
    
    private Vector2d position = new Vector2d(0, 0);

    public Robot() {
        topLeftMotor = new Motor(0x1);
        topRightMotor = new Motor(0x02);
        bottomLeftMotor = new Motor(0x03);
        bottomRightMotor = new Motor(0x04);

        bottomRightMotor.invert();
        topRightMotor.invert();
    }

    

}
