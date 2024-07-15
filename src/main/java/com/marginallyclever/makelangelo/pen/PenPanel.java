package com.marginallyclever.makelangelo.pen;

import com.marginallyclever.makelangelo.select.SelectColor;
import com.marginallyclever.makelangelo.select.SelectDouble;

import javax.swing.*;
import java.awt.*;

/**
 * Describes a pen that can be used to draw on a surface.
 */
public class PenPanel extends JPanel {
    private final Pen myPen;

    public PenPanel() {
        this(new Pen("Black"));
    }

    public PenPanel(final Pen pen) {
        super(new FlowLayout(FlowLayout.LEADING));
        this.myPen = pen;

        setName(PenPanel.class.getSimpleName());

        SelectColor colorChoice = new SelectColor("PenColor","", pen.color, this);
        colorChoice.addPropertyChangeListener(e -> pen.color = colorChoice.getColor());

        SelectDouble diameterChoice = new SelectDouble("PenDiameter","",pen.diameter);
        diameterChoice.addPropertyChangeListener(e -> pen.diameter = Math.abs(diameterChoice.getValue()));

        add(colorChoice);
        add(diameterChoice);
        add(new JLabel("⌀ mm"));

        setPreferredSize(new Dimension(250,25));
    }

    public void setSelected(boolean selected) {
        if (selected) {
            setBackground(Color.LIGHT_GRAY);
        } else {
            setBackground(UIManager.getColor("Panel.background"));
        }
    }

    public Pen getPen() {
        return myPen;
    }
}
