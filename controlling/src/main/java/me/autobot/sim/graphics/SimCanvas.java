package me.autobot.sim.graphics;

import me.autobot.code.Robot;
import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Int2;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.robot.sensors.UltrasonicSensor;
import me.autobot.lib.tools.RunnableWithArgs;
import me.autobot.server.WSClient;
import me.autobot.sim.MapLoader;
import me.autobot.sim.Simulation;
import me.autobot.sim.graphics.elements.CanvasButton;
import me.autobot.sim.graphics.elements.CanvasElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

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

    boolean rotR = false;
    boolean rotL = false;

    boolean mapEnabled = true;
    boolean knownPointsEnabled = true;

    int speed = 10;
    double turnSpeed = Math.PI / 100;


    // for the robot ai controls
    double aiSpeed = 0;
    final double aiSpeedIncrement = 0.1;
    Rotation2d aiDirection = new Rotation2d(0);
    final Rotation2d aiDirectionIncrement = new Rotation2d((Math.PI / 100) / 50);

    public void run() {
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        setFont(new Font("Arial", Font.PLAIN, 24));

        elements.add(
            new CanvasButton(0, 0, 60, 20,
                "tog map", () -> {
                    mapEnabled = !mapEnabled;
                }
            ).setStyle(Color.DARK_GRAY, Color.WHITE, new Color(44, 44, 44))
                    .adjustText(14, 5, 13)
        );

        elements.add(
            new CanvasButton(
                70, 0, 60, 20,
                "tog kpts", () -> {
                    knownPointsEnabled = !knownPointsEnabled;
                }
            ).setStyle(Color.DARK_GRAY, Color.WHITE, new Color(44, 44, 44))
                    .adjustText(14, 5, 13)
        );

        WSClient.registerCallable(0xB0, new RunnableWithArgs() {
            @Override
            public void run(Object... args) {
                int data = ((int[]) args[0])[0];

                switch (data) {
                    case 0xA1:
                        aiSpeed += aiSpeedIncrement;
                        break;
                    case 0xA2:
                        aiSpeed -= aiSpeedIncrement;
                        break;
                    case 0xA3:
                        aiDirection = aiDirection.rotateBy(aiDirectionIncrement);
                        break;
                    case 0xA4:
                        aiDirection = aiDirection.rotateBy(aiDirectionIncrement.inverse());
                    case 0xA0:
                        break;
                }

                if (Math.abs(aiDirection.getRadians()) > Math.PI / 100) {
                    aiDirection = Rotation2d.fromRadians(Math.PI / 100 * Math.signum(aiDirection.getRadians()));
                }
            }
        });

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
                } else if (e.getKeyChar() == 'q') {
                    rotR = true;
                } else if (e.getKeyChar() == 'e') {
                    rotL = true;
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
                } else if (e.getKeyChar() == 'q') {
                    rotR = false;
                } else if (e.getKeyChar() == 'e') {
                    rotL = false;
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

    public void paint(Graphics g) {
        if (Robot.instance == null) return;

        g.clearRect(0, 0, getWidth(), getHeight());

        final Int2 fmousePosition = mousePosition;

        //get all objects nearby the robot
        Robot robot = Simulation.getInstance().getRobot();
        ArrayList<Sensor> sensors = robot.getSensors();

        Rotation2d robotRotation = robot.getRotation();

        Graphics2D g2d = (Graphics2D) g.create();

        g2d.translate((getWidth() / 2), (getHeight() / 2));

        for (Box2d object : Simulation.getInstance().environment.obstacles) {
            if (object.signedDistance(robot.getPosition()) < 1000d) {
                g2d.setColor(Color.RED);
            } else {
                continue;
            }

            for (int i = 0x00; i <= sensors.size(); i++)
                if (object.flags.getOrDefault(i + "hit", false))
                    g2d.setColor(Color.GREEN);

            if (mapEnabled) g2d.fillRect(object.getPosition().x - (int) robot.getPosition().getX(), object.getPosition().y - (int) robot.getPosition().getY(), object.getSize().x, object.getSize().y);
        }

        g2d.dispose();

        g2d = (Graphics2D) g.create();

        g2d.translate((getWidth() / 2), (getHeight() / 2));
        g2d.rotate(robotRotation.getTheta());

        // 1px = 1cm
        g2d.setColor(Color.BLACK);
        if (Robot.instance.inCollision()) {
            g2d.setColor(Color.RED);
        }
        g2d.fillRect(
                (int) (-Robot.instance.getRobotSize().getX() / 2),  (int) (-Robot.instance.getRobotSize().getY() / 2),
                (int) (Robot.instance.getRobotSize().getX()), (int) (Robot.instance.getRobotSize().getY())
        );

        g2d.setColor(Color.WHITE);
        g2d.fillOval(-15, 20, 5, 5);
        g2d.fillOval(10, 20, 5, 5);

        for (Sensor sensor : sensors) {
            if (sensor instanceof UltrasonicSensor) {
                UltrasonicSensor us = (UltrasonicSensor) sensor;
                g2d.setColor(Color.BLUE);
                if (us.getAddress() == 0x01) {
                    g2d.setColor(Color.RED);
                }
                g2d.fillOval((int) (us.getRelativePosition().getX() - 5), (int) (us.getRelativePosition().getY() - 5), 10, 10);

                double distance = us.getDistance().getValue(Unit.Type.CENTIMETER);

                Vector2d ray = Vector2d.fromPolar(distance, Rotation2d.fromRadians(us.getRelativeRotation().getThetaRadians()));

                g2d.drawLine((int) (us.getRelativePosition().getX()), (int) (us.getRelativePosition().getY()), (int) (us.getRelativePosition().getX() + ray.getX()), (int) (us.getRelativePosition().getY() + ray.getY()));
                g2d.fillOval((int) (us.getRelativePosition().getX() + ray.getX() - 5), (int) (us.getRelativePosition().getY() + ray.getY() - 5), 10, 10);
            }
        }

        g2d.dispose();

        g2d = (Graphics2D) g.create();

        for (Vector2d point : robot.getMap().getLocations()) {
            g.setColor(new Color(242, 163, 60));

            g.fillOval(
                    (int) (point.getX() - robot.getPosition().getX()) + (getWidth() / 2),
                    (int) (point.getY() - robot.getPosition().getY()) + (getHeight() / 2),
                    3,3
            );
        }

        elements.forEach(e -> e.draw(g, fmousePosition));

        debugStr = "{" + aiSpeed + ", " + (aiDirection.getDegrees()) + "}";

        if (Math.abs(aiSpeed) > 0 || Math.abs(aiDirection.getTheta()) > 0) {
            Vector2d move = Vector2d.fromPolar(aiSpeed, robotRotation.rotateBy(Rotation2d.fromRadians(Math.PI / 2)));
            robot.move(move.getX(), move.getY());
            robot.rotate(aiDirection.getRadians());

            // draw vector
            g.setColor(Color.GREEN);
            g.drawLine((int) robot.getPosition().getX(), (int) robot.getPosition().getY(), (int) (move.getX() + robot.getPosition().getX()), (int) (move.getY() + robot.getPosition().getY()));

            if (robot.inCollision()) {
                aiSpeed = 0;
                aiDirection = Rotation2d.zero();
            }
        } else {
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

            if (rotL) {
                robot.rotate(turnSpeed);
            } else if (rotR) {
                robot.rotate(-turnSpeed);
            }
        }

        g.setColor(Color.BLACK);
        g.drawString(debugStr, 5, getHeight() - 10);

        //wait 20 ms
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        repaint();
    }
}
