package me.autobot.sim.graphics.elements;

import me.autobot.lib.math.coordinates.Int2;

import java.awt.*;

/**
 * A button that can be drawn on a canvas.
 * */
public class CanvasButton extends CanvasElement {

    Int2 position;
    Int2 size;

    /**
     * The title of the button.
     * */
    public String title;
    Runnable action;

    private Color backgroundColor = Color.GRAY;
    private Color textColor = Color.WHITE;
    private Color hoverColor = Color.LIGHT_GRAY;

    private int fontSize = 24;

    private Int2 fontPadding = new Int2(10, 20);

    /**
     * Creates a new CanvasButton with the given position, size, title, and action.
     * @param x The x position of the button.
     * @param y The y position of the button.
     * @param width The width of the button.
     * @param height The height of the button.
     * @param title The title of the button.
     * @param action The action to run when the button is clicked.
     * */
    public CanvasButton(int x, int y, int width, int height, String title, Runnable action) {
        this.position = new Int2(x, y);
        this.size = new Int2(width, height);

        this.title = title;
        this.action = action;
    }

    /**
     * Sets the style of the button.
     * @param backgroundColor The background color of the button.
     * @param textColor The text color of the button.
     * @param hoverColor The color of the button when the mouse is hovering over it.
     * @return The button object.
     * */
    public CanvasButton setStyle(Color backgroundColor, Color textColor, Color hoverColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.hoverColor = hoverColor;

        return this;
    }

    /**
     * Adjusts the text of the button.
     * @param fontSize The font size of the text.
     * @param leftPadding The left padding of the text.
     * @param topPadding The top padding of the text.
     * @return The button object.
     * */
    public CanvasButton adjustText(int fontSize, int leftPadding, int topPadding) {
        this.fontSize = fontSize;
        this.fontPadding = new Int2(leftPadding, topPadding);

        return this;
    }

    /**
     * Draws the button on the canvas. Called internally by the canvas.
     * @param g The graphics object to draw on.
     * @param mousePosition The position of the mouse.
     * */
    @Override
    public void draw(Graphics g, Int2 mousePosition) {
        if (isInside(mousePosition)) {
            g.setColor(hoverColor);
        } else {
            g.setColor(backgroundColor);
        }

        g.fillRect(position.x, position.y, size.x, size.y);

        // color of the text
        g.setColor(textColor);
        g.setFont(new Font("Arial", Font.PLAIN, fontSize));
        g.drawString(title, position.x + fontPadding.x, position.y + fontPadding.y);
    }

    /**
     * Checks if the given point is inside the button.
     * @param point The point to check. This should be the mouse position.
     * @return Whether the point is inside the button.
     * */
    public boolean isInside(Int2 point) {
        return point.x > position.x && point.x < position.x + size.x && point.y > position.y && point.y < position.y + size.y;
    }

    /**
     * Run the action of the button.
     * @see Runnable
     * */
    public void run() {
        action.run();
    }

}
