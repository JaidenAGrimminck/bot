package me.autobot.lib.robot;

import me.autobot.lib.math.coordinates.Vector3d;
import me.autobot.lib.math.rotation.Rotation3d;
import me.autobot.lib.hardware.i2c.SensorHubI2CConnection;
import me.autobot.lib.server.WSClient;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A device that's used to sense the environment around the robot.
 * */
public class Sensor extends Device {

    private static ArrayList<Sensor> sensors = new ArrayList<>();

    /**
     * Gets the sensor object with the specific identifier and robot address.
     * @param identifier The identifier of the sensor.
     * @param robotAddr The address of the robot.
     * @return The sensor object with the specific identifier and robot address.
     * */
    public static Sensor getSensor(int identifier, int robotAddr) {
        for (Sensor sensor : sensors) {
            if (sensor.identifier == identifier && sensor.getParentIdentification() == robotAddr) {
                return sensor;
            }
        }

        return null;
    }

    private double[] sensorValues;
    private final int sensorChannels;


    private Vector3d relativePosition;

    private Rotation3d relativeRotation;

    // any websocket clients that are subscribed to this sensor
    private ArrayList<WSClient> subscribers;

    // for I2C connection
    private int address;
    private int bus;

    // identifier for in general

    private final int identifier;

    private Timer updateTimer;

    private long updateInterval = 1000 / 20;

    /**
     * Creates a new sensor with the given address and number of sensor channels.
     * @param identifier The identifier of the sensor. Could be any number, should be unique to all other sensors on the robot.
     * @param sensorChannels The number of output channels the sensor has.
     * */
    public Sensor(int identifier, int sensorChannels) {
        super();

        this.sensorChannels = sensorChannels;
        this.bus = SensorHubI2CConnection.default_bus;

        sensorValues = new double[sensorChannels];

        sensors.add(this);

        this.identifier = identifier;

        startUpdateTimer();
    }

    /**
     * Connects the sensor to the I2C bus.
     * @param pin The pin to connect the sensor to.
     * @param address The address of the sensor.
     * @param bus The bus to connect the sensor to.
     * **/
    public void connectToI2C(int address, int bus, int pin) {
        this.address = address;
        this.bus = bus;

        if (this.getParent() == null) {
            throw new IllegalStateException("Cannot connect sensor to I2C bus without a parent.");
        }

        if (this.inSimulation()) {
            //ignore this if we are in simulation
            return;
        }

        SensorHubI2CConnection connection = SensorHubI2CConnection.getOrCreateConnection(
                SensorHubI2CConnection.generateId(bus, this.getAddress()), bus, this.getAddress()
        );

        connection.subscribeSensor(
        this, pin
        );
    }

    /**
     * Connects the sensor via Serial
     * @param port The port to connect to.
     */
    public void connectToSerial(String port) {
        // do nothing
    }

    /**
     * Starts the update timer for any sensor subscribers.
     * */
    private void startUpdateTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (subscribers == null) return;

                ArrayList<WSClient> toRemove = new ArrayList<>();

                for (int i = 0; i < subscribers.size(); i++) {
                    WSClient client = subscribers.get(i);
                    try {
                        if (client.isOpen())
                            client.sendSensorData(getParentIdentification(), (byte) getIdentifier(), getValues());
                        else
                            toRemove.add(client);
                    } catch (Exception e) {
                        // ignore
                    }
                }

                toRemove.forEach(subscribers::remove);
            }
        };

        updateTimer = new Timer();

        updateTimer.schedule(task, 0, updateInterval);
    }

    /**
     * Changes the update interval of the sensor for the subscribers.
     * @param interval The new update interval of the sensor subscribers.
     * */
    protected void changeUpdateInterval(long interval) {
        updateInterval = interval;
    }

    /**
     * Returns the raw sensor values.
     * @return The raw sensor values.
     * */
    public double[] getSensorValues() {
        return sensorValues;
    }

    /**
     * Returns the processed sensor values.
     * @return The processed sensor values.
     * */
    public double[] getValues() {
        return sensorValues;
    }

    /**
     * Returns the I2C address of the sensor hub.
     * @return The I2C address of the sensor hub.
     * **/
    public int getAddress() {
        return address;
    }

    /**
     * Returns the I2C bus of the sensor hub.
     * @return The I2C bus of the sensor hub.
     * */
    public int getBus() {
        return bus;
    }

    /**
     * Returns the identifier of the sensor.
     * @return The identifier of the sensor.
     * */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Plugs in fake sensor values for simulation.
     * @param values The values to simulate.
     * */
    public void simulateValues(double[] values) {
        if (!this.inSimulation()) {
            throw new IllegalStateException("Cannot simulate values when simulation is not enabled.");
        }

        if (values.length != sensorChannels) {
            throw new IllegalArgumentException("Values array must have the same length as the number of sensor channels");
        }

        sensorValues = values;
    }

    /**
     * Gets the relative position to the center of the robot.
     * @return The relative position to the center of the robot.
     * */
    public Vector3d getRelativePosition() {
        if (relativePosition == null) {
            throw new IllegalStateException("Relative position has not been set.");
        }

        return relativePosition;
    }

    /**
     * Gets the relative rotation to the center of the robot.
     * @return The relative rotation to the center of the robot.
     * */
    public Rotation3d getRelativeRotation() {
        if (relativeRotation == null) {
            throw new IllegalStateException("Relative rotation has not been set.");
        }

        return relativeRotation;
    }

    /**
     * Attaches the relative position of the sensor to the center of the robot.
     * @param relativePosition The relative position of the sensor to the center of the robot.
     * */
    public void attachRelativePosition(Vector3d relativePosition) {
        attachRelativePosition(relativePosition, new Rotation3d(0, Math.PI/2));
    }

    /**
     * Attaches the relative position and rotation of the sensor to the center of the robot.
     * @param relativePosition The relative position of the sensor to the center of the robot.
     * @param relativeRotation The relative rotation of the sensor to the center of the robot.
     * */
    public void attachRelativePosition(Vector3d relativePosition, Rotation3d relativeRotation) {
        this.relativePosition = relativePosition;
        this.relativeRotation = relativeRotation;
    }

    /**
     * Set the sensor values.
     * @param values The values to set the sensor to.
     * */
    protected void setSensorValues(double... values) {
        sensorValues = values;
    }

    /**
     * Set a specific sensor value.
     * @param index The index of the sensor value to set.
     * @param value The value to set the sensor to.
     * */
    public void setSensorValue(int index, double value) {
        sensorValues[index] = value;
    }

    /**
     * Subscribes a client to the sensor to recieve updates.
     * @param client The client to subscribe.
     * */
    public void subscribe(WSClient client) {
        if (subscribers == null) {
            subscribers = new ArrayList<>();
        }

        subscribers.add(client);
    }

    /**
     * Unsubscribes a client from the sensor.
     * @param client The client to unsubscribe.
     * */
    public void unsubscribe(WSClient client) {
        if (subscribers == null) return;

        subscribers.remove(client);
    }

    /**
     * Sets the Bus and Address of the sensor (for I2C communication).
     * @param bus The bus to set the sensor to.
     * @param address The address to set the sensor to.
     * */
    protected void setBusAndAddress(int bus, int address) {
        this.bus = bus;
        this.address = address;
    }

    /**
     * Returns the address/identifier of the parent robot.
     * @return The address/identifier of the parent robot.
     * */
    public byte getParentIdentification() {
        return this.getParent().getIdentification();
    }
}
