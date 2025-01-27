package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtlePathWalker;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;
import com.marginallyclever.nodegraphcore.Node;


/**
 * <p>(px,py) = path(index), where path(0) is the start and path(path.length) is the end.</p>
 * <p>(nx,ny) = the approximate normal at path(index).  This is approximated by finding</p>
 * <pre>normalize(path(index+epsilon) - path(index))</pre>
 * <p>for some very small epsilon, and taking into account the start and end of the path.</p>
 * <p>If the path is of zero-length then (0,0) will be generated.</p>
 * <p>path.length can be obtained from LoadTurtle.</p>
 */
public class PointOnPath extends Node {
    private final Input<Turtle> path = new Input<>("path", Turtle.class, new Turtle());
    private final Input<Number> index = new Input<>("index", Number.class, 0);
    private final Output<Number> px = new Output<>("px", Number.class, 0);
    private final Output<Number> py = new Output<>("py", Number.class, 0);
    private final Output<Number> nx = new Output<>("nx", Number.class, 0);
    private final Output<Number> ny = new Output<>("ny", Number.class, 0);

    public PointOnPath() {
        super("PointOnPath");
        addVariable(path);
        addVariable(px);
        addVariable(py);
        addVariable(nx);
        addVariable(ny);
    }

    private static final double EPSILON=0.00001;

    @Override
    public void update() {
        Turtle myPath = path.getValue();
        double total = myPath.getDrawDistance();
        double c0 = index.getValue().doubleValue();
        if(total==0 || c0 <= 0) {
            px.send(0);
            px.send(0);
            nx.send(1);
            ny.send(0);
            return;
        }

        double c1 = c0 + EPSILON;
        if(c1>total) {
            c1 = total;
            c0 = total - EPSILON;
        }
        TurtlePathWalker walker = new TurtlePathWalker(myPath);
        Point2D p0 = walker.walk(c0);
        Point2D p1 = walker.walk(c1-c0);
        double dx = p1.x - p0.x;
        double dy = p1.y - p0.y;
        Point2D n = new Point2D(dx,dy);
        n.normalize();
        px.send(p0.x);
        px.send(p0.y);
        nx.send(n.x);
        ny.send(n.y);
    }
}
