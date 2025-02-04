package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
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
        addVariable(turtleIn);
        addVariable(maxDistance);
        addVariable(turtleOut);
    }
    
    @Override
    public void update() {
        double d = maxDistance.getValue();
        Turtle in = turtleIn.getValue();
        Turtle out = new Turtle();

        float maxLen = in.history.size();
        int i=0;
        setComplete(0);
        for (TurtleMove move : in.history) {
            TurtleMove newMove;
            if (move.type == MovementType.TOOL_CHANGE) {
                newMove = new TurtleMove(move);
            } else {
                newMove = new TurtleMove(move);
                var radian = (Math.random() * Math.PI * 2.0);
                var randomD = Math.random() * d;
                var dx = Math.cos(radian) * randomD;
                var dy = Math.sin(radian) * randomD;
                newMove.x += dx;
                newMove.y += dy;
            }
            out.history.add(newMove);
            setComplete((int)(i++ * 100f / maxLen));
        }

        setComplete(100);
        turtleOut.send(out);
    }
}
