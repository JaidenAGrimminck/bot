package me.autobot.code;

import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.robot.Motor;
import me.autobot.lib.robot.Sensor;

import java.lang.reflect.Field;

//tank drive robot
public class Robot {
    private Motor topLeftMotor;
    private Motor topRightMotor;
    private Motor bottomLeftMotor;
    private Motor bottomRightMotor;
    
    private Vector2d position = new Vector2d(0, 0);

    private boolean totalSimulation = false;

    public Robot() {
        topLeftMotor = new Motor(0x1);
        topRightMotor = new Motor(0x02);
        bottomLeftMotor = new Motor(0x03);
        bottomRightMotor = new Motor(0x04);

        bottomRightMotor.invert();
        topRightMotor.invert();
    }

    public void startSimulation() {
        totalSimulation = true;

        //get all fields
        for (Field field : this.getClass().getDeclaredFields()) {
            //check if it extends sensor or device
            if (field.getType().getSuperclass().equals(Sensor.class)) {
                try {
                    Sensor sensor = (Sensor) field.get(this);
                    sensor.enableSimulation();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    

}
