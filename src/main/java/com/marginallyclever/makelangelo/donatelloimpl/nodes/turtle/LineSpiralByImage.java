package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputImage;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.turtletool.SpiralByIntensity;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * <p>Lay the path over the image and replace the line with a spiral, such that the radius and frequency are
 * controlled by the intensity of the image at the same point</p>
 * <h4>Ports</h4>
 * <ul>
 *     <li>turtle - the turtle to modify</li>
 *     <li>image - the image to use for line width</li>
 *     <li>stepSize - the distance between each pixel in the image</li>
 *     <li>thickness - the maximum line width</li>
 *     <li>pen diameter - the diameter of the pen</li>
 *     <li>result - the resulting turtle</li>
 * </ul>
 */
public class LineSpiralByImage extends Node {
    private final InputTurtle turtle = new InputTurtle("turtle");
    private final InputImage image = new InputImage("image");
    private final InputDouble stepSize = new InputDouble("stepSize", 1d);
    private final InputDouble thickness = new InputDouble("thickness", 5d);
    private final InputDouble penDiameter = new InputDouble("pen diameter", 0.8);
    private final OutputTurtle result = new OutputTurtle("result");

    public LineSpiralByImage() {
        super("LineSpiralByImage");
        addPort(image);
        addPort(turtle);
        addPort(stepSize);
        addPort(thickness);
        addPort(penDiameter);
        addPort(result);
    }

    @Override
    public void update() {
        Turtle myTurtle = turtle.getValue();
        if(myTurtle==null || !myTurtle.hasDrawing()) return;

        setComplete(0);

        var sourceImage = new TransformedImage(image.getValue());
        var tool = new SpiralByIntensity(sourceImage, thickness.getValue()/2.0, stepSize.getValue());
        result.setValue(tool.turtleToSpiral(myTurtle));

        setComplete(100);
    }
}
