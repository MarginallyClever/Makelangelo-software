package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Place a pattern on a path.
 */
public class PatternAtPoints extends Node {
    private final InputTurtle pattern = new InputTurtle("pattern");
    private final InputPoints listOfPoints = new InputPoints("points");
    private final OutputTurtle output = new OutputTurtle("output");

    public PatternAtPoints() {
        super("PatternAtPoints");
        addVariable(pattern);
        addVariable(listOfPoints);
        addVariable(output);
    }

    @Override
    public void update() {
        Turtle result = new Turtle();
        Turtle myPattern = pattern.getValue();
        ListOfPoints points = listOfPoints.getValue();
        if(points.isEmpty()) {
            setComplete(100);
            output.send(result);
            return;
        }
        setComplete(0);
        int total = points.size();
        int i=0;
        for(var p : points) {
            Turtle stamp = new Turtle(myPattern);
            stamp.translate(p.x,p.y);
            result.add(stamp);
            setComplete((100*i++/total));
        }
        setComplete(100);
        output.send(result);
    }
}
