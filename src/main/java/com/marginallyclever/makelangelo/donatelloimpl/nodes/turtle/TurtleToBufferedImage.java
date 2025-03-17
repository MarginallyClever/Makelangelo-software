package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.OutputImage;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleToBufferedImageHelper;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.image.BufferedImage;

/**
 * Convert a {@link Turtle} to a {@link BufferedImage}.
 */
public class TurtleToBufferedImage extends Node {
    private final InputTurtle turtle = new InputTurtle("turtle");
    private final OutputImage output = new OutputImage("output");

    public TurtleToBufferedImage() {
        super("TurtleToBufferedImage");
        addPort(turtle);
        addPort(output);
    }

    @Override
    public void update() {
        Turtle source = turtle.getValue();
        output.setValue(TurtleToBufferedImageHelper.generateImage(source,this,(int)getRectangle().getWidth()));
    }
}
