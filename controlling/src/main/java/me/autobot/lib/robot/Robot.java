package me.autobot.lib.robot;

import me.autobot.lib.logging.Log;
import me.autobot.lib.logging.Logger;
import me.autobot.lib.math.Clock;
import me.autobot.lib.math.Geometry;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.odometry.SimpleOdometry2d;
import me.autobot.sim.Simulation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The general robot class.
 * This can be extended to create a robot with sensors and motors.
 * **/
public class Robot implements Logger {

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
     * Gets the first robot / only robot if only one robot is created.
     * @return A robot instance.
     */
    public static Robot getInstance() {
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
