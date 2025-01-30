package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.donatello.SwingProvider;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.nodegraphcore.port.Input;

import java.awt.*;

public class InputDouble extends Input<Double> implements SwingProvider {
    private SelectDouble selectDouble;

    public InputDouble(String name, Double startingValue) throws IllegalArgumentException {
        super(name, Double.class, startingValue);
    }

    @Override
    public Component getSwingComponent(Component parent) {
        if(selectDouble==null) {
            selectDouble = new SelectDouble(name,name,this.value);
            selectDouble.addSelectListener( evt -> {
                setValue(evt.getNewValue());
            });
        }
        return selectDouble;
    }


}
