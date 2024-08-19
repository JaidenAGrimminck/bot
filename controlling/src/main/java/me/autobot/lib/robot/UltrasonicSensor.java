package me.autobot.lib.robot;

import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.sim.Simulation;
import me.autobot.sim.graphics.SimCanvas;

import java.awt.*;
import java.util.ArrayList;

public class UltrasonicSensor extends Sensor {

    private final static Unit maxDistance = new Unit(255, Unit.Type.CENTIMETER);

    private final static Unit maxNoise = new Unit(1, Unit.Type.CENTIMETER);

    public UltrasonicSensor(int address) {
        super(address, 1);
        setSensorValues(0);
    }

    public Vector2d getEstimatedAbsPosition() {
        Vector2d pos = getRobot().getPosition();

        Vector2d relativePos = this.getRelativePosition().toXY();

        Rotation2d robotRotation = this.getRobot().getRotation();

        //first, rotate the relative position by the robot's rotation
        Vector2d rotatedPos = relativePos.rotate(robotRotation);

        //then, add the robot's position to get the absolute position
        return pos.add(rotatedPos);
    }

    @Override
    public double[] getValues() {
        return new double[] {getDistance().getValue(Unit.Type.CENTIMETER)};
    }

    public Unit getDistance() {
        if (inSimulation()) {
            if (!Simulation.running()) return Unit.zero();

            //get position
            Vector2d pos = getRobot().getPosition();

            Vector2d relativePos = this.getRelativePosition().toXY();

            Rotation2d robotRotation = this.getRobot().getRotation();

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

                object.flags.put(getAddress() + "hit", false);

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
                closestObject.flags.put(getAddress() + "hit", true);

                //if it does, return the distance to the object
                return Unit.Type.CENTIMETER.c(closestObject.raycastDistance(absolutePos, ray) + (Math.random() * maxNoise.getValue(Unit.Type.CENTIMETER)));
            }

            //if no objects are in the way, return max distance
            return maxDistance;
        } else return Unit.Type.CENTIMETER.c(getSensorValues()[0]);
    }


}
