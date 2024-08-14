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

    public void paint(Graphics g) {
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

        Polar polar2 = new Polar(intersecting != null ? intersecting.signedDistance(Vector2d.zero()) : 255, Rotation2d.fromRadians(t));

        ray = polar2.toVector();

        g.setColor(Color.BLUE);
        g.drawLine(
                (getWidth() / 2),
                (getHeight() / 2),
                (getWidth() / 2) + (int) ray.getX(),
                (getHeight() / 2) + (int) ray.getY()
        );

        t += Math.PI / 100;

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        repaint();
    }

    public void paint_(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());

        final Int2 fmousePosition = mousePosition;

        //get all objects nearby the robot
        Robot robot = Simulation.getInstance().getRobot();

        for (Box2d object : Simulation.getInstance().environment.obstacles) {
            if (object.signedDistance(robot.getPosition()) < 1000d) {
                g.setColor(Color.RED);
            } else {
                continue;
            }

            if (object.inRay) {
                g.setColor(Color.GREEN);
            }

            g.fillRect(object.getPosition().x - (int) robot.getPosition().getX() + (getWidth() / 2), object.getPosition().y - (int) robot.getPosition().getY() + (getHeight() / 2), object.getSize().x, object.getSize().y);
        }


        // 1px = 1cm
        g.setColor(Color.BLACK);
        g.fillRect((getWidth() / 2) - 20, (getHeight() / 2) - 30, 40, 60);

        g.setColor(Color.WHITE);
        g.fillOval((getWidth() / 2) - 15, (getHeight() / 2) + 20, 5, 5);
        g.fillOval((getWidth() / 2) + 10, (getHeight() / 2) + 20, 5, 5);

        ArrayList<Sensor> sensors = robot.getSensors();

        for (Sensor sensor : sensors) {
            if (sensor instanceof UltrasonicSensor) {
                UltrasonicSensor us = (UltrasonicSensor) sensor;
                g.setColor(Color.BLUE);
                g.fillOval((int) ((getWidth() / 2) + us.getRelativePosition().getX() - 5), (int) ((getHeight() / 2) + us.getRelativePosition().getY() - 5), 10, 10);

                double distance = us.getDistance().getValue(Unit.Type.CENTIMETER);

                Vector2d ray = Vector2d.fromPolar(distance, Rotation2d.fromRadians(us.getRelativeRotation().getThetaRadians()));

                if (sensor.getAddress() == 0x04) {
                    g.setColor(Color.RED);
                }

                g.drawLine((int) (getWidth() / 2 + us.getRelativePosition().getX()), (int) (getHeight() / 2 + us.getRelativePosition().getY()), (int) (getWidth() / 2 + us.getRelativePosition().getX() + ray.getX()), (int) (getHeight() / 2 + us.getRelativePosition().getY() + ray.getY()));
                g.fillOval((int) (getWidth() / 2 + us.getRelativePosition().getX() + ray.getX() - 5), (int) (getHeight() / 2 + us.getRelativePosition().getY() + ray.getY() - 5), 10, 10);
            }
        }

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
