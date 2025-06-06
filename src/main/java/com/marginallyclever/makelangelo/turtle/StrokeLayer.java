package com.marginallyclever.makelangelo.turtle;

import java.awt.*;

/**
 * This class is a {@link ListOfLines} with a given stroke (color and diameter).
 */
public class StrokeLayer extends ListOfLines {
    private Color color;
    private double diameter;

    public StrokeLayer(Color color, double diameter) {
        super();
        this.color = color;
        this.diameter = diameter;
    }

    /**
     * Creates a new {@link StrokeLayer} with the same color and diameter as arg0.
     * @param arg0 the {@link StrokeLayer} to copy
     */
    public StrokeLayer(StrokeLayer arg0) {
        this(arg0.color,arg0.diameter);
        for(Line2d line : arg0.getAllLines()) {
            this.addLast(new Line2d(line));
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color arg0) {
        color = arg0;
    }

    public double getDiameter() {
        return diameter;
    }

    public void setDiameter(double arg0) {
        diameter = arg0;
    }
}
