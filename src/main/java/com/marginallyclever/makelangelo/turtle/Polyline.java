package com.marginallyclever.makelangelo.turtle;

import java.awt.*;

/**
 * A poly line is a collection of x,y coordinates and a color.
 */
public class Polyline {
    private final int[] x;
    private final int[] y;
    private final int n;
    private final Color color;

    public Polyline(int[] x, int[] y, int n, Color color) {
        if (x == null || y == null) throw new IllegalArgumentException("x and y must not be null");
        if (n < x.length) {
            // trim the buffers to the correct size.
            int[] newX = new int[n];
            int[] newY = new int[n];
            System.arraycopy(x, 0, newX, 0, n);
            System.arraycopy(y, 0, newY, 0, n);
            x = newX;
            y = newY;
        }
        this.x = x;
        this.y = y;
        this.n = n;
        this.color = color;
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.drawPolyline(x, y, n);
    }
}
