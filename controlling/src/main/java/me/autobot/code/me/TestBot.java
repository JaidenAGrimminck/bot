package me.autobot.code.me;

import me.autobot.lib.controls.NintendoJoycon;
import me.autobot.lib.robot.PlayableRobot;
import me.autobot.lib.robot.Robot;

/**
 * Bot for testing.
 * */
@PlayableRobot(name = "Test Bot")
public class TestBot extends Robot {

    private NintendoJoycon nintendoJoycon;

    @Override
    protected void setup() {
        nintendoJoycon = new NintendoJoycon((byte) 0xB5);
    }

    @Override
    protected void loop() {

    }
}
