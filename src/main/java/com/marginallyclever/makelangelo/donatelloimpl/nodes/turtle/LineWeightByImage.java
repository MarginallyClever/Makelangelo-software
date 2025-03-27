package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputImage;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.turtletool.ThickenLinesByIntensity;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Lay the path over the image and change the width of the line by the intensity of the image at the same
 * location.  The fine grain resolution (and the amount of testing) is controlled by the stepSize.
 * @author Dan Royer
 * @since 2025-01-07
 */
public class LineWeightByImage extends Node {
    private final InputImage image = new InputImage("image");
    private final InputTurtle turtle = new InputTurtle("turtle");
    private final InputDouble stepSize = new InputDouble("stepSize", 5d);
    private final InputDouble thickness = new InputDouble("thickness", 5d);
    private final InputDouble penDiameter = new InputDouble("pen diameter", 0.8);
    private final OutputTurtle result = new OutputTurtle("result");

    public LineWeightByImage() {
        super("LineWeightByImage");
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
        if(myTurtle==null || myTurtle.history.isEmpty()) return;

        TransformedImage sourceImage = new TransformedImage(image.getValue());
        sourceImage.setScale(1,1);
        sourceImage.setTranslation(0,0);
        setComplete(0);

        var tool = new ThickenLinesByIntensity();
        Turtle turtle = tool.execute(myTurtle,sourceImage,stepSize.getValue(),thickness.getValue(),penDiameter.getValue());

        setComplete(100);
        result.setValue(turtle);
    }
}
