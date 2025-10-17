package me.autobot.lib.robot;

import me.autobot.lib.systems.mechanisms.Mechanism;
import me.autobot.lib.telemetry.Log;
import me.autobot.lib.telemetry.Logger;
import me.autobot.lib.math.Clock;
import me.autobot.lib.math.objects.Geometry;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.odometry.SimpleOdometry2d;
import me.autobot.lib.telemetry.SysoutMiddleman;
import me.autobot.lib.server.Server;
import me.autobot.lib.server.WSClient;
import me.autobot.lib.server.topica.Topica;
import me.autobot.lib.tools.suppliers.ByteSupplier;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * The general robot class.
 * This can be extended to create a robot with sensors and motors.
 * **/
public abstract class Robot implements Logger {

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
        Topica.createDatabase();
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

        new Topica.Database.Topic("/robot/selected", "");

        Server.start();
        Topica.start();
    }

    /**
     * Starts a singular robot.
     * @param robot The robot to start.
     * */
    public static void start(Robot robot) {
        Topica.createDatabase();
        SysoutMiddleman.start();

        if (currentRobot != null) {
            System.out.println("[WARNING] Robot already started, stopping current robot.");
            currentRobot.stopLoop();
        }

        robotClasses.add(robot.getClass());

        editable = false;

        currentRobot = robot;

        new Topica.Database.Topic("/robot/selected", robot.getClass().getSimpleName());

        Server.start();
        Topica.start();
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

            if (Topica.getDatabase().hasTopic("/robot/selected")) {
                Topica.getDatabase().getTopic("/robot/selected").update(robot.getClass().getSimpleName());
            }
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

            if (Topica.getDatabase().hasTopic("/robot/selected")) {
                Topica.getDatabase().getTopic("/robot/selected").update("");
            }
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

    private static Timer statusTimer;

    /**
     * Publishes the status of the robot to Topica
     * */
    public static void publishStatus() {
        new Topica.Database.Topic("/robot/status", Topica.BYTE_TYPE, new byte[11]);

        TimerTask status = new TimerTask() {
            @Override
            public void run() {
                byte[] data = new byte[11];

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

                Topica.getDatabase().getTopic("/robot/status").update(data);
            }
        };

        statusTimer = new Timer();
        statusTimer.schedule(status, 0, 100);
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
     * Creates the telemetry topics.
     * */
    public static void createTelemetryTopics() {
        Topica.topic("/robot/telemetry", Topica.BYTE_TYPE).bind(() -> {
            Byte[] data = new Byte[3];

            data[0] = 0x6D;
            data[1] = 0x00;
            data[2] = 0x00;

            return data;
        });
    }

    /**
     * Subscribes a WSClient to the telemetry of the robot.
     * @param client The client to subscribe.
     *               @see WSClient
     * @param subscribe Whether to subscribe or unsubscribe.
     * */
    @Deprecated
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

    @Log
    private byte identification;

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

        Mechanism.init_all(this);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (paused) return;

                loop();

                Mechanism.update_all();
            }
        };

        loopTimer = new Timer();
        loopTimer.schedule(task, loopTime, loopTime);

        timeCreated = System.currentTimeMillis();
        clock = new Clock();
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
     * TODO: Deprecate this method and replace it with a default behavior.
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
     * Cancels the loop timer.
     * Will stop the loop method from being called in the future.
     * */
    public void stopLoop() {
        loopTimer.cancel();
        stop();
        Mechanism.stop_all();
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
