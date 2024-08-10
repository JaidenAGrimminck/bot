package me.autobot.sim.graphics.elements;

import me.autobot.lib.math.coordinates.Int2;

import java.awt.*;

public class CanvasButton extends CanvasElement {

    Int2 position;
    Int2 size;

    public String title;
    Runnable action;

    private Color backgroundColor = Color.GRAY;
    private Color textColor = Color.WHITE;
    private Color hoverColor = Color.LIGHT_GRAY;

    private int fontSize = 24;

    private Int2 fontPadding = new Int2(10, 20);

    public CanvasButton(int x, int y, int width, int height, String title, Runnable action) {
        this.position = new Int2(x, y);
        this.size = new Int2(width, height);

        this.title = title;
        this.action = action;
    }

    public CanvasButton setStyle(Color backgroundColor, Color textColor, Color hoverColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.hoverColor = hoverColor;

        return this;
    }

    public CanvasButton adjustText(int fontSize, int leftPadding, int topPadding) {
        this.fontSize = fontSize;
        this.fontPadding = new Int2(leftPadding, topPadding);

        return this;
    }

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

    public boolean isInside(Int2 point) {
        return point.x > position.x && point.x < position.x + size.x && point.y > position.y && point.y < position.y + size.y;
    }

    public void run() {
        action.run();
    }

}
