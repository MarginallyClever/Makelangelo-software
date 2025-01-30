package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.donatello.SwingProvider;
import com.marginallyclever.makelangelo.select.SelectColor;
import com.marginallyclever.nodegraphcore.port.Input;

import java.awt.*;

public class InputColor extends Input<Color> implements SwingProvider {
    private SelectColor selectColor;

    public InputColor(String name, Color startingValue) throws IllegalArgumentException {
        super(name, Color.class, startingValue);
    }

    @Override
    public Component getSwingComponent(Component parent) {
        if(selectColor==null) {
            selectColor = new SelectColor(name,name,value,parent);
            selectColor.addSelectListener( evt -> {
                setValue(evt.getNewValue());
            });
        }
        return selectColor;
    }
}
