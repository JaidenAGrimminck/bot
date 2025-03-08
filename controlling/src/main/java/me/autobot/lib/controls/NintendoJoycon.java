package me.autobot.lib.controls;

import me.autobot.lib.math.Mathf;
import me.autobot.lib.tools.RunnableWithArgs;
import me.autobot.lib.server.WSClient;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Nintendo Switch Joycon controller, both left and right combined into one class.
 * */
public class NintendoJoycon implements Gamepad {

    private static HashMap<Byte, NintendoJoycon> joycons = new HashMap<>();

    /**
     * Gets the joycon with the given event byte. If it does not exist, it will create a new joycon.
     * @param eventByte The event byte to get the joycon for.
     * @return The joycon with the given event byte.
     * */
    public static NintendoJoycon getJoycon(byte eventByte) {
        if (!joycons.containsKey(eventByte)) {
            new NintendoJoycon(eventByte);
        }

        return joycons.get(eventByte);
    }

    /**
     * The a button on the right joycon.
     * */
    public boolean a = false;

    /**
     * The b button on the right joycon.
     * */
    public boolean b = false;

    /**
     * The x button on the right joycon.
     * */
    public boolean x = false;

    /**
     * The y button on the right joycon.
     * */
    public boolean y = false;

    /**
     * The l button on the left joycon.
     * */
    public boolean l = false;

    /**
     * The r button on the right joycon.
     * */
    public boolean r = false;

    /**
     * The zl button on the left joycon.
     * */
    public boolean zl = false;

    /**
     * The zr button on the right joycon.
     * */
    public boolean zr = false;

    /**
     * The minus button on the left joycon.
     * */
    public boolean minus = false;

    /**
     * The plus button on the right joycon.
     * */
    public boolean plus = false;

    /**
     * The left stick button on the left joycon.
     * */
    public boolean leftStick = false;

    /**
     * The right stick button on the right joycon.
     * */
    public boolean rightStick = false;

    /**
     * The dpad up button on the left joycon.
     * */
    public boolean dpadUp = false;

    /**
     * The dpad down button on the left joycon.
     * */
    public boolean dpadDown = false;

    /**
     * The dpad left button on the left joycon.
     * */
    public boolean dpadLeft = false;

    /**
     * The dpad right button on the left joycon.
     * */
    public boolean dpadRight = false;

    /**
     * Right joycon left sl button.
     * */
    public boolean rightSl = false;

    /**
     * Right joycon right sr button.
     * */
    public boolean rightSr = false;

    /**
     * Left joycon left sl button.
     * */
    public boolean leftSl = false;

    /**
     * Left joycon right sr button.
     * */
    public boolean leftSr = false;

    /**
     * Right joycon home button.
     * */
    public boolean home = false;

    /**
     * Capture button.
     * */
    public boolean capture = false;

    /**
     * Charging grip button.
     * */
    public boolean chargingGrip = false;

    /**
     * The left joystick x value.
     * */
    public double leftStickX = 0;

    /**
     * The left joystick y value.
     * */
    public double leftStickY = 0;

    /**
     * The right joystick x value.
     * */
    public double rightStickX = 0;

    /**
     * The right joystick y value.
     * */
    public double rightStickY = 0;

    /**
     * Creates a new SwitchJoycons object.
     * @param eventByte The event byte to listen for (in the event addresses).
     * */
    public NintendoJoycon(byte eventByte) {
        //Register callable that listens for the event byte for the joycon.
        WSClient.registerCallable(eventByte, new RunnableWithArgs() {
            @Override
            public void run(Object... args) {
                int[] data = Mathf.allPos((int[]) args[0]);

                //takes in a list of 35 bytes.
                // first 3 bytes are the buttons
                // next 32 bytes are the joysticks

                // to keep the data at a minimum, use the standard of ~1kb per second.
                // this means ~30 updates per second, should be plenty for a controller.

                // first byte
                // 0b 0000 0000 EXPANDED:
                // 0: a
                // 1: b
                // 2: x
                // 3: y
                // 4: l
                // 5: r
                // 6: zl
                // 7: zr
                a = (data[0] & 0b00000001) == 1;
                b = (data[0] & 0b00000010) == 2;
                x = (data[0] & 0b00000100) == 4;
                y = (data[0] & 0b00001000) == 8;
                l = (data[0] & 0b00010000) == 16;
                r = (data[0] & 0b00100000) == 32;
                zl = (data[0] & 0b01000000) == 64;
                zr = (data[0] & 0b10000000) == 128;

                // second byte
                // 0b 0000 0000 EXPANDED:
                // 0: minus
                // 1: plus
                // 2: leftStick
                // 3: rightStick
                // 4: dpadLeft
                // 5: dpadRight
                // 6: dpadUp
                // 7: dpadDown
                minus = (data[1] & 0b00000001) == 1;
                plus = (data[1] & 0b00000010) == 2;
                leftStick = (data[1] & 0b00000100) == 4;
                rightStick = (data[1] & 0b00001000) == 8;
                dpadLeft = (data[1] & 0b00010000) == 16;
                dpadRight = (data[1] & 0b00100000) == 32;
                dpadUp = (data[1] & 0b01000000) == 64;
                dpadDown = (data[1] & 0b10000000) == 128;

                // third byte
                // 0b 0000 0000 EXPANDED:
                // 0: rightSl
                // 1: rightSr
                // 2: leftSl
                // 3: leftSr
                // 4: home
                // 5: capture
                // 6: chargingGrip
                // 7: unused

                rightSl = (data[2] & 0b00000001) == 1;
                rightSr = (data[2] & 0b00000010) == 2;
                leftSl = (data[2] & 0b00000100) == 4;
                leftSr = (data[2] & 0b00001000) == 8;
                home = (data[2] & 0b00010000) == 16;
                capture = (data[2] & 0b00100000) == 32;
                chargingGrip = (data[2] & 0b01000000) == 64;

                // then, the rest of the data is the joysticks values (each a double (8 bytes) and 4 doubles for the 2 joysticks)
                // 8 byte chunks:
                // 0: left x
                // 1: left y
                // 2: right x
                // 3: right y
                int[] joystickData = new int[32];
                System.arraycopy(data, 3, joystickData, 0, 32);

                ByteBuffer buffer = ByteBuffer.allocate(8);
                for (int i = 0; i < 8; i++) {
                    buffer.put((byte) joystickData[i]);
                }
                buffer.flip();
                leftStickX = buffer.getDouble();

                buffer = ByteBuffer.allocate(8);
                for (int i = 8; i < 16; i++) {
                    buffer.put((byte) joystickData[i]);
                }
                buffer.flip();
                leftStickY = buffer.getDouble();

                buffer = ByteBuffer.allocate(8);
                for (int i = 16; i < 24; i++) {
                    buffer.put((byte) joystickData[i]);
                }
                buffer.flip();
                rightStickX = buffer.getDouble();

                buffer = ByteBuffer.allocate(8);
                for (int i = 24; i < 32; i++) {
                    buffer.put((byte) joystickData[i]);
                }
                buffer.flip();
                rightStickY = buffer.getDouble();
            }
        });

        joycons.put(eventByte, this);
    }

    @Override
    public double getLeftX() {
        return leftStickX;
    }

    @Override
    public double getLeftY() {
        return leftStickY;
    }

    @Override
    public double getRightX() {
        return rightStickX;
    }

    @Override
    public double getRightY() {
        return rightStickY;
    }
}
