package me.autobot.code.mechanisms;

import me.autobot.lib.hardware.ws.WSSensorConnection;
import me.autobot.lib.math.Mathf;
import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.objects.Rectangle;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.systems.mechanisms.Mechanism;
import me.autobot.lib.tools.RunnableWithArgs;
import me.autobot.server.WSClient;

import java.nio.ByteBuffer;
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

            int numPoints = Mathf.getIntFromBytes(data, 11, 4, true);


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
                float rotation = Mathf.getFloatFromBytes(pointData, i, 4, true);
                float distance = Mathf.getFloatFromBytes(pointData, i + 4, 4, true);
                float intensity = Mathf.getFloatFromBytes(pointData, i + 8, 4, true);

                points[onPoint] = new Point(distance, rotation, intensity);

                onPoint++;
            }

            for (Response response : responses) {
                response.onLidarData(points);
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

    private static ArrayList<WSClient> subscribers = new ArrayList<>();


    static {
        System.out.println("[Mechanism] Registering LIDAR at 0xA5 callable for subscription. See class docs for details.");
        // callable `0xA5` (1) | (1) is whether to subscribe or not, 0 is no, 1 is yes

        WSClient.registerCallable(0xA5, new RunnableWithArgs() {
            @Override
            public void run(WSClient client, int[] data) {
                if (data[0] == 1) {
                    subscribers.add(client);
                } else {
                    subscribers.remove(client);
                }
            }
        });
    }


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

        System.out.println("LIDAR received " + points.length + " points.");

        if (subscribers.isEmpty()) {
            return;
        }

        byte[] point_payload = new byte[points.length * 12];

        for (int i = 0; i < points.length; i++) {
            byte[] distance = ByteBuffer.allocate(4).putFloat(points[i].distance).array();
            byte[] angle = ByteBuffer.allocate(4).putFloat(points[i].angle).array();
            byte[] intensity = ByteBuffer.allocate(4).putFloat(points[i].intensity).array();

            System.arraycopy(distance, 0, point_payload, i * 12, 4);
            System.arraycopy(angle, 0, point_payload, i * 12 + 4, 4);
            System.arraycopy(intensity, 0, point_payload, i * 12 + 8, 4);
        }

        byte[] finalPayload = new byte[point_payload.length + 4];
        System.arraycopy(point_payload, 0, finalPayload, 4, point_payload.length);

        finalPayload[0] = (byte) 0xA5;

        ArrayList<WSClient> toRemove = new ArrayList<>();

        for (WSClient client : subscribers) {
            if (!client.isOpen()) {
                toRemove.add(client);
                continue;
            }

            try {
                client.send(finalPayload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        subscribers.removeAll(toRemove);
    }
}
