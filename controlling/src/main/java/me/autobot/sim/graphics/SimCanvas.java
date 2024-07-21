package me.autobot.sim.graphics;

import javax.swing.*;
import java.awt.*;

public class SimCanvas extends JFrame {
    public void run() {
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        setFont(new Font("Arial", Font.PLAIN, 24));

        SimScreen screen = new SimScreen();

        try {
            screen.setFullScreen(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //keep on drawing
        while (true) {
            repaint();
            try {
                Thread.sleep(20);
            } catch (Exception e) {
            }
        }
    }

    double t1 = 0;

    public void paint(Graphics g) {
        if (t1 == 0) {
            t1 = System.currentTimeMillis();
        }

        //draw a box
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, (int) Math.pow(2, (System.currentTimeMillis() - t1) / 1000), 200);
        g.setColor(Color.BLACK);


    }
}
