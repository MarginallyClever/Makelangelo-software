package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;


import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.donatello.ports.OutputDouble;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtlePathWalker;
import com.marginallyclever.nodegraphcore.Node;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;


/**
 * <p>(px,py) = path(index), where path(0) is the start and path(path.length) is the end.</p>
 * <p>(nx,ny) = the approximate normal at path(index).  This is approximated by finding</p>
 * <pre>{@code
 * normalize(path(index+epsilon) - path(index))
 * }</pre>
 * <p>for some very small epsilon, and taking into account the start and end of the path.</p>
 * <p>If the path is of zero-length then (0,0) will be generated.</p>
 * <p>path.length can be obtained from LoadTurtle.</p>
 */
public class PointOnPath extends Node {
    private final InputTurtle path = new InputTurtle("path");
    private final InputInt index = new InputInt("index", 0);
    private final OutputDouble px = new OutputDouble("px", 0d);
    private final OutputDouble py = new OutputDouble("py", 0d);
    private final OutputDouble nx = new OutputDouble("nx", 0d);
    private final OutputDouble ny = new OutputDouble("ny", 0d);

    public PointOnPath() {
        super("PointOnPath");
        addPort(path);
        addPort(px);
        addPort(py);
        addPort(nx);
        addPort(ny);
    }

    private static final double EPSILON=0.00001;

    @Override
    public void update() {
        Turtle myPath = path.getValue();
        double total = myPath.getDrawDistance();
        double c0 = index.getValue().doubleValue();
        if(total==0 || c0 <= 0) {
            px.setValue(0d);
            px.setValue(0d);
            nx.setValue(1d);
            ny.setValue(0d);
            return;
        }

        double c1 = c0 + EPSILON;
        if(c1>total) {
            c1 = total;
            c0 = total - EPSILON;
        }
        TurtlePathWalker walker = new TurtlePathWalker(myPath);
        Point2d p0 = walker.walk(c0);
        Point2d p1 = walker.walk(c1-c0);
        double dx = p1.x - p0.x;
        double dy = p1.y - p0.y;
        var n = new Vector2d(dx,dy);
        n.normalize();
        px.setValue(p0.x);
        py.setValue(p0.y);
        nx.setValue(n.x);
        ny.setValue(n.y);
    }
}
