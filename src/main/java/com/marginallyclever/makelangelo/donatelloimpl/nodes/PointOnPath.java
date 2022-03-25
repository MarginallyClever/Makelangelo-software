package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.NodeVariable;

/**
 * <p>(px,py) = path(index), where path(0) is the start and path(path.length) is the end.</p>
 * <p>(nx,ny) = the approximate normal at path(index).  This is approximated by finding</p>
 * <pre>normalize(path(index+epsilon) - path(index))</pre>
 * <p>for some very small epsilon, and taking into account the start and end of the path.</p>
 * <p>If the path is of zero-length then (0,0) will be generated.</p>
 * <p>path.length can be obtained from LoadTurtle.</p>
 */
public class PointOnPath extends Node {
    private final NodeVariable<Turtle> path = NodeVariable.newInstance("path", Turtle.class, new Turtle(),true,false);
    private final NodeVariable<Number> index = NodeVariable.newInstance("index", Number.class, 0,true,false);
    private final NodeVariable<Number> px = NodeVariable.newInstance("px", Number.class, 0,false,true);
    private final NodeVariable<Number> py = NodeVariable.newInstance("py", Number.class, 0,false,true);
    private final NodeVariable<Number> nx = NodeVariable.newInstance("nx", Number.class, 0,false,true);
    private final NodeVariable<Number> ny = NodeVariable.newInstance("ny", Number.class, 0,false,true);

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
    public void update() throws Exception {
        Turtle sum = new Turtle();
        Turtle myPath = path.getValue();
        double total = myPath.getDrawDistance();
        if(total!=0) {
            double c0 = index.getValue().doubleValue();
            if (c0 > 0) {
                double c1 = c0 + EPSILON;
                Point2D p0 = myPath.interpolate(c0);
                px.setValue(p0.x);
                px.setValue(p0.y);

                Point2D p1;
                if(c1>total) {
                    c1=total;
                    p1 = myPath.interpolate(total);
                    p0 = myPath.interpolate(total-EPSILON);
                } else {
                    p1 = myPath.interpolate(c1);
                }
                double dx = p1.x - p0.x;
                double dy = p1.y - p0.y;
                Point2D n = new Point2D(dx,dy);
                n.normalize();
                nx.setValue(n.x);
                ny.setValue(n.y);
            }
        } else {
            px.setValue(0);
            px.setValue(0);
            nx.setValue(1);
            ny.setValue(0);
        }
        cleanAllInputs();
    }
}
