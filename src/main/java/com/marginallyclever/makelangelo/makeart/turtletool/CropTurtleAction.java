package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Crop a Turtle's drawing to fit within the Paper's margins.
 */
public class CropTurtleAction extends TurtleTool {
    private final Paper paper;

    public CropTurtleAction(Paper paper) {
        super(Translator.get("CropTurtleAction"));
        this.paper = paper;
    }

    @Override
    public Turtle run(Turtle turtle) {
        Turtle result = new Turtle(turtle);
        CropTurtle.run(result, paper.getMarginRectangle());
        return result;
    }
}
