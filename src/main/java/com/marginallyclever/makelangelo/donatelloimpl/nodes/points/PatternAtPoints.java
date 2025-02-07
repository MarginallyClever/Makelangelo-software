package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputImage;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Place a pattern on a path.  Modulate the pattern by the image.
 */
public class PatternAtPoints extends Node {
    private final InputTurtle pattern = new InputTurtle("pattern");
    private final InputPoints listOfPoints = new InputPoints("points");
    private final InputImage inputImage = new InputImage("image");
    private final InputDouble min = new InputDouble("min", 0.1);
    private final InputDouble max = new InputDouble("max", 1.0);
    private final OutputTurtle output = new OutputTurtle("output");

    public PatternAtPoints() {
        super("PatternAtPoints");
        addVariable(pattern);
        addVariable(listOfPoints);
        addVariable(inputImage);
        addVariable(min);
        addVariable(max);
        addVariable(output);
    }

    @Override
    public void update() {
        Turtle result = new Turtle();
        Turtle myPattern = pattern.getValue();
        ListOfPoints points = listOfPoints.getValue();
        BufferedImage image = inputImage.getValue();
        var w = image.getWidth();
        var h = image.getHeight();

        if(points.isEmpty()) {
            setComplete(100);
            output.send(result);
            return;
        }

        double bottom = min.getValue();
        double diff = max.getValue() - min.getValue();

        setComplete(0);
        int total = points.size();
        int i=0;
        for(var p : points) {
            Turtle stamp = new Turtle(myPattern);
            if(p.x>=0 && p.x<w && p.y>=0 && p.y<h) {
                // inside image
                var c = new Color(image.getRGB((int)p.x,(int)p.y));
                // get intensity of c as a value 0....1
                var intensity = (c.getBlue()+c.getGreen()+c.getRed())/(3.0*255.0);
                var capped = Math.max(0,Math.min(1,intensity));
                var i2 = bottom + capped * diff;
                stamp.scale(i2,i2);
            }
            stamp.translate(p.x,p.y);
            result.add(stamp);
            setComplete((100*i++/total));
        }
        setComplete(100);
        output.send(result);
    }
}
