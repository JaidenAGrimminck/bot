package me.autobot.sim.graphics;

import javax.swing.*;
import java.awt.*;

public class SimScreen {
    private GraphicsDevice gd;
    private DisplayMode dm;

    public SimScreen() {
        dm = new DisplayMode(800, 600, 16, DisplayMode.REFRESH_RATE_UNKNOWN);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        gd = ge.getDefaultScreenDevice();
    }

    public void setFullScreen(JFrame window) {
        window.setUndecorated(false);
        window.setResizable(true);
        gd.setFullScreenWindow(window);

        if (dm != null && gd.isDisplayChangeSupported()) {
            try {
                gd.setDisplayMode(dm);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public Window getFullScreenWindow() {
        return gd.getFullScreenWindow();
    }

    public void restoreScreen() {
        Window w = gd.getFullScreenWindow();
        if (w != null) {
            w.dispose();
        }
        gd.setFullScreenWindow(null);
    }
}
