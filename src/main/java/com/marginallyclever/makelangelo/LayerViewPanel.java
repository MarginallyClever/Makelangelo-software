package com.marginallyclever.makelangelo;

import com.marginallyclever.makelangelo.turtle.StrokeLayer;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.swing.*;
import java.awt.*;

public class LayerViewPanel extends JPanel {
    private final MainFrame mainFrame;

    public LayerViewPanel(MainFrame mainFrame) {
        super(new GridBagLayout());
        this.mainFrame = mainFrame;
        setName("LayerViewPanel");
    }

    public void setTurtle(Turtle turtle) {
        removeAll();
        if(turtle!=null) fillPanel(turtle);

        revalidate();
        repaint();
    }

    private void fillPanel(Turtle turtle) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx=0;
        c.gridy=0;
        c.weightx=1;

        for(var layer : turtle.getLayers()) {
            JPanel layerView = createOneLayerView(turtle,layer);
            add(layerView,c);
            c.gridy++;
        }
        JPanel spacer = new JPanel();
        c.weighty=1;
        add(spacer,c);
    }

    private JPanel createOneLayerView(Turtle turtle,StrokeLayer layer) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        // add a visibility checkbox
        JCheckBox visibilityCheckBox = new JCheckBox();
        visibilityCheckBox.setSelected(layer.isVisible());
        visibilityCheckBox.addActionListener(e -> {
            layer.setVisible(visibilityCheckBox.isSelected());
            if(mainFrame!=null) {
                mainFrame.setTurtle(turtle);
            }
        });
        panel.add(visibilityCheckBox);
        JLabel nameLabel = new JLabel(layer.getName() +" "+ layer.getAllLines().size() + " lines");
        panel.add(nameLabel);

        return panel;
    }
}
