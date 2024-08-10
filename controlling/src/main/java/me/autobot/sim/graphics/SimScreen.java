package me.autobot.sim.graphics;

import javax.swing.*;
import java.awt.*;

public class SimScreen {
    private void createGUI() {
        SimCanvas mainPanel = new SimCanvas();

        JFrame frame = new JFrame("Display");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    public SimScreen() {
        SwingUtilities.invokeLater(this::createGUI);
    }
}
