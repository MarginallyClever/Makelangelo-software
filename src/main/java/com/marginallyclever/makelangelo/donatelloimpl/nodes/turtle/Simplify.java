package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;


import com.marginallyclever.convenience.linecollection.LineCollection;
import com.marginallyclever.convenience.linecollection.RamerDouglasPeuckerRecursive;
import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

public class Simplify extends Node {
    private final InputTurtle inputTurtle = new InputTurtle("input");
    private final OutputTurtle outputTurtle = new OutputTurtle("output");
    private final InputDouble distanceTolerance = new InputDouble("distanceTolerance", 1.6);

    public Simplify() {
        super("Simplify");
        addPort(inputTurtle);
        addPort(distanceTolerance);
        addPort(outputTurtle);
    }

    @Override
    public void update() {
        var before = inputTurtle.getValue();
        if(before==null || !before.hasDrawing()) return;
        setComplete(0);

        LineCollection beforeLines = before.getAsLineSegments();
        LineCollection afterLines = removeColinearSegments(beforeLines);

        Turtle t = new Turtle();
        t.addLineSegments(afterLines);
        outputTurtle.setValue(t);
        setComplete(100);
    }

    /**
     * Split the collection by color, then by travel moves to get contiguous blocks in a single color.
     * simplify these blocks using Douglas-Peucker method.
     * @param originalLines the lines to simplify
     * @return the simplified lines
     */
    private LineCollection removeColinearSegments(LineCollection originalLines) {
        LineCollection result = new LineCollection();
        var d = distanceTolerance.getValue();

        var byColor = originalLines.splitByColor();
        for(LineCollection c : byColor ) {
            var byTravel = c.splitByTravel();
            for(LineCollection t : byTravel ) {
                LineCollection after = (new RamerDouglasPeuckerRecursive(t)).simplify(d);
                result.addAll(after);
            }
        }

        return result;
    }
}
