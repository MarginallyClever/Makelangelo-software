package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.makeart.turtletool.CropTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.geom.Rectangle2D;

/**
 * Crop a {@link Turtle} to a rectangle.
 */
public class CropToRectangle extends Node {
    private final InputTurtle input = new InputTurtle("input");
    private final InputInt w = new InputInt("width", 420);  // A2 width
    private final InputInt h = new InputInt("height", 594);  // A2 height
    private final OutputTurtle output = new OutputTurtle("output");

    public CropToRectangle() {
        super("Crop");
        addPort(input);
        addPort(w);
        addPort(h);
        addPort(output);
    }

    @Override
    public void update() {
        var turtle = input.getValue();
        if (turtle == null) return;

        Turtle result = new Turtle(turtle);
        var width = w.getValue();
        var height = h.getValue();
        Rectangle2D.Double rectangle = new Rectangle2D.Double(0, 0, width, height);
        CropTurtle.run(result, rectangle);
        output.setValue(result);
    }
}
