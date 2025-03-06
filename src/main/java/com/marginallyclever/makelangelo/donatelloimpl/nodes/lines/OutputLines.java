package com.marginallyclever.makelangelo.donatelloimpl.nodes.lines;

import com.marginallyclever.makelangelo.turtle.ListOfLines;
import com.marginallyclever.nodegraphcore.port.Output;

/**
 * {@link Output} for a {@link ListOfLines}.
 */
public class OutputLines extends Output<ListOfLines> {
    public OutputLines(String _name) throws IllegalArgumentException {
        super(_name, ListOfLines.class, new ListOfLines());
    }
}