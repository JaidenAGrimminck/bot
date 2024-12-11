package me.autobot.lib.robot;

import me.autobot.lib.telemetry.Log;
import me.autobot.lib.telemetry.Logger;
import me.autobot.lib.math.Clock;
import me.autobot.lib.math.objects.Geometry;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.odometry.SimpleOdometry2d;
import me.autobot.lib.telemetry.SysoutMiddleman;
import me.autobot.server.Server;
import me.autobot.server.WSClient;
import me.autobot.sim.Simulation;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * The general robot class.
 * This can be extended to create a robot with sensors and motors.
 * **/
public class Robot implements Logger {

    /**
     * List of all the robot classes.
     * */
    private static ArrayList<Class<? extends Robot>> robotClasses = new ArrayList<>();

    /**
     * Whether the robot selected can be started.
     * */
    private static boolean editable = true;

    /**
     * The current robot instance.
     * */
    private static Robot currentRobot;

    /**
     * Starts all the robots (adds them to the editable list available for selection).
     * */
    public static void start() {
        SysoutMiddleman.start();

        // use dex loader to get all robots with @PlayableRobot annotations
        Reflections reflections = new Reflections("me.autobot.code");
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(PlayableRobot.class);

        for (Class<?> clazz : annotated) {
            if (Robot.class.isAssignableFrom(clazz)) {
                robotClasses.add((Class<? extends Robot>) clazz);
                System.out.println("[INFO] Added robot " + clazz.getSimpleName() + " to the list of robots.");
            }
        }

        editable = true;

        Server.start();
    }

    /**
     * Starts a singular robot.
     * @param robot The robot to start.
     * */
    public static void start(Robot robot) {
        SysoutMiddleman.start();

        if (currentRobot != null) {
            System.out.println("[WARNING] Robot already started, stopping current robot.");
            currentRobot.stopLoop();
        }

        robotClasses.add(robot.getClass());

        editable = false;

        currentRobot = robot;

        Server.start();
    }

    /**
     * Starts a robot by index.
     * @param index The index of the robot to start.
     * */
    public static void startRobot(int index) {
        if (currentRobot != null) {
            System.out.println("[WARNING] Robot already started, stopping current robot.");
            currentRobot.stopLoop();
        }

        try {
            Robot robot = robotClasses.get(index).getDeclaredConstructor().newInstance();
            currentRobot = robot;
            System.out.println("[INFO] Started robot " + robot.getClass().getSimpleName() + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the current robot.
     * */
    public static void disableRobot() {
        if (currentRobot != null) {
            currentRobot.stopLoop();
            System.out.println("[INFO] Stopped current robot.");
            currentRobot = null;
        }
    }

    /**
     * Pauses the current robot.
     * */
    public static void pauseRobot() {
        if (currentRobot != null) {
            currentRobot.setPaused(true);
            System.out.println("[INFO] Paused current robot.");
        }
    }

    /**
     * Resumes the current robot.
     * */
    public static void resumeRobot() {
        if (currentRobot != null) {
            currentRobot.setPaused(false);
            System.out.println("[INFO] Resumed current robot.");
        }
    }

    private static ArrayList<WSClient> wsClients = new ArrayList<>();
    private static Timer wsTimer = new Timer();

    /**
     * Subscribes a WSClient to the status of the robot.
     * @param client The client to subscribe.
     * @param subscribe Whether to subscribe or unsubscribe.
     * */
    public static void subscribeToStatus(WSClient client, boolean subscribe) {
        if (subscribe) {
            if (wsClients.isEmpty()) {
                wsTimer.cancel();

                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        //`0x6C`, (1), (2...9), (10), (11...26) | This is a response to a subscription to the robot status. (1) is the index of the current robot (`0xFF` means no robot selected), (2...5) is a long with the current robot clock, (6) is a bit, following: `[editable, playing(0)/paused(1), 1...7]` (7...26) is reserved (I forgot what I wanted to put there).     |
                        byte[] data = new byte[26];
                        data[0] = 0x6C;
                        if (currentRobot == null) {
                            data[1] = (byte) 0xFF;
                        } else {
                            data[1] = (byte) robotClasses.indexOf(currentRobot.getClass());
                        }

                        ByteBuffer buffer = ByteBuffer.allocate(8);
                        if (currentRobot != null) buffer.putLong(currentRobot.clock().getTimeElapsed());
                        else buffer.putLong(0);

                        for (int i = 0; i < 8; i++) {
                            data[i + 2] = buffer.get(i);
                        }

                        byte status = 0;
                        if (editable) {
                            status |= (byte) 0b10000000;
                        }

                        if (currentRobot != null) {
                            if (currentRobot.paused) {
                                status |= 0b01000000;
                            }
                        }

                        data[10] = status;

                        ArrayList<WSClient> toRemove = new ArrayList<>();

                        for (WSClient client : wsClients) {
                            if (!client.isOpen()) {
                                toRemove.add(client);
                                System.out.println("[INFO] Removed inactive client from robot status.");
                            } else {
                                try {
                                    client.send(data);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        wsClients.removeAll(toRemove);
                    }
                };

                wsTimer = new Timer();
                wsTimer.schedule(task, 0, 100);
            }

            wsClients.add(client);

            System.out.println("[INFO] Subscribed client to robot status.");
        } else {
            wsClients.remove(client);

            if (wsClients.isEmpty()) {
                wsTimer.cancel();
            }
        }
    }

    private final static ArrayList<WSClient> telemetryClients = new ArrayList<>();
    private static boolean setupTelemetryListeners = false;

    /**
     * Removes all inactive clients from the telemetry list.
     * */
    public static void removeAllInactiveTelemetryClients() {
        ArrayList<WSClient> toRemove = new ArrayList<>();

        int n_removed = 0;

        for (WSClient c : telemetryClients) {
            if (!c.isOpen()) {
                toRemove.add(c);
                n_removed++;
            }
        }

        telemetryClients.removeAll(toRemove);

        if (n_removed > 0) {
            System.out.println("[INFO] Removed " + n_removed + " inactive clients from robot telemetry.");
        }
    }

    /**
     * Subscribes a WSClient to the telemetry of the robot.
     * @param client The client to subscribe.
     *               @see WSClient
     * @param subscribe Whether to subscribe or unsubscribe.
     * */
    public static void subscribeToTelemetry(WSClient client, boolean subscribe) {
        if (subscribe) {
            if (!setupTelemetryListeners) {
                //`0x6D`, (1), (2), ...
                // This is a response to a subscription to the telemetry data,
                // where (1) indicates whether the telemetry data is a start (`0x01`) or an update (`0x00`),
                // and (2) is the type (0 = out, 1 = err).
                //

                SysoutMiddleman.Sysout.Listener listener = (msg) -> {
                    //if (telemetryClients.isEmpty()) return;

                    byte[] data = new byte[msg.getMessage().length() * Character.BYTES + 3];
                    data[0] = 0x6D;
                    data[1] = 0x00;
                    data[2] = (byte) msg.getType();

                    ByteBuffer buffer = ByteBuffer.allocate(msg.getMessage().length() * Character.BYTES);
                    for (char c : msg.getMessage().toCharArray()) {
                        buffer.putChar(c);
                    }

                    for (int i = 0; i < buffer.position(); i++) {
                        data[i + 3] = buffer.get(i);
                    }

                    for (WSClient c : telemetryClients) {
                        if (!c.isOpen()) continue;
                        try {
                            c.send(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    removeAllInactiveTelemetryClients();
                };

                SysoutMiddleman.Sysout.getOut().addListener(listener);
                SysoutMiddleman.Sysout.getError().addListener(listener);

                setupTelemetryListeners = true;
            }

            telemetryClients.add(client);

            System.out.println("[INFO] Subscribed client to robot telemetry.");

            // Send initial message
            int payloadLen = 3;
            for (int i = 0; i < SysoutMiddleman.getMessages().size(); i++) {
                payloadLen += SysoutMiddleman.getMessages().get(i).getMessage().length() * Character.BYTES + 1;
            }

            byte[] payload = new byte[payloadLen];
            payload[0] = 0x6D;
            payload[1] = 0x01;
            payload[2] = 0x00;

            int index = 3;
            for (int i = 0; i < SysoutMiddleman.getMessages().size(); i++) {
                byte[] data = new byte[SysoutMiddleman.getMessages().get(i).getMessage().length() * Character.BYTES + 1];
                data[0] = (byte) SysoutMiddleman.getMessages().get(i).getType();

                ByteBuffer buffer = ByteBuffer.allocate(SysoutMiddleman.getMessages().get(i).getMessage().length() * Character.BYTES);
                for (char c : SysoutMiddleman.getMessages().get(i).getMessage().toCharArray()) {
                    buffer.putChar(c);
                }

                for (int j = 0; j < buffer.position(); j++) {
                    data[j + 1] = buffer.get(j);
                }

                for (byte datum : data) {
                    payload[index] = datum;
                    index++;
                }

                //System.out.println("msg size: " + data.length + " - " + SysoutMiddleman.getMessages().get(i).getMessage());
            }

            try {
                client.send(payload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            telemetryClients.remove(client);
        }
    }

    //static methods
    private static boolean totalSimulation = false;

    /**
     * Starts the simulation for all robots.
     */
    public static void startSimulation() {
        totalSimulation = true;
        for (Robot robot : robots) {
            robot.switchDevicesToSimulation();
        }
    }

    private static ArrayList<Robot> robots = new ArrayList<>();

    /**
     * Gets all the robots.
     * @return An array list of all the robots.
     */
    public static ArrayList<Robot> getRobots() {
        return robots;
    }

    /**
     * Gets all robot classes.
     * @return An array list of all robot classes.
     */
    public static ArrayList<Class<? extends Robot>> getRobotClasses() {
        return robotClasses;
    }

    /**
     * Gets the first robot / only robot if only one robot is created (for multi-robot simulations). If there is only one controlling robot, this will return that robot.
     * @return A robot instance.
     */
    public static Robot getInstance() {
        if (currentRobot != null) {
            return currentRobot;
        }

        return robots.get(0);
    }


    private Vector2d robotSize = new Vector2d(40, 60);

    @Log
    private SimpleOdometry2d odometry;

    private boolean hasCrashed = false;

    @Log
    private byte identification;

    @Log
    private HashMap<String, Integer> flags = new HashMap<>();

    private long loopTime = 50;
    private final Timer loopTimer;

    private long timeCreated = 0;

    private boolean paused = false;

    private Clock clock;

    /**
     * Creates a new, basic robot.
     * This robot has no sensors or motors.
     * To create a robot with sensors and motors, extend this class and add the sensors and motors.
     * */
    public Robot() {
        identification = (byte) robots.size();

        robots.add(this);

        this.setup();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (paused) return;

                loop();
            }
        };

        loopTimer = new Timer();
        loopTimer.schedule(task, loopTime, loopTime);

        timeCreated = System.currentTimeMillis();
        clock = new Clock();
    }

    /**
     * Initializes the odometry of the robot.
     * This is used to keep track of the position and rotation of the robot.
     * @param odometry The odometry of the robot.
     *                 @see SimpleOdometry2d
     * */
    protected void initializeOdometry(SimpleOdometry2d odometry) {
        this.odometry = odometry;
    }

    /**
     * Sets the size of the robot.
     * This is used for collision detection.
     * @param sizeVector The size of the robot.
     * */
    protected void setRobotSize(Vector2d sizeVector) {
        robotSize = sizeVector;
    }

    /**
     * Gets the identification of the robot.
     * This is a unique identifier for the robot, used generally for communication with multiple simulated robots.
     * If you have only one robot, you can generally ignore this, as the value will be 0x00.
     * @return The identification of the robot.
     * */
    public byte getIdentification() {
        return identification;
    }

    /***
     * Pauses the robot.
     * @param paused Whether the robot should be paused.
     * */
    public void setPaused(boolean paused) {
        this.paused = paused;
        this.clock.setPaused(paused);
    }

    /***
     * Switches all sensors to simulation mode.
     * This method is called when the simulation starts.
     * It is not recommended to call this method manually.
     * @see #startSimulation()
     * @see #switchDevicesToSimulation()
     * */
    protected void switchDevicesToSimulation() {
        ArrayList<Device> devices = getDevices();
        for (Device device : devices) {
            device.enableSimulation();
        }
    }

    /**
     * Moves the robot by a certain amount.
     * @param a The amount to move in the x direction.
     *          Positive values move the robot to the right.
     *          Negative values move the robot to the left.
     * @param b The amount to move in the y direction.
     *          Positive values move the robot up.
     *          Negative values move the robot down.
     * @see #rotate(double)
     * **/
    public void move(double a, double b) {
        if (hasCrashed) return;
        odometry.move(new Vector2d(a, b));
        onMove();
    }

    /**
     * Rotates the robot by a certain amount.
     * @param theta The amount to rotate the robot by.
     *              Positive values rotate the robot clockwise.
     *              Negative values rotate the robot counterclockwise.
     *              The angle is in radians.
     *              @see Math#toRadians(double)
     *              @see Math#toDegrees(double)
     *              @see Rotation2d#fromDegrees(double)
     *              @see Rotation2d#getTheta()
     * @see #move(double, double)
     * **/
    public void rotate(double theta) {
        if (hasCrashed) return;
        odometry.rotate(Rotation2d.fromRadians(theta));
        onMove();
    }

    /**
     * Event called whenever the robot moves.
     * Override this method with:
     * <code>
     * &#64;Override
     * protected void onMove() {
     *     //your code here
     *     //this code will be executed whenever the robot moves
     * }
     * </code>
     * */
    protected void onMove() {

    }

    /**
     * Setup method.
     * This method is called when the robot is created.
     * Override this method with:
     * <code>
     * &#64;Override
     * protected void setup() {
     *    //your code here
     *    //this code will be executed when the robot is created
     * }
     * </code>
     * **/
    protected void setup() {

    }

    /**
     * Loop method.
     * This method is called every loop of the simulation.
     * Override this method with:
     * <code>
     *     &#64;Override
     *     protected void loop() {
     *     //your code here
     *     //this code will be executed every loop of the simulation
     *     }
     * </code>
     * */
    protected void loop() {

    }

    /**
     * Stop method.
     * This method is called when the robot is stopped.
     * Override this method with:
     * <code>
     *     &#64;Override
     *     protected void stop() {
     *     //your code here
     *     //this code will be executed when the robot is stopped
     *     }
     * </code>
     * */
    protected void stop() {

    }

    /**
     * Returns the position of the robot using the odometry.
     * If the odometry is not set, it'll return a zero vector.
     * @return The position of the robot.
     * **/
    public Vector2d getPosition() {
        if (odometry == null) return Vector2d.zero();
        return odometry.getPosition();
    }

    /**
     * Returns the rotation of the robot using the odometry.
     * If the odometry is not set, it'll return a zero rotation.
     * @return The rotation of the robot.
     * **/
    public Rotation2d getRotation() {
        if (odometry == null) return Rotation2d.zero();
        return odometry.getRotation();
    }

    /**
     * Gets all the sensors of the robot.
     * @return An array list of all the sensors of the robot.
     * @see Device
     * @see Sensor
     * */
    public ArrayList<Sensor> getSensors() {
        ArrayList<Sensor> sensors = new ArrayList<>();
        for (Field field : this.getClass().asSubclass(this.getClass()).getDeclaredFields()) {
            if ((field.getType().getSuperclass() == null ? field.getType() : field.getType().getSuperclass()).equals(Sensor.class)) {
                boolean accessible = field.canAccess(this);

                field.setAccessible(true);

                try {
                    sensors.add((Sensor) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                field.setAccessible(accessible);
            }
        }
        return sensors;
    }

    /**
     * Gets all the motors of the robot.
     * @return An array list of all the motors of the robot.
     * @see Device
     * @see Motor
     * */
    public ArrayList<Motor> getMotors() {
        ArrayList<Motor> motors = new ArrayList<>();
        for (Field field : this.getClass().asSubclass(this.getClass()).getDeclaredFields()) {
            if ((field.getType().getSuperclass() == null ? field.getType() : field.getType().getSuperclass()).equals(Motor.class)) {
                boolean accessible = field.canAccess(this);

                field.setAccessible(true);

                try {
                    motors.add((Motor) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                field.setAccessible(accessible);
            }
        }
        return motors;
    }

    /**
     * Gets all the devices of the robot.
     * @return An array list of all the devices of the robot.
     * @see Device
     * */
    public ArrayList<Device> getDevices() {
        ArrayList<Device> devices = new ArrayList<>();
        for (Field field : this.getClass().asSubclass(this.getClass()).getDeclaredFields()) {
            Class<?> topLevel = (field.getType().getSuperclass() == null ? field.getType() : field.getType().getSuperclass());
            while (topLevel.getSuperclass() != null) {
                topLevel = topLevel.getSuperclass();

                if (topLevel == Device.class) {
                    break;
                }
            }

             if (topLevel.equals(Device.class)) {
                boolean accessible = field.canAccess(this);

                field.setAccessible(true);

                try {
                    devices.add((Device) field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                field.setAccessible(accessible);
            }
        }
        return devices;
    }

    /**
     * Registers all the devices of the robot.
     * This should be called after assigning all the devices to the robot, but BEFORE connecting them to I2C.
     * */
    protected void registerAllDevices() {
        getDevices().forEach(device -> {
            if (device == null) {
                System.out.println("[WARNING] Device is null, skipping registration!");
                return;
            }
            device.setParent(this);
        });
    }

    /**
     * Gets the size of the robot.
     * @return The size of the robot.
     * */
    public Vector2d getRobotSize() {
        return robotSize;
    }

    /**
     * Checks if the robot is in collision with any obstacles.
     * @return True if the robot is in collision with any obstacles, false otherwise.
     * */
    public boolean inCollision() {
        if (totalSimulation) {
            // get obstacles
            ArrayList<Box2d> obstacles = (ArrayList<Box2d>) Simulation.getInstance().environment.obstacles.clone();

            // get robot position
            Vector2d pos = getPosition();
            // get robot rotation
            Rotation2d rot = getRotation();

            //sort the box2d points by distance to the robot
            obstacles.sort((o1, o2) -> {
                double d1 = o1.getPosition().distance(pos.toInt2());
                double d2 = o2.getPosition().distance(pos.toInt2());
                return Double.compare(d1, d2);
            });

            Vector2d topLeft = new Vector2d(-robotSize.getX() / 2, -robotSize.getY() / 2);
            Vector2d topRight =  new Vector2d(robotSize.getX() / 2, -robotSize.getY() / 2);
            Vector2d bottomLeft = new Vector2d(-robotSize.getX() / 2, robotSize.getY() / 2);
            Vector2d bottomRight = new Vector2d(robotSize.getX() / 2, robotSize.getY() / 2);

            //rotate the points
            topLeft = topLeft.rotate(rot);
            topRight = topRight.rotate(rot);
            bottomLeft = bottomLeft.rotate(rot);
            bottomRight = bottomRight.rotate(rot);

            Vector2d[] list = new Vector2d[] {topLeft, topRight, bottomLeft, bottomRight};

            //add the position to the points
            for (int i = 0; i < list.length; i++) {
                list[i] = pos.add(list[i]);
            }


            for (Box2d obstacle : obstacles) {
                if (Geometry.twoPolygonsIntersecting(list, obstacle.getVertices())) {
                    hasCrashed = true;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the odometry of the robot.
     * @return The odometry of the robot.
     */
    public SimpleOdometry2d getOdometry() {
        return odometry;
    }

    /**
     * Sets the odometry of the robot.
     * @param odometry The odometry of the robot.
     */
    protected void setOdometry(SimpleOdometry2d odometry) {
        this.odometry = odometry;
    }

    /**
     * Sets a flag for the robot.
     * @param key The key of the flag.
     * @param value The value of the flag.
     * */
    public void setFlag(String key, int value) {
        flags.put(key, value);
    }

    /**
     * Gets a flag for the robot.
     * @param key The key of the flag.
     * @return The value of the flag.
     * */
    public int getFlag(String key) {
        return flags.get(key);
    }

    /**
     * Resets the robot.
     * This method resets the robot's position and rotation to the starting position and rotation.
     * */
    public void reset() {
        hasCrashed = false;
        odometry.reset();
    }

    /**
     * Cancels the loop timer.
     * Will stop the loop method from being called in the future.
     * */
    public void stopLoop() {
        loopTimer.cancel();
        stop();
    }

    /**
     * Sets the loop time.
     * @param loopTime The time between each loop.
     */
    public void setLoopTime(long loopTime) {
        this.loopTime = loopTime;
    }

    /**
     * Gets the loop time.
     * @return The time between each loop.
     */
    public long getLoopTime() {
        return loopTime;
    }

    /**
     * Gets the time elapsed since the robot was created.
     * @return The time elapsed in milliseconds.
     * */
    public long getTimeElapsed() {
        return System.currentTimeMillis() - timeCreated;
    }

    /**
     * Gets the clock of the robot.
     * @return The clock of the robot.
     */
    public Clock clock() {
        return clock;
    }
}
