package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.donatello.SwingProvider;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.nodegraphcore.port.Input;

import java.awt.*;

public class InputBoolean extends Input<Boolean> implements SwingProvider {
    private SelectBoolean selectBoolean;

    public InputBoolean(String name, Boolean startingValue) throws IllegalArgumentException {
        super(name, Boolean.class, startingValue);
    }

    @Override
    public Component getSwingComponent(Component parent) {
        if(selectBoolean==null) {
            selectBoolean = new SelectBoolean(name,name,this.value);
            selectBoolean.addSelectListener( evt -> {
                setValue(evt.getNewValue());
            });
        }
        return selectBoolean;
    }
}
