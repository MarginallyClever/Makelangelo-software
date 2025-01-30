package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.donatello.SwingProvider;
import com.marginallyclever.makelangelo.select.SelectFile;

import java.awt.*;

public class InputFilename extends InputString implements SwingProvider {
    private SelectFile selectFile;

    public InputFilename(String name, String startingValue) throws IllegalArgumentException {
        super(name, startingValue);
    }

    @Override
    public Component getSwingComponent(Component parent) {
        if(selectFile==null) {
            selectFile = new SelectFile(name,name,value,parent);
            selectFile.addSelectListener( evt -> {
                setValue(evt.getNewValue());
            });
        }
        return selectFile;
    }
}
