package me.autobot.sim.graphics;

import me.autobot.code.Robot;
import me.autobot.lib.math.Unit;
import me.autobot.lib.math.coordinates.Box2d;
import me.autobot.lib.math.coordinates.Int2;
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
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class SimCanvas extends JPanel {
    public SimCanvas() {
        new Thread(this::run).start();
    }

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

        try {
            Simulation.getInstance().environment.obstacles = MapLoader.mapToObjects(MapLoader.loadMap("/Users/jgrimminck/Documents/coding projects/bot/controlling/src/maps/map.txt"), 20);
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

    public void paint(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());

        final Int2 fmousePosition = mousePosition;

        elements.forEach(e -> e.draw(g, fmousePosition));

        //get all objects nearby the robot
        Robot robot = Simulation.getInstance().getRobot();

        for (Box2d object : Simulation.getInstance().environment.obstacles) {
            if (robot.getPosition().distance(object.getPosition().toVector2d()) < 1000d) {
                g.setColor(Color.RED);
            } else {
                continue;
            }

            g.fillRect(object.getPosition().x - (int) robot.getPosition().getX(), object.getPosition().y - (int) robot.getPosition().getY(), object.getSize().x, object.getSize().y);
        }

        // 1px = 1cm
        g.setColor(Color.BLACK);
        g.fillRect((getWidth() / 2) - 20, (getHeight() / 2) - 30, 40, 60);


        ArrayList<Sensor> sensors = robot.getSensors();

        for (Sensor sensor : sensors) {
            if (sensor instanceof UltrasonicSensor) {
                UltrasonicSensor us = (UltrasonicSensor) sensor;
                g.setColor(Color.BLUE);
                g.fillOval((int) ((getWidth() / 2) + us.getRelativePosition().getX() - 5), (int) ((getHeight() / 2) + us.getRelativePosition().getY() - 5), 10, 10);

                double distance = us.getDistance().getValue(Unit.Type.CENTIMETER);

//                if (sensor.getAddress() == 0x04) {
//                    System.out.println("Distance: " + distance);
//                }

                Vector2d ray = Vector2d.fromPolar(distance, Rotation2d.fromRadians(us.getRelativeRotation().getThetaRadians()));

                g.drawLine((int) (getWidth() / 2 + us.getRelativePosition().getX()), (int) (getHeight() / 2 + us.getRelativePosition().getY()), (int) (getWidth() / 2 + us.getRelativePosition().getX() + ray.getX()), (int) (getHeight() / 2 + us.getRelativePosition().getY() + ray.getY()));

            }
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
