package me.autobot.code;

import me.autobot.lib.robot.Robot;
import me.autobot.lib.hardware.serial.SerialConnection;
import me.autobot.server.WSServer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Bot for testing.
 * */
public class TestBot extends Robot {


    @Override
    protected void setup() {
        WSServer.wsstart();

        SerialConnection connection = new SerialConnection(38400, "/dev/cu.usbmodem101") {
            ArrayList<Byte> buffer = new ArrayList<>();

            boolean aligned = false;

            byte[] alignment = new byte[] {0x55, 0x56, 0x57, 0x58, 0x59, 0x60, 0x61, 0x62};

            float lastSeen = 0;

            @Override
            protected void onSerialData(byte[] data) {
                //concat buffer and data
                for (byte b : data) {
                    buffer.add(b);
                }

                // buffer will send 8 0x00 in a row. use this to get aligned with the data
                if (!aligned) {
                    if (buffer.size() >= 8) {
                        // the 0x00 may be in the middle of the data, so we need to find the first 0x00.
                        // there may also exist 0x00 standalone in the data

                        int firstZeroIndex = -1;
                        int countInARow = 0;
                        int highestCountInARow = 0;

                        for (int i = 0; i < buffer.size(); i++) {
                            if (buffer.get(i) == alignment[countInARow]) {
                                countInARow++;
                            } else {
                                if (countInARow > highestCountInARow) {
                                    highestCountInARow = countInARow;
                                }
                                countInARow = 0;
                            }

                            if (countInARow == 8) {
                                firstZeroIndex = i - 7;
                                break;
                            }
                        }

                        if (firstZeroIndex != -1) {
                            buffer.subList(0, firstZeroIndex).clear();
                            System.out.println("aligned.");
                            this.write(new int[] {0x05});
                            aligned = true;
                        } else {
                            System.out.println("not aligned, highest count: " + highestCountInARow);
                        }
                    }

                    return;
                }

                if (buffer.size() >= 4) {
                    System.out.println("pre: " + buffer.size());

                    //for each 4 bytes, print the float
                    for (int i = buffer.size() - 4; i < buffer.size() - 3; i++) {
                        byte[] bytes = new byte[] {buffer.get(i), buffer.get(i + 1), buffer.get(i + 2), buffer.get(i + 3)};
                        float f = ByteBuffer.wrap(bytes).getFloat();
                        System.out.println(f);

                        if (f > 2 && f < 500) {
                            if (f != lastSeen + 1) {
                                System.out.println("missing data between " + lastSeen + " and " + f);
                            }
                            lastSeen = f;
                        }

                    }

                    int bytesToRemove = buffer.size() / 4 * 4;
                    if (bytesToRemove > 0) {
                        buffer.subList(0, bytesToRemove).clear();

                        System.out.println("left in buffer: " + buffer.size());

                        // set the remaining data to the buffer
                    } else {
                        System.out.println("no data to remove");
                    }
                }
            }
        };

//        while (!connection.open()) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        System.out.println("Connected to serial port");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //System.out.println("Sending 30 to serial port");
                connection.write(new int[] {30});
            }
        };

        java.util.Timer timer = new java.util.Timer();
        timer.schedule(task, 1000, 10);
    }
}
