package me.autobot.sim.testing;

import me.autobot.code.Robot;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Int2;
import me.autobot.lib.math.coordinates.Polar;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.robot.sensors.UltrasonicSensor;
import me.autobot.sim.Simulation;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SimCanvasTesting extends JFrame {
    boolean start = false;

    UltrasonicSensor sensor;

    double t = 0;

    public void paint_(Graphics g) {
        if (!start) {
            sensor = new UltrasonicSensor(0x04);


            start = true;
        }

        g.clearRect(0, 0, getWidth(), getHeight());

        ArrayList<Box2d> boxes = new ArrayList<>();

        for (double i = 0; i < Math.PI * 2; i += Math.PI / 10) {
            double x = Math.cos(i) * 100;
            double y = Math.sin(i) * 100;

            boxes.add(new Box2d(
                    new Int2((int) x, (int) y),
                    new Int2(20, 20)
            ));
        }

        for (Box2d object : boxes) {
            g.setColor(Color.RED);
            g.fillRect(
                    (getWidth() / 2) + object.getPosition().x,
                    (getHeight() / 2) + object.getPosition().y,
                    object.getSize().x,
                    object.getSize().y
            );
        }

        Polar polar = new Polar(255, Rotation2d.fromRadians(t));

        Vector2d ray = polar.toVector();

        Box2d intersecting = null;
        int n = 0;

        for (Box2d object : boxes) {
            if (object.lineIntersects(Vector2d.zero(), ray)) {
                g.setColor(Color.GREEN);
                intersecting = object;
                n++;
            } else {
                g.setColor(Color.RED);
            }

            g.fillRect(
                    (getWidth() / 2) + object.getPosition().x,
                    (getHeight() / 2) + object.getPosition().y,
                    object.getSize().x,
                    object.getSize().y
            );
        }

        Polar polar2 = new Polar(intersecting != null ? intersecting.raycastDistance(Vector2d.zero(), ray) : 255, Rotation2d.fromRadians(t));

        ray = polar2.toVector();

        g.setColor(Color.BLUE);
        g.drawLine(
                (getWidth() / 2),
                (getHeight() / 2),
                (getWidth() / 2) + (int) ray.getX(),
                (getHeight() / 2) + (int) ray.getY()
        );

        t += Math.PI / 100;

//        Box2d c = new Box2d(
//                new Int2(-10, -10),
//                new Int2(20, 20)
//        );
//
//        for (int i = 0; i < 300; i++) {
//            for (int j = 0; j < 300; j++) {
//                Vector2d v = new Vector2d(i - 150d, j - 150d);
//
//                double dist = 1000;
//
//                for (Box2d object : boxes) {
//                    double d = object.signedDistance(v);
//
//                    if (d < dist) {
//                        dist = d;
//                    }
//                }
//
//                if (dist <= 0) {
//                    g.setColor(Color.RED);
//                } else {
//                    g.setColor(new Color(0, 255 - (int) dist, 0));
//                }
//
//                g.fillRect(
//                        (getWidth() / 2) + (int)v.getX(),
//                        (getHeight() / 2) + (int)v.getY(),
//                        1,
//                        1
//                );
//            }
//        }
//
//        for (double th = 0; th < 360; th += 0.5) {
//            Rotation2d r = Rotation2d.fromDegrees(th);
//
//            Polar p = new Polar(255, r);
//
//            Vector2d v = p.toVector();
//
//            double d = 255;
//
//            for (Box2d object : boxes) {
//                if (!object.intersectsRay(Vector2d.zero(), v)) {
//                    continue;
//                }
//
//                double dist = object.raycastDistance(new Vector2d(0,0), v);
//
//                if (dist < d) {
//                    d = dist;
//                }
//            }
//
//            Polar p2 = new Polar(d, r);
//
//            Vector2d v2 = p2.toVector();
//
//            g.setColor(Color.BLUE);
//            g.drawLine(
//                    (getWidth() / 2),
//                    (getHeight() / 2),
//                    (getWidth() / 2) + (int)v2.getX(),
//                    (getHeight() / 2) + (int)v2.getY()
//            );
//        }
//
//        g.setColor(Color.BLACK);
//
//        for (Box2d object : boxes) {
//            g.drawRect(
//                    (getWidth() / 2) + object.getPosition().x,
//                    (getHeight() / 2) + object.getPosition().y,
//                    object.getSize().x,
//                    object.getSize().y
//            );
//        }
//
//        g.drawRect((getWidth() / 2) + c.getPosition().x, (getHeight() / 2) + c.getPosition().y, c.getSize().x, c.getSize().y);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        repaint();
    }
}
