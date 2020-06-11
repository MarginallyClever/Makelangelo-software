package com.marginallyclever.makelangelo.select;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.convenience.GridBagConstraintsLabel;
import com.marginallyclever.convenience.GridBagConstraintsValue;

public class SelectSlider extends Select {
	private JLabel label;
	private JSlider field;
	
	public SelectSlider(String labelText,int top,int bottom,int defaultValue) {
		super();
		
		label = new JLabel(labelText,SwingConstants.LEFT);
		
		field = new JSlider();
		field.setMaximum(top);
		field.setMinimum(bottom);
		field.setMinorTickSpacing(1);
		field.setValue(defaultValue);
		field.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setChanged();
				notifyObservers();
			}
		});

		panel.setLayout(new GridLayout(0,1));
		panel.add(label,new GridBagConstraintsLabel());
		panel.add(field,new GridBagConstraintsValue());
	}
	
	public int getValue() {
		return field.getValue();
	}
	
	public void setValue(int v) {
		field.setValue(v);
	}
}
