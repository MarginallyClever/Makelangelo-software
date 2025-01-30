package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.donatello.SwingProvider;
import com.marginallyclever.makelangelo.select.SelectTextField;
import com.marginallyclever.nodegraphcore.port.Input;

import java.awt.*;

public class InputString extends Input<String> implements SwingProvider {
    private SelectTextField selectTextField;

    public InputString(String name, String startingValue) throws IllegalArgumentException {
        super(name, String.class, startingValue);
    }

    @Override
    public Component getSwingComponent(Component parent) {
        if(selectTextField==null) {
            selectTextField = new SelectTextField(name,name,this.value);
            selectTextField.addSelectListener( evt -> {
                setValue(evt.getNewValue());
            });
        }
        return selectTextField;
    }
}
