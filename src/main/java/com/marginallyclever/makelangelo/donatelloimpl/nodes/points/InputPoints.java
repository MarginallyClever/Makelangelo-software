package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.makelangelo.turtle.ConcreteListOfPoints;
import com.marginallyclever.makelangelo.turtle.ListOfPoints;
import com.marginallyclever.nodegraphcore.port.Input;

/**
 * {@link Input} for a {@link ConcreteListOfPoints}.
 */
public class InputPoints extends Input<ListOfPoints> {
    public InputPoints(String _name) throws IllegalArgumentException {
        super(_name, ListOfPoints.class, new ConcreteListOfPoints());
    }
}
