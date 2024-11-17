package me.autobot.lib.controls;

import me.autobot.lib.math.Mathf;
import me.autobot.lib.tools.RunnableWithArgs;
import me.autobot.server.WSClient;

import java.nio.ByteBuffer;

public class SwitchJoycons {
    public boolean a = false;
    public boolean b = false;
    public boolean x = false;
    public boolean y = false;
    public boolean l = false;
    public boolean r = false;
    public boolean zl = false;
    public boolean zr = false;
    public boolean minus = false;
    public boolean plus = false;
    public boolean leftStick = false;
    public boolean rightStick = false;
    public boolean dpadUp = false;
    public boolean dpadDown = false;
    public boolean dpadLeft = false;
    public boolean dpadRight = false;

    public double leftStickX = 0;
    public double leftStickY = 0;
    public double rightStickX = 0;
    public double rightStickY = 0;

    /**
     * Creates a new SwitchJoycons object.
     * @param eventByte The event byte to listen for.
     * */
    public SwitchJoycons(byte eventByte) {
        WSClient.registerCallable(eventByte, new RunnableWithArgs() {
            @Override
            public void run(Object... args) {
                int[] data = Mathf.allPos((int[]) args[0]);

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

                // then, the rest of the data is the joysticks values (each a double (8 bytes) and 4 doubles for the 2 joysticks)
                // bytes:
                // 0: left x
                // 1: left y
                // 2: right x
                // 3: right y
                int[] joystickData = new int[32];
                System.arraycopy(data, 2, joystickData, 0, 32);

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
    }
    
    
}
