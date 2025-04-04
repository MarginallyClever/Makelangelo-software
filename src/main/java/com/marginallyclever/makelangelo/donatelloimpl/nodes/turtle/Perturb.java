package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

/**
 * The {@link Perturb} class is a {@link Node} that modifies the path history
 * of a {@link Turtle} by introducing random perturbations to its movement coordinates.
 * Each movement, excluding tool change operations, is adjusted by a random offset
 * within a specified maximum distance.
 */
public class Perturb extends Node {
    private final InputTurtle turtleIn = new InputTurtle("turtle");
    private final InputDouble maxDistance = new InputDouble("distance",10d);
    private final OutputTurtle turtleOut = new OutputTurtle("result");
    
    public Perturb() {
        super("Perturb");
        addPort(turtleIn);
        addPort(maxDistance);
        addPort(turtleOut);
    }
    
    @Override
    public void update() {
        double d = maxDistance.getValue();
        Turtle in = turtleIn.getValue();
        Turtle out = new Turtle();

        float size = in.countPoints()+1;
        int i=0;
        setComplete(0);
        for( var layer : in.strokeLayers ) {
            for( var line : layer.getAllLines() ) {
                for( var point : line.getAllPoints() ) {
                    var radian = (Math.random() * Math.PI * 2.0);
                    var randomD = Math.random() * d;
                    point.x += Math.cos(radian) * randomD;
                    point.y += Math.sin(radian) * randomD;
                    setComplete((int)(i++ * 100f / size));
                }
            }
        }
        setComplete(100);
        turtleOut.setValue(out);
    }
}
