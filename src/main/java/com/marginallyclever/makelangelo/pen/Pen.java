package com.marginallyclever.makelangelo.pen;

import java.awt.*;

/**
 * Describes a pen that can be used to draw on a surface.
 */
public class Pen {
    public String name;
    public Color color = new Color(0,0,0);
    public double radius;

    public Pen() {
        this("Black");
    }

    public Pen(String name) {
        super();
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " " + radius + " "+color.getRed()+" "+color.getGreen()+" "+color.getBlue();
    }
}