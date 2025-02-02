package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.donatello.SwingProvider;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;

import java.awt.*;

public class InputOneOfMany extends InputInt implements SwingProvider {
    private SelectOneOfMany selectOneOfMany;
    private String [] options;

    public InputOneOfMany(String name, Integer startingValue) throws IllegalArgumentException {
        super(name, startingValue);
    }

    @Override
    public Component getSwingComponent(Component parent) {
        if(selectOneOfMany == null) {
            selectOneOfMany = new SelectOneOfMany(name,name);
            if(options!=null) selectOneOfMany.setNewList(options);
            selectOneOfMany.addSelectListener(evt-> setValue(evt.getNewValue()) );
        }
        return selectOneOfMany;
    }

    public void setOptions(String[] options) {
        this.options = options;
        if(selectOneOfMany!=null) {
            selectOneOfMany.setNewList(options);
        }
    }
}
