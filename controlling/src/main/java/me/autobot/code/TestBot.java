package me.autobot.code;

import me.autobot.lib.controls.Joycon;
import me.autobot.lib.robot.PlayableRobot;
import me.autobot.lib.robot.Robot;
import me.autobot.lib.hardware.serial.SerialConnection;
import me.autobot.server.WSServer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Bot for testing.
 * */
@PlayableRobot(name = "Test Bot")
public class TestBot extends Robot {

    private Joycon joycon;

    @Override
    protected void setup() {
        joycon = new Joycon((byte) 0xB5);
    }

    @Override
    protected void loop() {

    }
}
