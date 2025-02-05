package com.marginallyclever.makelangelo.turtle;

import java.awt.*;

/**
 * A tool for creating a {@link Polyline}.  Optimizes to remove points on nearly straight lines.
 */
public class PolylineBuilder {
    private int[] x;
    private int[] y;
    private int size;

    public PolylineBuilder() {
        x = new int[100];
        y = new int[100];
        size = 0;
    }

    public void add(int x, int y) {
        if (size >= this.x.length) {
            // grow the buffer if needed.
            int[] newX = new int[this.x.length * 2];
            int[] newY = new int[this.y.length * 2];
            System.arraycopy(this.x, 0, newX, 0, size);
            System.arraycopy(this.y, 0, newY, 0, size);
            this.x = newX;
            this.y = newY;
        }
        this.x[size] = x;
        this.y[size] = y;
        size++;
    }

    /**
     * Build a {@link Polyline} with the given color.  This is the same as calling {@link #compile(Color, int)}
     * with a deviation of 10 degrees.
     *
     * @param color the color of the line.
     * @return a {@link Polyline} with the given color.
     */
    public Polyline compile(Color color) {
        return compile(color, 10);
    }

    /**
     * Build a {@link Polyline} with the given color.  Remove any points that form a nearly straight line.
     *
     * @param color the color of the line.
     * @param deviationDegrees the maximum deviation in degrees between two lines to be considered a straight line.  Can
     *                         be zero.  If deviation is negative, no optimization will be performed.
     * @return a {@link Polyline} with the given color.
     */
    public Polyline compile(Color color, int deviationDegrees) {
        if (size < 3) {
            return new Polyline(x.clone(), y.clone(), size, color);
        }

        // examine the buffers and remove any points that form a nearly straight line.
        // use a dot product to determine if the angle between the two lines is less than `maxDeviation` degrees.
        var nx = new int[size];
        var ny = new int[size];
        int j = 0;
        nx[j] = x[0];
        ny[j] = y[0];
        j++;

        var maxDeviation = deviationDegrees>0 ? Math.toRadians(deviationDegrees) : -1;

        var x0 = x[0];
        var y0 = y[0];
        var x1 = x[1];
        var y1 = y[1];
        for (int i = 2; i < size; i++) {
            var x2 = x[i];
            var y2 = y[i];
            if(maxDeviation>=0) {
                // compare line 0-1 with line 1-2
                var dx1 = x1 - x0;
                var dy1 = y1 - y0;
                var dx2 = x2 - x1;
                var dy2 = y2 - y1;
                var dot = dx1 * dx2 + dy1 * dy2;
                var len1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
                var len2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);
                var len = len1 * len2;
                var angle = len == 0 ? 0 : Math.acos(dot / len);  // no divide by zero
                // if the angle is less than the deviation, skip point 1.
                if (angle > maxDeviation) {
                    // otherwise save point 1
                    nx[j] = x1;
                    ny[j] = y1;
                    j++;
                    x0 = x1;
                    y0 = y1;
                }
            }
            // move on to the next point.
            x1 = x2;
            y1 = y2;
        }
        nx[j] = x1;
        ny[j] = y1;
        j++;
        //if(j<n) System.out.println("saved "+(n-j)+" points");
        return new Polyline(nx, ny, j, color);
    }

    public void clear() {
        size = 0;
    }

    public int getSize() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
}
