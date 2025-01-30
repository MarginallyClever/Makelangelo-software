package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.port.Output;

public class OutputTurtle extends Output<Turtle>  {
    public OutputTurtle(String _name) throws IllegalArgumentException {
        super(_name, Turtle.class, new Turtle());
    }
}
