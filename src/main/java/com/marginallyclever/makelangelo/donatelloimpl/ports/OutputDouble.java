package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.nodegraphcore.port.Output;

public class OutputDouble extends Output<Double> {
    public OutputDouble(String _name, Double startingValue) throws IllegalArgumentException {
        super(_name, Double.class, startingValue);
    }
}
