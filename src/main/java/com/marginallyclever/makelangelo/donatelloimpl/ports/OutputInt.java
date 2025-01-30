package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.nodegraphcore.port.Output;

public class OutputInt extends Output<Integer> {
    public OutputInt(String _name, Integer startingValue) throws IllegalArgumentException {
        super(_name, Integer.class, startingValue);
    }
}
