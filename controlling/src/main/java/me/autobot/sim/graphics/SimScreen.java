package me.autobot.sim.graphics;

import javax.swing.*;
import java.awt.*;

public class SimScreen {
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

    public SimScreen() {
        SwingUtilities.invokeLater(this::createGUI);
    }
}
