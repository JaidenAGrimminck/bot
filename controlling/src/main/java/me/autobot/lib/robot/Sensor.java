package me.autobot.lib.robot;

import me.autobot.lib.math.coordinates.Vector3d;
import me.autobot.lib.math.rotation.Rotation3d;
import me.autobot.lib.serial.SensorHubI2CConnection;
import me.autobot.server.WSClient;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Sensor extends Device {

    private static ArrayList<Sensor> sensors = new ArrayList<>();

    public static Sensor getSensor(int robotAddr, int address) {
        for (Sensor sensor : sensors) {
            if (sensor.getAddress() == address && sensor.getParentAddress() == robotAddr) {
                return sensor;
            }
        }

        return null;
    }

    private double[] sensorValues;
    private final int sensorChannels;

    private Vector3d relativePosition;

    private Rotation3d relativeRotation;

    private ArrayList<WSClient> subscribers;

    private final int address;
    private final int bus;

    private Timer updateTimer;

    private long updateInterval = 1000 / 20;

    public Sensor(int address, int sensorChannels) {
        super();

        this.sensorChannels = sensorChannels;
        this.bus = SensorHubI2CConnection.default_bus;

        sensorValues = new double[sensorChannels];

        this.address = address;

        sensors.add(this);

        startUpdateTimer();
    }

    public Sensor(int address, int bus, int sensorChannels) {
        super();

        this.sensorChannels = sensorChannels;
        this.bus = bus;

        sensorValues = new double[sensorChannels];

        this.address = address;

        sensors.add(this);

        startUpdateTimer();
    }

    /**
     * Connects the sensor to the I2C bus.
     * **/
    public void connectToI2C(int pin) {
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
                            client.sendSensorData(getParentAddress(), (byte) address, getValues());
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

    protected void changeUpdateInterval(long interval) {
        updateInterval = interval;
    }

    //generally raw data
    public double[] getSensorValues() {
        return sensorValues;
    }

    //this is the processed data, if it is.
    public double[] getValues() {
        return sensorValues;
    }

    public int getAddress() {
        return address;
    }

    public void simulateValues(double[] values) {
        if (!this.inSimulation()) {
            throw new IllegalStateException("Cannot simulate values when simulation is not enabled.");
        }

        if (values.length != sensorChannels) {
            throw new IllegalArgumentException("Values array must have the same length as the number of sensor channels");
        }

        sensorValues = values;
    }

    public Vector3d getRelativePosition() {
        if (relativePosition == null) {
            throw new IllegalStateException("Relative position has not been set.");
        }

        return relativePosition;
    }

    public Rotation3d getRelativeRotation() {
        if (relativeRotation == null) {
            throw new IllegalStateException("Relative rotation has not been set.");
        }

        return relativeRotation;
    }

    public void attachRelativePosition(Vector3d relativePosition) {
        attachRelativePosition(relativePosition, new Rotation3d(0, Math.PI/2));
    }

    public void attachRelativePosition(Vector3d relativePosition, Rotation3d relativeRotation) {
        this.relativePosition = relativePosition;
        this.relativeRotation = relativeRotation;
    }

    public void setSensorValues(double... values) {
        sensorValues = values;
    }

    public void setSensorValue(int index, double value) {
        sensorValues[index] = value;
    }

    public void subscribe(WSClient client) {
        if (subscribers == null) {
            subscribers = new ArrayList<>();
        }

        subscribers.add(client);
    }

    public void unsubscribe(WSClient client) {
        if (subscribers == null) return;

        subscribers.remove(client);
    }

    public byte getParentAddress() {
        return this.getParent().getIdentification();
    }
}
