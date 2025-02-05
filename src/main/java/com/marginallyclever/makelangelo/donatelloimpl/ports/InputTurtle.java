package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.donatello.SwingProvider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.port.Input;

import java.awt.*;

public class InputTurtle extends Input<Turtle> implements SwingProvider {
    public InputTurtle(String name) throws IllegalArgumentException {
        super(name, Turtle.class, new Turtle());
    }

    @Override
    public Component getSwingComponent(Component parent) {
        return null;
    }
}
