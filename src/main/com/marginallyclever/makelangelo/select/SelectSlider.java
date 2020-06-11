package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A slider to restrict integer values to the range you want. 
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectSlider extends Select {
	private JLabel label;
	private JSlider field;
	private JLabel value;
	
	public SelectSlider(String labelText,int top,int bottom,int defaultValue) {
		super();

		label = new JLabel(labelText,JLabel.LEADING);
		value = new JLabel("0",JLabel.TRAILING);
		
		field = new JSlider();
		field.setMaximum(top);
		field.setMinimum(bottom);
		field.setMinorTickSpacing(1);
		field.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
		        int n = field.getValue();
		        value.setText(Integer.toString(n));
		        setChanged();
		        notifyObservers();
			}
		});
		field.setValue(defaultValue);

		Dimension dim = new Dimension(30,1);
		value.setMinimumSize(dim);
		value.setPreferredSize(dim);
		value.setMaximumSize(dim);
		
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.CENTER);
		panel.add(value,BorderLayout.LINE_END);
	}
	
	public int getValue() {
		return field.getValue();
	}
	
	public void setValue(int v) {
		field.setValue(v);
	}
}
