package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtlePathWalker;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;

/**
 * Warp an existing path into a spiral.
 */
public class Spiral extends Node {
    private final Input<Turtle> source = new Input<>("turtle", Turtle.class, new Turtle());
    private final Input<Number> twist = new Input<>("twist", Number.class, 1);
    private final Input<Number> stepSize = new Input<>("stepSize", Number.class, 1);
    private final Output<Turtle> output = new Output<>("output", Turtle.class, new Turtle());

    public Spiral() {
        super("Spiral");
        addVariable(source);
        addVariable(twist);
        addVariable(stepSize);
        addVariable(output);
    }

    @Override
    public void update() {
        try {
            var twistValue = twist.getValue().doubleValue();
            var turtle = source.getValue();
            TurtlePathWalker pathWalker = new TurtlePathWalker(turtle);
            double dist = turtle.getBounds().width;
            var pace = Math.max(0.0001, stepSize.getValue().doubleValue());

            double curve = Math.toRadians(twistValue);
            double curveSum = 0;
            double px=0,py=0;

            var result = new Turtle();
            double t = 0.0;  // the distance along the curve
            setComplete(0);
            while(!pathWalker.isDone()) {
                // get the next point
                var p2 = pathWalker.walk(pace);
                // the x of the original path is the distance forward
                var dx = p2.x-t;
                t = p2.x;
                // forward direction
                double nx = Math.cos(curveSum);
                double ny = Math.sin(curveSum);
                curveSum += dx * curve;  // as px/py move forward, the curve increases proportionally.
                curve += Math.toRadians(dx * 0.00001);
                px += dx * nx;
                py += dx * ny;
                // the y of the original path is the distance sideways
                double tx = p2.y * ny;
                double ty = p2.y * -nx;

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
