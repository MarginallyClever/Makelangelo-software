package com.marginallyclever.makelangelo.donatelloimpl.nodes.lines;

import com.marginallyclever.makelangelo.turtle.ListOfLines;
import com.marginallyclever.nodegraphcore.port.Input;

/**
 * {@link Input} for a {@link ListOfLines}.
 */
public class InputLines extends Input<ListOfLines> {
    public InputLines(String _name) throws IllegalArgumentException {
        super(_name, ListOfLines.class, new ListOfLines());
    }
}
