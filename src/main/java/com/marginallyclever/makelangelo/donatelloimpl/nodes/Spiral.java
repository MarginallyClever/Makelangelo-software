package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtlePathWalker;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Warp an existing path into a spiral.
 */
public class Spiral extends Node {
    private final InputTurtle source = new InputTurtle("turtle");
    private final InputDouble r0 = new InputDouble("r0", 1d);
    private final InputDouble dr = new InputDouble("dr", 1d);
    private final InputDouble stepSize = new InputDouble("stepSize", 1d);
    private final OutputTurtle output = new OutputTurtle("output");

    public Spiral() {
        super("Spiral");
        addVariable(source);
        addVariable(r0);
        addVariable(dr);
        addVariable(stepSize);
        addVariable(output);
    }

    @Override
    public void update() {
        try {
            var r0Value = r0.getValue();
            var drValue = dr.getValue();
            var turtle = source.getValue();
            TurtlePathWalker pathWalker = new TurtlePathWalker(turtle);
            double dist = turtle.getBounds().width;
            var pace = Math.max(0.1, stepSize.getValue());

            double curveSum = 0;
            double px,py;

            var result = new Turtle();
            double t = 0.0;  // the distance along the curve
            setComplete(0);
            while(!pathWalker.isDone()) {
                // get the next point
                var p2 = pathWalker.walk(pace);
                // the x of the original path is the distance forward
                var dt = p2.x-t;
                t = p2.x;
                // radius at this point
                double rN = r0Value + drValue * (curveSum/(2*Math.PI));
                // forward direction
                double nx = Math.cos(curveSum);
                double ny = Math.sin(curveSum);
                // position
                px = rN * nx;
                py = rN * ny;
                // dt is the arc length of the curve between the last point and this point
                // the angle represented by dt and rN is calculated as follows:
                double dtAngle = dt / rN;
                curveSum += dtAngle;
                // the y of the original path is the distance sideways
                double tx = p2.y * nx;
                double ty = p2.y * ny;

                result.moveTo(px+tx,py+ty);
                result.penDown();
                setComplete((int) (t / dist * 100));
            }
            setComplete(100);
            output.send(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
