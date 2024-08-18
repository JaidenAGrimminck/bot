package me.autobot.lib.graphics;

import java.awt.*;

public class Draw {
    public static void fillRotatedRect(Graphics g, double x, double y, double width, double height, double angle) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x, y);
        g2d.rotate(angle);
        //use x,y as top left corner
        g2d.fillRect(0, 0, (int) width, (int) height);
        g2d.dispose();
    }
}
