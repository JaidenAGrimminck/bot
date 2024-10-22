package me.autobot.sim.graphics.elements;

import me.autobot.lib.math.coordinates.Int2;

import java.awt.*;

/**
 * An element that can be drawn on a canvas.
 * */
public abstract class CanvasElement {

    /**
     * Creates a new canvas element.
     * */
    public CanvasElement() {}

    /**
     * Draw the element on the canvas.
     * @param g The graphics object to draw on.
     * @param mousePosition The position of the mouse on the canvas.
     * */
    public void draw(Graphics g, Int2 mousePosition) {

    }


}
