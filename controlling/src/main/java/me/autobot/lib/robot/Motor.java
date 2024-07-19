package me.autobot.lib.robot;

import me.autobot.lib.logging.Log;
import me.autobot.lib.math.Mathf;

public class Motor extends Device {
    //assuming the max motor properties

    private static double maxVoltage = 36; //same as battery
    private static double maxCurrent = 19; //split. 20 is max tech, but that'll trip the fuse

    private boolean isInverted = false;

    @Log
    private double speed = 0; //[-1, 1], 0 is stop

    private int address = 0; //i2c address

    public Motor(int address) {
        super();

        this.address = address;
    }

    public void setSpeed(double speed) {
        this.speed = (isInverted ? -1 : 1) * Mathf.clamp(speed, -1, 1);
        reportSpeed();
    }
    
    public void stop(double speed) {
        this.speed = 0;
        reportSpeed();
    }

    public void invert() {
        isInverted = !isInverted;
    }

    public void brake() {
        //report to motor controller using i2c directly
    }

    private void reportSpeed() {
        //report speed to motor controller
        //use i2c, todo
    }

    @Override
    public void emergencyStop() {
        stop(0);
        brake(); //idk about brake but yeah ig
    }
}
