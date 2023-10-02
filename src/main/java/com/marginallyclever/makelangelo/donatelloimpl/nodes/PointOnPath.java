package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.Packet;

/**
 * <p>(px,py) = path(index), where path(0) is the start and path(path.length) is the end.</p>
 * <p>(nx,ny) = the approximate normal at path(index).  This is approximated by finding</p>
 * <pre>normalize(path(index+epsilon) - path(index))</pre>
 * <p>for some very small epsilon, and taking into account the start and end of the path.</p>
 * <p>If the path is of zero-length then (0,0) will be generated.</p>
 * <p>path.length can be obtained from LoadTurtle.</p>
 */
public class PointOnPath extends Node {
    private final DockReceiving<Turtle> path = new DockReceiving<>("path", Turtle.class, new Turtle());
    private final DockReceiving<Number> index = new DockReceiving<>("index", Number.class, 0);
    private final DockShipping<Number> px = new DockShipping<>("px", Number.class, 0);
    private final DockShipping<Number> py = new DockShipping<>("py", Number.class, 0);
    private final DockShipping<Number> nx = new DockShipping<>("nx", Number.class, 0);
    private final DockShipping<Number> ny = new DockShipping<>("ny", Number.class, 0);

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
        if(path.hasPacketWaiting()) path.receive();
        if(index.hasPacketWaiting()) index.receive();

        Turtle myPath = path.getValue();
        double total = myPath.getDrawDistance();
        if(total!=0) {
            double c0 = index.getValue().doubleValue();
            if (c0 > 0) {
                double c1 = c0 + EPSILON;
                Point2D p0 = myPath.interpolate(c0);
                px.send(new Packet<>(p0.x));
                px.send(new Packet<>(p0.y));

                Point2D p1;
                if(c1>total) {
                    p1 = myPath.interpolate(total);
                    p0 = myPath.interpolate(total-EPSILON);
                } else {
                    p1 = myPath.interpolate(c1);
                }
                double dx = p1.x - p0.x;
                double dy = p1.y - p0.y;
                Point2D n = new Point2D(dx,dy);
                n.normalize();
                nx.send(new Packet<>(n.x));
                ny.send(new Packet<>(n.y));
            }
        } else {
            px.send(new Packet<>(0));
            px.send(new Packet<>(0));
            nx.send(new Packet<>(1));
            ny.send(new Packet<>(0));
        }
    }
}
