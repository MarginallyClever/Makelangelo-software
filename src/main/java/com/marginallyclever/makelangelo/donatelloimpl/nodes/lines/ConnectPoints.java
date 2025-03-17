package com.marginallyclever.makelangelo.donatelloimpl.nodes.lines;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.nodes.points.InputPoints;
import com.marginallyclever.makelangelo.turtle.Line2d;
import com.marginallyclever.makelangelo.turtle.ListOfLines;
import com.marginallyclever.makelangelo.turtle.ListOfPoints;
import com.marginallyclever.nodegraphcore.Node;

/**
 * Connects points that are within a certain distance of each other.
 */
public class ConnectPoints extends Node {
    private final InputPoints points = new InputPoints("points");
    private final InputDouble maxDistance = new InputDouble("max distance", 10.0);
    private final OutputLines lines = new OutputLines("lines");

    public ConnectPoints() {
        super("ConnectPoints");
        addPort(points);
        addPort(maxDistance);
        addPort(lines);
    }

    @Override
    public void update() {
        ListOfPoints input = points.getValue();
        double dist = this.maxDistance.getValue();
        double d2 = dist * dist;

        ListOfLines result = new ListOfLines();
        var list = input.getAllPoints();
        if(list.size() < 2) {
            lines.setValue(result);
            return;
        }

        setComplete(0);
        int total = list.size() + 1;
        var prev = list.getFirst();
        Line2d line=null;
        for(int i = 1; i < list.size(); i++) {
            var p = list.get(i);
            if(prev.distanceSquared(p) < d2) {
                if(line == null) {
                    result.add(line = new Line2d());
                    line.add(prev);
                }
                line.add(p);
            } else {
                line = null;
            }
            prev = p;
            setComplete(100 * i / total);
        }

        lines.setValue(result);
        setComplete(100);
    }
}
