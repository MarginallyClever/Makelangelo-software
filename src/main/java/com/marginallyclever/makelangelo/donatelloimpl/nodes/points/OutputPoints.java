package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.nodegraphcore.port.Output;

/**
 * {@link Output} for a {@link ListOfPoints}.
 */
public class OutputPoints extends Output<ListOfPoints> {
    public OutputPoints(String _name) throws IllegalArgumentException {
        super(_name, ListOfPoints.class, new ListOfPoints());
    }
}
