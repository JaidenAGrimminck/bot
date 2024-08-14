package me.autobot.lib.robot;

import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.sim.Simulation;
import me.autobot.sim.graphics.SimCanvas;

import java.util.ArrayList;

public class UltrasonicSensor extends Sensor {

    private final static Unit maxDistance = new Unit(255, Unit.Type.CENTIMETER);

    public UltrasonicSensor(int address) {
        super(address, 1);
        setSensorValues(0);
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
            Vector2d absolutePos = pos.add(relativePos);

            Rotation2d relativeRotation = Rotation2d.fromRadians(getRelativeRotation().getThetaRadians());

            //need to create a ray from absolutepos in direction of relative rotation, so create vector2d max distance in direction of relative rotation
            Vector2d ray = Vector2d.fromPolar(maxDistance.getValue(Unit.Type.CENTIMETER), relativeRotation);

            //then, we need to check if there's an object in the way
            //we can do this by checking if the ray intersects with any objects
            ArrayList<Box2d> objects = (ArrayList<Box2d>) Simulation.getInstance().environment.obstacles.clone();

            //filter out objects that are too far away
            objects.removeIf(object -> object.signedDistance(absolutePos) > maxDistance.getValue(Unit.Type.CENTIMETER));

            Box2d closestObject = null;
            double closestDistance = maxDistance.getValue(Unit.Type.CENTIMETER);

            for (Box2d object : objects) {
                if (getAddress() == 0x04) object.inRay = false;

                if (object.lineIntersects(absolutePos, ray) && object.signedDistance(absolutePos) < closestDistance) {
                    closestDistance = object.signedDistance(absolutePos);
                    closestObject = object;
                }
            }

            if (closestObject != null) {
                if (getAddress() == 0x04) {
                    closestObject.inRay = true;
                    SimCanvas.debugStr = closestObject.getPosition().toString() + ", " + getRobot().getPosition().toString();
                }

                //if it does, return the distance to the object
                return Unit.Type.CENTIMETER.c(closestObject.signedDistance(absolutePos));
            }

            //if no objects are in the way, return max distance
            return maxDistance;
        } else return Unit.Type.CENTIMETER.c(getSensorValues()[0]);
    }


}
