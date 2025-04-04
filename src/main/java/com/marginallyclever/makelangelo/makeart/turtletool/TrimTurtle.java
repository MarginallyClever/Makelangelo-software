package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.turtle.Line2d;
import com.marginallyclever.makelangelo.turtle.StrokeLayer;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrimTurtle {
    private static final Logger logger = LoggerFactory.getLogger(TrimTurtle.class);

    /**
     * <p>Remove trimHead number of commands from the start of the turtle history.</p>
     * <p>Remove trimTail number of commands from the end of the turtle history.</p>
     * @param turtle the source turtle.
     * @param trimHead the number of commands to remove from the start of the turtle history.
     * @param trimTail the number of commands to remove from the end of the turtle history.
     * @return the {@link Turtle} with the trimmed history.
     */
    public static Turtle run(Turtle turtle, int trimHead, int trimTail) {
        logger.debug("turtle.countLoops={} trimHead={} trimTail={}", turtle.countLoops(), trimHead, trimTail);
        Turtle skinny = newWay(turtle, trimHead, trimTail);
        //Turtle skinny = oldWay(turtle, trimHead, trimTail);

        logger.debug("skinnyTurtle.countLoops={} ?=(trimTail-trimHead)={}", skinny.countLoops(),trimTail-trimHead);
        return skinny;
    }

    private static Turtle newWay(Turtle turtle, int trimHead, int trimTail) {
        // walk the turtle path to the end, but only add the points between trimHead and trimTail to the new turtle.
        var iter = turtle.getIterator();
        // skip the first trimHead points
        for(int i = 0; i < trimHead-1 && iter.hasNext(); ++i) {
            iter.next();
        }
        // create a new turtle
        Turtle skinny = new Turtle();
        StrokeLayer iLayer = null;
        Line2d iLine = null;

        var skinnyLayer = skinny.strokeLayers.getFirst();
        skinnyLayer.setColor(iter.getLayer().getColor());
        skinnyLayer.setDiameter(iter.getLayer().getDiameter());

        // add the points between trimHead and trimTail to the new turtle
        for(int i = trimHead; i < trimTail && iter.hasNext(); ++i) {
            var p = iter.next();
            if(iLine!=iter.getLine()) {
                if(iLayer!=iter.getLayer()) {
                    iLayer = iter.getLayer();
                    skinnyLayer.setColor(iLayer.getColor());
                    skinnyLayer.setDiameter(iLayer.getDiameter());
                }
                // add a new line to the new turtle
                iLine = iter.getLine();
                skinnyLayer.add(new Line2d());
            }
            // add the point to the new turtle
            skinnyLayer.getLast().add(p);
        }

        // insurance?
        skinny.penUp();
        return skinny;
    }
}
