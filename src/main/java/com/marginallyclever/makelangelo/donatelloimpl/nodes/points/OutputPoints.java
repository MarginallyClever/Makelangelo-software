package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.makelangelo.turtle.ConcreteListOfPoints;
import com.marginallyclever.makelangelo.turtle.ListOfPoints;
import com.marginallyclever.nodegraphcore.port.Output;

/**
 * {@link Output} for a {@link ConcreteListOfPoints}.
 */
public class OutputPoints extends Output<ListOfPoints> {
    public OutputPoints(String _name) throws IllegalArgumentException {
        super(_name, ListOfPoints.class, new ConcreteListOfPoints());
    }
}
