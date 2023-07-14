package com.marginallyclever.makelangelo;

import com.marginallyclever.makelangelo.rangeslider.RangeSlider;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class MakeleangeloRangeSlider extends JPanel {
    private final RangeSlider rangeSlider;
    private final JLabel labelRangeMin = new JLabel();
    private final JLabel labelRangeMax = new JLabel();

    public MakeleangeloRangeSlider() {
        super(new BorderLayout());

        rangeSlider = new RangeSlider();
        rangeSlider.addChangeListener(this::onChangeSlider);

        labelRangeMax.setHorizontalAlignment(SwingConstants.RIGHT);

        labelRangeMin.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        labelRangeMax.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        labelRangeMin.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        labelRangeMax.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        Dimension d = labelRangeMin.getPreferredSize();
        d.width=60;
        labelRangeMin.setPreferredSize(d);
        labelRangeMax.setPreferredSize(d);

        this.add(labelRangeMin, BorderLayout.WEST);
        this.add(rangeSlider, BorderLayout.CENTER);
        this.add(labelRangeMax, BorderLayout.EAST);
    }

    /**
     * When the two-headed drawing start/end slider is moved
     * @param e {@link ChangeEvent} describing the move.
     */
    private void onChangeSlider(ChangeEvent e) {
        RangeSlider slider = (RangeSlider)e.getSource();
        int bottom = slider.getValue();
        int top = slider.getUpperValue();
        labelRangeMin.setText(Integer.toString(bottom));
        labelRangeMax.setText(Integer.toString(top));

        firePropertyChange("slider",null,null);
    }

    public void addChangeListener(ChangeListener listener) {
    	rangeSlider.addChangeListener(listener);
    }

    public void setLimits(int bottom, int top) {
        rangeSlider.setMinimum(bottom);
        rangeSlider.setValue(bottom);
        rangeSlider.setMaximum(top);
        rangeSlider.setUpperValue(top);
    }

    public int getValue() {
        return rangeSlider.getValue();
    }

    public int getUpperValue() {
        return rangeSlider.getUpperValue();
    }

    public int getBottom() {
        return rangeSlider.getValue();
    }

    public int getTop() {
        return rangeSlider.getUpperValue();
    }
}
