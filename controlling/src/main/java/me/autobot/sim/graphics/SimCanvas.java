package me.autobot.sim.graphics;

import me.autobot.lib.math.coordinates.Int2;
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

        // 1px = 1cm


        g.setColor(Color.BLACK);
        g.fillRect((getWidth() / 2) - 20, (getHeight() / 2) - 30, 40, 60);



        //wait 20 ms
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        repaint();
    }
}
