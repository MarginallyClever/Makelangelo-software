package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.donatello.SwingProvider;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.nodegraphcore.port.Input;

import java.awt.*;

public class InputInt extends Input<Integer> implements SwingProvider {
    private SelectInteger selectInteger;

    public InputInt(String name, Integer startingValue) throws IllegalArgumentException {
        super(name, Integer.class, startingValue);
    }

    @Override
    public Component getSwingComponent(Component parent) {
        if(selectInteger==null) {
            selectInteger = new SelectInteger(name,name,this.value);
            selectInteger.addSelectListener( evt -> setValue(evt.getNewValue()) );
        }
        return selectInteger;
    }
}
