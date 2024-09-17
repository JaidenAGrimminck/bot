package me.autobot.lib.robot;

import me.autobot.lib.math.coordinates.Vector3d;
import me.autobot.lib.math.rotation.Rotation3d;
import me.autobot.server.WSClient;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Sensor extends Device {

    private static ArrayList<Sensor> sensors = new ArrayList<>();

    public static Sensor getSensor(int address) {
        for (Sensor sensor : sensors) {
            if (sensor.getAddress() == address) {
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

    private boolean simulating = false;

    private Timer updateTimer;

    private long updateInterval = 1000 / 20;

    public Sensor(int address, int sensorChannels) {
        super();

        this.sensorChannels = sensorChannels;

        sensorValues = new double[sensorChannels];

        this.address = address;

        sensors.add(this);

        startUpdateTimer();
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
                            client.sendSensorData((byte) address, getValues());
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

    public void enableSimulation() {
        simulating = true;
    }

    public boolean inSimulation() {
        return simulating;
    }

    public int getAddress() {
        return address;
    }

    public void simulateValues(double[] values) {
        if (!simulating) {
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

    protected void setSensorValues(double... values) {
        sensorValues = values;
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
}
