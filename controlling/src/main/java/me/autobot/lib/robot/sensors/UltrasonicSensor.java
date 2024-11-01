package me.autobot.lib.robot.sensors;

import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.serial.i2c.SensorHubI2CConnection;
import me.autobot.lib.serial.serial.SensorHubSerialConnection;
import me.autobot.sim.Simulation;

import java.util.ArrayList;

/**
 * An ultrasonic sensor is a sensor that can detect the distance to the nearest object in front of it.
 * It can be used to detect objects in front of the robot, and can be used to avoid obstacles.
 * */
public class UltrasonicSensor extends Sensor {
    /**
     * The maximum recording distance of the ultrasonic sensor
     * in centimeters.
     * @see Unit
     * */
    public final static Unit maxDistance = new Unit(255, Unit.Type.CENTIMETER);

    private final static Unit maxNoise = new Unit(1, Unit.Type.CENTIMETER);

    /**
     * Creates a new ultrasonic sensor object.
     * @param identifier The identifier of the sensor.
     * @param address The address of the sensor hub (i2c).
     * */
    public UltrasonicSensor(int identifier, int address) {
        super(identifier, address, 1);
        setSensorValues(0);
    }

    /**
     * Connects the ultrasonic sensor to the I2C bus.
     * @param pin The echo pin on the sensor hub.
     * */
    @Override
    public void connectToI2C(int pin) {
        if (getParent() == null) {
            throw new IllegalStateException("Cannot connect sensor to I2C bus without a parent.");
        }

        if (inSimulation()) {
            //ignore this if we are in simulation
            return;
        }

        //connect to the I2C bus
        SensorHubI2CConnection.getOrCreateConnection(
                SensorHubI2CConnection.generateId(getBus(), getAddress()), getBus(), getAddress()
        ).subscribeSensor(this, pin);
    }

    /**
     * Connects the ultrasonic sensor to the serial port.
     * @param port The port to connect to.
     */
    public void connectToSerial(int pin, String port) {
        if (getParent() == null) {
            throw new IllegalStateException("Cannot connect sensor to serial without a parent.");
        }

        if (inSimulation()) {
            //ignore this if we are in simulation
            return;
        }

        SensorHubSerialConnection.getOrCreateConnection(
                9600, port
        ).adj().subscribeSensor(this, pin);
    }

    /**
     * Gets the estimated absolute position of the sensor (relative to a starting point, not the robot).
     * @return The estimated absolute position of the sensor.
     * */
    public Vector2d getEstimatedAbsPosition() {
        Vector2d pos = getParent().getPosition();

        //get flat position
        Vector2d relativePos = this.getRelativePosition().toXY();

        Rotation2d robotRotation = this.getParent().getRotation();

        //first, rotate the relative position by the robot's rotation
        Vector2d rotatedPos = relativePos.rotate(robotRotation);

        //then, add the robot's position to get the absolute position
        return pos.add(rotatedPos);
    }

    /**
     * Gets the values of the sensor.
     * @return The values of the sensor.
     * */
    @Override
    public double[] getValues() {
        return new double[] {
                getDistance().getValue(Unit.Type.CENTIMETER)
        };
    }

    /**
     * Gets the distance to the nearest object in front of the sensor.
     * @return The distance to the nearest object in front of the sensor.
     * */
    public Unit getDistance() {
        if (inSimulation()) {
            if (!Simulation.running()) return Unit.zero();

            //get position
            Vector2d pos = getParent().getPosition();

            Vector2d relativePos = this.getRelativePosition().toXY();

            Rotation2d robotRotation = this.getParent().getRotation();

            //first, rotate the relative position by the robot's rotation
            Vector2d rotatedPos = relativePos.rotate(robotRotation);

            //then, add the robot's position to get the absolute position
            Vector2d absolutePos = pos.add(rotatedPos);

            Rotation2d relativeRotation = Rotation2d.fromRadians(getRelativeRotation().getThetaRadians() + robotRotation.getTheta());

            //need to create a ray from absolutepos in direction of relative rotation, so create vector2d max distance in direction of relative rotation
            Vector2d ray = Vector2d.fromPolar(maxDistance.getValue(Unit.Type.CENTIMETER), relativeRotation);

            ray = ray.add(absolutePos);

            //then, we need to check if there's an object in the way
            //we can do this by checking if the ray intersects with any objects
            ArrayList<Box2d> objects = (ArrayList<Box2d>) Simulation.getInstance().environment.obstacles.clone();

            //filter out objects that are too far away
            objects.removeIf(object -> object.signedDistance(absolutePos) > maxDistance.getValue(Unit.Type.CENTIMETER) * 3);

            Box2d closestObject = null;
            double closestDistance = maxDistance.getValue(Unit.Type.CENTIMETER);

            for (Box2d object : objects) {
//                if (getAddress() == 0x02) object.inRay = false;
//
//                object.inZone = true;
//
//                if (object.intersectsRay(absolutePos, ray) && getAddress() == 0x02) {
//                    object.inRay = true;
//                }

                object.flags.put(getParentAddress() + getAddress() + "hit", false);

                if (object.lineIntersects(absolutePos, ray) && object.raycastDistance(absolutePos, ray) < closestDistance) {
                    closestDistance = object.raycastDistance(absolutePos, ray);
                    closestObject = object;
                }
            }

            if (closestObject != null) {
//                if (getAddress() == 0x02) {
//                    closestObject.inRay = true;
//                    SimCanvas.debugStr = closestObject.getPosition().toString() + ", " + getRobot().getPosition().toString();
//                }
                closestObject.flags.put(getParentAddress() + getAddress() + "hit", true);

                //if it does, return the distance to the object
                return Unit.Type.CENTIMETER.c(closestObject.raycastDistance(absolutePos, ray) + (Math.random() * maxNoise.getValue(Unit.Type.CENTIMETER)));
            }

            //if no objects are in the way, return max distance
            return maxDistance;
        } else return Unit.Type.CENTIMETER.c(getSensorValues()[0]);
    }
}
