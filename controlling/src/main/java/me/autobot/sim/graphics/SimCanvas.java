package me.autobot.sim.graphics;

import me.autobot.lib.robot.Robot;
import me.autobot.lib.math.Mathf;
import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Int2;
import me.autobot.lib.math.coordinates.Vector2d;
import me.autobot.lib.math.map.Map2d;
import me.autobot.lib.math.rotation.Rotation2d;
import me.autobot.lib.robot.Sensor;
import me.autobot.lib.robot.sensors.UltrasonicSensor;
import me.autobot.lib.tools.RunnableWithArgs;
import me.autobot.server.WSClient;
import me.autobot.sim.MapLoader;
import me.autobot.sim.Simulation;
import me.autobot.sim.evolution.EvolutionTracker;
import me.autobot.sim.graphics.elements.CanvasButton;
import me.autobot.sim.graphics.elements.CanvasElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


/**
 * The simulation canvas is a JPanel that is used to draw the simulation.
 * */
public class SimCanvas extends JPanel {
    /**
     * The frame to draw the canvas on.
     * */
    private JFrame frame;

    /**
     * Creates a new simulation canvas.
     * @param frame The frame to draw the canvas on.
     * */
    public SimCanvas(JFrame frame) {
        this.frame = frame;
        new Thread(this::run).start();
    }


    /**
     * If the up key is pressed.
     * */
    private boolean up = false;
    /**
     * If the down key is pressed.
     * */
    private boolean down = false;
    /**
     * If the left key is pressed.
     * */
    private boolean left = false;
    /**
     * If the right key is pressed.
     * */
    private boolean right = false;

    /**
     * If the rotation right key is pressed.
     * */
    private boolean rotR = false;
    /**
     * If the rotation left key is pressed.
     * */
    private boolean rotL = false;

    /**
     * Enables the block map to be drawn.
     * */
    private boolean mapEnabled = true;
    /**
     * Whether to display known points ("recorded" points) or not.
     * */
    private boolean knownPointsEnabled = true;

    /**
     * Speed of the robot.
     * */
    private int speed = 10;
    /**
     * Turn speed of the robot.
     * */
    private double turnSpeed = Math.PI / 100;


    // for the robot ai controls
    /**
     * Number of AI robots.
     * */
    final static public int numberOfAIRobots = 10;

    /**
     * The evolution tracker of the simulation.
     * */
    private EvolutionTracker evoTracker;

    /**
     * A map of obstacles.
     * @see Map2d
     * */
    public static Map2d obstaclesMap = new Map2d();

    /**
     * Startup of the simulation canvas.
     * */
    public void run() {
        evoTracker = new EvolutionTracker(Robot.getRobots());

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
                int robotID = Mathf.allPos(((int[]) args[0])[0]);
                int action = Mathf.allPos(((int[]) args[0])[1]);

                switch (action) {
                    case 0xA1:
                        evoTracker.changeSpeed(robotID, true);
                        break;
                    case 0xA2:
                        evoTracker.changeSpeed(robotID, false);
                        break;
                    case 0xA3:
                        evoTracker.rotate(robotID, true);
                        break;
                    case 0xA4:
                        evoTracker.rotate(robotID, false);
                    case 0xA0:
                        break;
                }
            }
        });

        WSClient.registerCallable(0xB5, new RunnableWithArgs() {
            @Override
            public void run(WSClient client) {
                evoTracker.assignWSRef(client);
                evoTracker.start();
                System.out.println("[Evolution] Started sim.");
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

    /**
     * Gets the preferred size of the canvas.
     * @return The preferred size of the canvas.
     * */
    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        return new Dimension(800, 600);
    }

    /**
     * List of elements on the canvas
     * */
    private ArrayList<CanvasElement> elements = new ArrayList<>();

    /**
     * The mouse position.
     * */
    private Int2 mousePosition = Int2.zero();

    /**
     * Just used for debugging purposes.
     * */
    public static String debugStr = "";

    /**
     * The paint method of the canvas, draws it every frame.
     * @param g The graphics object to draw with.
     * */
    public void paint(Graphics g) {
        if (Robot.getRobots().isEmpty()) return;

        // clears the canvas
        g.clearRect(0, 0, getWidth(), getHeight());

        //the mouse position (used for any threads or whatevers)
        final Int2 fmousePosition = mousePosition;

        //the focused robot (where the camera view is gonna be)
        int topIndex = 0;
        double topScore = -Double.MAX_VALUE;
        for (int i = 0; i < evoTracker.getScores().length; i++) {
            if (evoTracker.getScores()[i] > topScore) {
                topScore = evoTracker.getScores()[i];
                topIndex = i;
            }
        }

        Robot focusedRobot = Robot.getRobots().get(topIndex); //sort by advantage to focus on top robot.

        //create some graphics
        Graphics2D g2d = (Graphics2D) g.create();

        //move it to the center
        g2d.translate((getWidth() / 2), (getHeight() / 2));

        //draw the obstacles (boxes)
        for (Box2d object : Simulation.getInstance().environment.obstacles) {
            if (object.signedDistance(focusedRobot.getPosition()) < 1000d) {
                g2d.setColor(Color.RED);
            } else {
                continue;
            }

            for (Robot robot : Robot.getRobots())
                for (int i = 0x00; i <= robot.getSensors().size(); i++)
                    if (object.flags.getOrDefault(robot.getIdentification() + i + "hit", false))
                        g2d.setColor(Color.GREEN);

            if (mapEnabled) g2d.fillRect(object.getPosition().x - (int) focusedRobot.getPosition().getX(), object.getPosition().y - (int) focusedRobot.getPosition().getY(), object.getSize().x, object.getSize().y);
        }

        //dispose that
        g2d.dispose();

        //draw the robots
        for (Robot bot : Robot.getRobots()) {
            Rotation2d robotRotation = bot.getRotation();

            g2d = (Graphics2D) g.create();

            //translate to center if focused on that bot
            if (bot.getIdentification() == focusedRobot.getIdentification()) {
                g2d.translate(
                        (getWidth() / 2), (getHeight() / 2)
                );
            } else {
                //else, translate to the bot's position
                g2d.translate(
                        (int) (bot.getPosition().getX() - focusedRobot.getPosition().getX()) + (getWidth() / 2),
                        (int) (bot.getPosition().getY() - focusedRobot.getPosition().getY()) + (getHeight() / 2)
                );
            }
            g2d.rotate(robotRotation.getTheta());

            // 1px = 1cm
            g2d.setColor(Color.BLACK);
            if (bot.inCollision()) {
                g2d.setColor(Color.RED);
            }
            g2d.fillRect(
                    (int) (-bot.getRobotSize().getX() / 2), (int) (-bot.getRobotSize().getY() / 2),
                    (int) (bot.getRobotSize().getX()), (int) (bot.getRobotSize().getY())
            );

            g2d.setColor(Color.WHITE);
            g2d.fillOval(-15, 20, 5, 5);
            g2d.fillOval(10, 20, 5, 5);

            for (Sensor sensor : bot.getSensors()) {
                if (sensor instanceof UltrasonicSensor) {
                    UltrasonicSensor us = (UltrasonicSensor) sensor;
                    g2d.setColor(Color.BLUE);
                    if (us.getIdentifier() == 0x01) {
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
        }

        g2d = (Graphics2D) g.create();

        for (Vector2d point : obstaclesMap.getLocations()) {
            g.setColor(new Color(242, 163, 60));

            g.fillOval(
                    (int) (point.getX() - focusedRobot.getPosition().getX()) + (getWidth() / 2),
                    (int) (point.getY() - focusedRobot.getPosition().getY()) + (getHeight() / 2),
                    3,3
            );
        }

        elements.forEach(e -> e.draw(g, fmousePosition));

        //debugStr = "{" + evoTracker.getSpeeds()[0] + ", " + (evoTracker.getRotations()[0].getDegrees()) + "}";
        debugStr = "{gen: " + evoTracker.getGeneration() + ", timeleft: " + evoTracker.getTimeLeft() + "}";

        for (int i = 0; i < Robot.getRobots().size(); i++) {
            Robot bot = Robot.getRobots().get(i);

            double aiSpeed = evoTracker.getSpeeds()[i];
            Rotation2d aiDirection = evoTracker.getRotations()[i];

            if (Math.abs(aiSpeed) > 0 || Math.abs(aiDirection.getTheta()) > 0) {
                Vector2d move = Vector2d.fromPolar(aiSpeed, bot.getRotation().rotateBy(Rotation2d.fromRadians(Math.PI / 2)));
                bot.move(move.getX(), move.getY());
                bot.rotate(aiDirection.getRadians());

                // draw vector
                g.setColor(Color.GREEN);
                g.drawLine((int) bot.getPosition().getX(), (int) bot.getPosition().getY(), (int) (move.getX() + bot.getPosition().getX()), (int) (move.getY() + bot.getPosition().getY()));

                if (bot.inCollision()) {
                    evoTracker.stop(i);
                }
            } else {
                if (down) {
                    bot.move(0, speed);
                }
                if (up) {
                    bot.move(0, -speed);
                }
                if (left) {
                    bot.move(-speed, 0);
                }
                if (right) {
                    bot.move(speed, 0);
                }

                if (rotL) {
                    bot.rotate(turnSpeed);
                } else if (rotR) {
                    bot.rotate(-turnSpeed);
                }
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
