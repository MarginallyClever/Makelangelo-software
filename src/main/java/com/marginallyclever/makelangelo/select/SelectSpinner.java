package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SelectSpinner extends Select {
    private final JSpinner field;

    public SelectSpinner(String internalName, String labelText, int min, int max, int value) {
        super(internalName);

        JLabel label = new JLabel(labelText, JLabel.LEADING);

        List<Integer> list = new ArrayList<>();
        for (int i = min; i<= max; i++) {
            list.add(i);
        }
        field = new JSpinner(new SpinnerListModel(list));

        Dimension d = field.getPreferredSize();
        d.width = 50;
        field.setPreferredSize(d);
        field.setValue(value);

        this.add(label, BorderLayout.LINE_START);
        this.add(field,BorderLayout.LINE_END);
    }

    public int getValue() {
        return (int) field.getValue();
    }

    public void setValue(int v) {
        field.setValue(v);
    }
}
