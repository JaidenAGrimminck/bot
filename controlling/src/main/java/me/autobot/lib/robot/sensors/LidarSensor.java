package me.autobot.lib.robot.sensors;

import me.autobot.lib.math.Unit;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.hardware.serial.SerialConnection;
import me.autobot.lib.tools.RunnableWithArgs;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * A sensor that can interpret LIDAR data from the sensor hub.
 * This is supposed to be used with the YDLIDAR X4 PRO sensor.
 * */
public class LidarSensor extends Sensor {

    /**
     * The serial connection for the lidar sensor.
     * */
    public static class LidarSerialConnection extends SerialConnection {

        //if
        private boolean aligned = false;

        // alignment sequence
        private final byte[] alignment = {
                0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59
        };

        // message to send to start alignment
        private final byte[] startAlignment = {
                0x04
        };

        // message to send once alignment is complete
        private final byte[] finishedAlignment = {
                0x05
        };

        /**
         * Buffer for the serial data.
         * */
        private ArrayList<Byte> buffer = new ArrayList<>();

        private RunnableWithArgs onRecievedData = new RunnableWithArgs();

        /**
         * Creates a new serial connection with the comm port.
         *
         * @param commPort The comm port of the serial connection.
         */
        public LidarSerialConnection(String commPort) {
            super(9600, commPort);

            write(startAlignment);
        }

        /**
         * Take in the serial data and process it
         * @param data The data to process.
         * */
        @Override
        protected void onSerialData(byte[] data) {
            //concat buffer and data
            for (byte b : data) {
                buffer.add(b);
            }

            if (!aligned) {
                checkAlignment();
                return;
            }

            //todo: check for extra bytes. for now we'll assume its perfect

            if (buffer.size() < 8) return;

            byte[] distanceFloat = new byte[4];
            byte[] rotationFloat = new byte[4];

            for (int i = 0; i < 8; i++) {
                if (i < 4) distanceFloat[i] = buffer.get(i);
                else rotationFloat[i - 4] = buffer.get(i);
            }

            buffer.subList(0, 8).clear();

            float distance = ByteBuffer.wrap(distanceFloat, 0, Float.BYTES).getFloat();
            float rotation = ByteBuffer.wrap(rotationFloat, 0, Float.BYTES).getFloat();

            this.onRecievedData.run(distance, rotation);
        }

        /**
         * Check if the alignment is correct.
         * */
        private void checkAlignment() {
            if (buffer.size() < 8) return;

            // the alignment may exist everywhere in the data.

            int firstAlignmentIndex = -1;
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
                    firstAlignmentIndex = i - 7;
                    break;
                }
            }

            if (firstAlignmentIndex != -1) {
                buffer.subList(0, firstAlignmentIndex).clear();
                this.write(finishedAlignment);
                aligned = true;
            }
        }


    }

    private LidarSerialConnection serialConnection;

    /**
     * Creates a new LidarSensor.
     * @param identifier The identifier of the Lidar sensor.
     * */
    public LidarSensor(int identifier) {
        super(identifier, 2);
    }

    /**
     * Connects the LidarSensor to Serial
     * @param port The comm port to connect to.
     */
    @Override
    public void connectToSerial(String port) {
        if (getParent() == null) {
            throw new IllegalStateException("Cannot connect sensor to serial without a parent.");
        }

        if (inSimulation()) {
            //ignore this if we are in simulation
            return;
        }

        serialConnection = new LidarSerialConnection(port);

        serialConnection.onRecievedData = new RunnableWithArgs() {
            /**
             * Accept the distance and rotation from the lidar sensor.
             * @param args The arguments to pass to the runnable.
             * */
            @Override
            public void run(Object... args) {
                float distance = (float) args[0];
                float rotation = (float) args[1];

                updateData(distance, rotation);
            }
        };
    }

    /**
     * Update the internal raw sensor values
     * @param distance The distance from the lidar sensor to whatever object it may detect
     * @param rotation The rotation of the lidar sensor.
     * */
    protected void updateData(float distance, float rotation) {
        this.setSensorValues((double) distance, (double) rotation);
    }

    /**
     * Gets the rotation of the lidar sensor.
     * @return The rotation of the lidar sensor.
     * */
    public Rotation2d getRotation() {
        return Rotation2d.fromDegrees(this.getSensorValues()[1]);
    }

    /**
     * Gets the distance from the lidar sensor to whatever is being detected.
     * @return The distance from the lidar sensor.
     * */
    public Unit getDistance() {
        return new Unit(this.getSensorValues()[0], Unit.Type.CENTIMETER);
    }
}
