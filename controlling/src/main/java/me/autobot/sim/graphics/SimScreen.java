package me.autobot.sim.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * A class that creates the GUI for the simulation.
 * */
public class SimScreen {
    /**
     * Actually creates the GUI for the simulation.
     * */
    private void createGUI() {
        JFrame frame = new JFrame("Display");

        SimCanvas mainPanel = new SimCanvas(frame);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setFocusable(true);
    }

    /**
     * Creates a new SimScreen object (and GUI for the simulation).
     * */
    public SimScreen() {
        SwingUtilities.invokeLater(this::createGUI);
    }
}
