package me.autobot.code.mechanisms;

import me.autobot.lib.hardware.ws.WSSensorConnection;
import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.objects.Rectangle;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.systems.mechanisms.Mechanism;

import java.util.ArrayList;

/**
 * LIDAR Mechanism that can construct a terrain map from LIDAR data.
 * */
public class LIDAR extends Mechanism {
    ArrayList<Rectangle> rectangles = new ArrayList<>();

    ArrayList<Vector2d> points = new ArrayList<>();

    /**
     * LIDAR Point structure.
     * */
    public class Point {
        float distance;
        float angle;
        float intensity;

        /**
         * Creates a new LIDAR Point structure.
         * @param distance The distance of the point.
         * @param angle The angle of the point.
         * @param intensity The intensity of the point.
         * */
        public Point(float distance, float angle, float intensity) {
            this.distance = distance;
            this.angle = angle;
            this.intensity = intensity;
        }
    }

    /**
     * The serial connection for the lidar sensor.
     * */
    public class WSLidarSensorConnection extends WSSensorConnection {
        /**
         * Response for the LIDAR sensor.
         * */
        public static class Response {
            /**
             * Called when the LIDAR sensor has new data.
             * @param points The points from the LIDAR sensor.
             * */
            public void onLidarData(Point[] points) {

            }
        }

        private ArrayList<Response> responses = new ArrayList<>();

        /**
         * Creates a new WSLidarSensorConnection.
         * */
        public WSLidarSensorConnection() {
            super();
        }

        /**
         * Called when the LIDAR sensor has new data.
         * @param data The data from the LIDAR sensor.
         * */
        @Override
        public void onUpdate(int[] data) {
            // first two bytes are 0xA55A
            if (data[0] != 0xA5 || data[1] != 0x5A) {
                return;
            }

            // next byte is if there was an error
            if (data[2] != 0) {
                return;
            }

            // next 4 bytes is the timestamp (unsigned int)
            int timestamp = data[3] << 24 | data[4] << 16 | data[5] << 8 | data[6];

            // next 4 bytes is the range (float)
            float range = Float.intBitsToFloat(data[7] << 24 | data[8] << 16 | data[9] << 8 | data[10]);

            // next 4 bytes is the number of points (unsigned int)
            int numPoints = data[11] << 24 | data[12] << 16 | data[13] << 8 | data[14];

            // copy the rest of the bytes to the point data
            int[] pointData = new int[data.length - 15];
            System.arraycopy(data, 15, pointData, 0, pointData.length);

            // each point is 12 bytes
            // offset byte 0...3: rotation (float)
            // offset byte 4...7: range (float)
            // offset byte 8...11: intensity (float)

            Point[] points = new Point[numPoints];

            int onPoint = 0;
            for (int i = 0; i < pointData.length; i += Float.BYTES * 3) {
                float rotation = Float.intBitsToFloat(pointData[i] << 24 | pointData[i + 1] << 16 | pointData[i + 2] << 8 | pointData[i + 3]);
                float distance = Float.intBitsToFloat(pointData[i + 4] << 24 | pointData[i + 5] << 16 | pointData[i + 6] << 8 | pointData[i + 7]);
                float intensity = Float.intBitsToFloat(pointData[i + 8] << 24 | pointData[i + 9] << 16 | pointData[i + 10] << 8 | pointData[i + 11]);

                points[onPoint] = new Point(distance, rotation, intensity);

                onPoint++;
            }
        }

        /**
         * Adds a response to the LIDAR sensor.
         * @param response The response to add.
         * */
        public void addResponse(Response response) {
            responses.add(response);
        }

        /**
         * Gets the ID of the sensor.
         * @return The ID of the sensor.
         */
        @Override
        public int getId() {
            return 0xDD;
        }
    }

    private WSLidarSensorConnection lidarSensor;

    /**
     * Initializes the LIDAR mechanism.
     * */
    @Override
    public void init() {
        lidarSensor = new WSLidarSensorConnection();
        lidarSensor.addResponse(new WSLidarSensorConnection.Response() {
            @Override
            public void onLidarData(Point[] points) {
                LIDAR.this.onLidarData(points);
            }
        });
    }

    /**
     * Updates the LIDAR mechanism.
     * */
    @Override
    protected void update() {

    }

    /**
     * Called when the LIDAR sensor has new data.
     * @param points The points from the LIDAR sensor.
     * */
    protected void onLidarData(Point[] points) {
        // clear points
        this.points.clear();

    }
}
