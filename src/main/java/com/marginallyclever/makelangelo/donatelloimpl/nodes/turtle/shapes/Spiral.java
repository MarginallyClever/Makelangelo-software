package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.shapes;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.donatello.ports.InputOneOfMany;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Generates a spiral shape using the {@link Turtle}.
 */
public class Spiral extends Node {
    private final InputOneOfMany style = new InputOneOfMany("style");
    private final InputDouble radiusValue = new InputDouble("radius",500.0);
    private final InputDouble spacingValue = new InputDouble("spacing",5.0);
    private final InputInt countValue = new InputInt("count",50);
    private final OutputTurtle output = new OutputTurtle("output");

    public Spiral() {
        super("Spiral");
        addPort(style);
        addPort(radiusValue);
        addPort(countValue);
        addPort(spacingValue);
        addPort(output);

        style.setOptions(new String[]{"count * spacing","radius / count","radius / spacing"});
    }

    @Override
    public void update() {
        setComplete(0);
        Turtle result = switch(style.getValue()) {
            case 1 -> doRadiusCount(radiusValue.getValue(),countValue.getValue());
            case 2 -> doRadiusSpacing(radiusValue.getValue(),spacingValue.getValue());
            default -> doCountSpacing(countValue.getValue(),spacingValue.getValue());
        };
        output.setValue(result);
        setComplete(100);
    }

    // Generate a spiral using the count (number of turns) and spacing (per turn).
    private Turtle doRadiusCount(double radius,int count) {
        return doRadiusSpacing(radius, radius / count);
    }

    // Generate a spiral using the radius and spacing (per turn).
    private Turtle doCountSpacing(int count,double spacing) {
        return doRadiusSpacing(count*spacing,spacing);
    }

    // Generate a spiral using the radius and count (number of turns).
    private Turtle doRadiusSpacing(double radius,double spacing) {
        Turtle turtle = new Turtle();

        int i;

        double r = radius;
        double fx, fy;
        while (r > spacing/2) {
            // find circumference of current circle
            double c1 = Math.floor((2.0f * r - spacing) * Math.PI);

            for (i = 0; i < c1; ++i) {
                double p = (double)i / c1;
                double f = Math.PI * 2.0 * p;
                double r1 = r - spacing * p;
                fx = Math.cos(f) * r1;
                fy = Math.sin(f) * r1;

                turtle.moveTo(fx, fy);
                turtle.penDown();
            }
            r -= spacing;
        }
        return turtle;
    }
}
