package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.nodegraphcore.port.Input;

/**
 * {@link Input} for a {@link ListOfPoints}.
 */
public class InputPoints extends Input<ListOfPoints> {
    public InputPoints(String _name) throws IllegalArgumentException {
        super(_name, ListOfPoints.class, new ListOfPoints());
    }
}
