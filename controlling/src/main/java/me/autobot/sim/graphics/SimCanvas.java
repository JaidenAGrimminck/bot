package me.autobot.sim.graphics;

import me.autobot.code.Robot;
import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Int2;
import me.autobot.lib.math.coordinates.Polar;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.robot.UltrasonicSensor;
import me.autobot.sim.MapLoader;
import me.autobot.sim.Simulation;
import me.autobot.sim.graphics.elements.CanvasButton;
import me.autobot.sim.graphics.elements.CanvasElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class SimCanvas extends JPanel {
    private JFrame frame;

    public SimCanvas(JFrame frame) {
        this.frame = frame;
        new Thread(this::run).start();
    }

    double scale = 0.001;

    boolean up = false;
    boolean down = false;
    boolean left = false;
    boolean right = false;

    int speed = 10;

    public void run() {
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        setFont(new Font("Arial", Font.PLAIN, 24));

        elements.add(
                new CanvasButton(0, 0, 100, 50,
                        "Menu", () -> System.out.println("Button 1 pressed")
                ).setStyle(Color.DARK_GRAY, Color.WHITE, new Color(44, 44, 44))
                        .adjustText(24, 20, 30)
        );

        ActionListener mousepress = e -> {
            elements.forEach(element -> {
                if (element instanceof CanvasButton) {
                    CanvasButton button = (CanvasButton) element;
                    if (button.isInside(mousePosition)) {
                        button.run();
                    }
                }
            });
        };

        MouseAdapter mouseAdapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mousepress.actionPerformed(null);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition = new Int2(e.getPoint().x, e.getPoint().y);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        KeyListener keyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 'w') {
                    up = true;
                } else if (e.getKeyChar() == 's') {
                    down = true;
                } else if (e.getKeyChar() == 'a') {
                    left = true;
                } else if (e.getKeyChar() == 'd') {
                    right = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == 'w') {
                    up = false;
                } else if (e.getKeyChar() == 's') {
                    down = false;
                } else if (e.getKeyChar() == 'a') {
                    left = false;
                } else if (e.getKeyChar() == 'd') {
                    right = false;
                }
            }
        };

        frame.addKeyListener(keyListener);

        try {
            Simulation.getInstance().environment.obstacles = MapLoader.mapToObjects(MapLoader.loadMap("/Users/jgrimminck/Documents/coding projects/bot/controlling/src/maps/map.txt"), 20);

            Simulation.getInstance().environment.obstacles.add(
                    new Box2d(
                            new Int2(-20, -20),
                            new Int2(20, 2020)
                    )
            );

            Simulation.getInstance().environment.obstacles.add(
                    new Box2d(
                            new Int2(0, -20),
                            new Int2(2000, 20)
                    )
            );

            Simulation.getInstance().environment.obstacles.add(
                    new Box2d(
                            new Int2(-20, 2000),
                            new Int2(2040, 20)
                    )
            );

            Simulation.getInstance().environment.obstacles.add(
                    new Box2d(
                            new Int2(2000, -10),
                            new Int2(20, 2000)
                    )
            );


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        return new Dimension(800, 600);
    }

    private ArrayList<CanvasElement> elements = new ArrayList<>();

    private Int2 mousePosition = Int2.zero();

    public static String debugStr = "";

    boolean start = false;

    UltrasonicSensor sensor;

    double t = 0;

    public void paint_(Graphics g) {
        if (!start) {
            sensor = new UltrasonicSensor(0x04);


            start = true;
        }

        g.clearRect(0, 0, getWidth(), getHeight());

        Robot robot = Simulation.getInstance().getRobot();

        Box2d left = new Box2d(
                new Int2(-120, -10),
                new Int2(20, 20)
        );

        Box2d right = new Box2d(
                new Int2(100, -10),
                new Int2(20, 20)
        );

        Box2d front = new Box2d(
                new Int2(-10, -120),
                new Int2(20, 20)
        );

        Box2d back = new Box2d(
                new Int2(-10, 100),
                new Int2(20, 20)
        );

        ArrayList<Box2d> boxes = new ArrayList<>();
//        boxes.add(left);
//        boxes.add(right);
//        boxes.add(front);
//        boxes.add(back);

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

    public void paint(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());

        final Int2 fmousePosition = mousePosition;

        //get all objects nearby the robot
        Robot robot = Simulation.getInstance().getRobot();

        Rotation2d robotRotation = robot.getRotation();

        Graphics2D g2d = (Graphics2D) g.create();

        g2d.translate((getWidth() / 2), (getHeight() / 2));

        for (Box2d object : Simulation.getInstance().environment.obstacles) {
            if (object.signedDistance(robot.getPosition()) < 1000d) {
                g2d.setColor(Color.RED);
            } else {
                continue;
            }

            if (object.inZone) {
                g2d.setColor(Color.BLUE);
            }

            if (object.inRay) {
                g2d.setColor(Color.GREEN);
            }

            g2d.fillRect(object.getPosition().x - (int) robot.getPosition().getX(), object.getPosition().y - (int) robot.getPosition().getY(), object.getSize().x, object.getSize().y);
        }

        g2d.dispose();

        g2d = (Graphics2D) g.create();

        g2d.translate((getWidth() / 2), (getHeight() / 2));
        g2d.rotate(robotRotation.getTheta());

        // 1px = 1cm
        g2d.setColor(Color.BLACK);
        g2d.fillRect(- 20,  -30, 40, 60);

        g2d.setColor(Color.WHITE);
        g2d.fillOval(-15, 20, 5, 5);
        g2d.fillOval(10, 20, 5, 5);

        ArrayList<Sensor> sensors = robot.getSensors();

        for (Sensor sensor : sensors) {
            if (sensor instanceof UltrasonicSensor) {
                UltrasonicSensor us = (UltrasonicSensor) sensor;
                g2d.setColor(Color.BLUE);
                g2d.fillOval((int) (us.getRelativePosition().getX() - 5), (int) (us.getRelativePosition().getY() - 5), 10, 10);

                double distance = us.getDistance().getValue(Unit.Type.CENTIMETER);

                Vector2d ray = Vector2d.fromPolar(distance, Rotation2d.fromRadians(us.getRelativeRotation().getThetaRadians()));

                if (sensor.getAddress() == 0x02) {
                    g2d.setColor(Color.RED);
                }

                g2d.drawLine((int) (us.getRelativePosition().getX()), (int) (us.getRelativePosition().getY()), (int) (us.getRelativePosition().getX() + ray.getX()), (int) (us.getRelativePosition().getY() + ray.getY()));
                g2d.fillOval((int) (us.getRelativePosition().getX() + ray.getX() - 5), (int) (us.getRelativePosition().getY() + ray.getY() - 5), 10, 10);
            }
        }

        g2d.dispose();

        g.setColor(Color.BLACK);
        g.drawString(debugStr, 100, 10);

        elements.forEach(e -> e.draw(g, fmousePosition));

        if (down) {
            robot.move(0, speed);
        }
        if (up) {
            robot.move(0, -speed);
        }
        if (left) {
            robot.move(-speed, 0);
        }
        if (right) {
            robot.move(speed, 0);
        }

        //wait 20 ms
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        repaint();
    }
}
