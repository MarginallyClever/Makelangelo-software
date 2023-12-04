package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import java.awt.*;

public class SelectSlider extends Select {
	private final JSlider field = new JSlider();
	
	public SelectSlider(String internalName,String labelText,int top,int bottom,int defaultValue) {
		super(internalName);

		JLabel label = new JLabel(labelText, JLabel.LEADING);

		JLabel value = new JLabel("0",JLabel.TRAILING);

		field.setMaximum(top);
		field.setMinimum(bottom);
		field.setMinorTickSpacing(1);
		field.addChangeListener((e) -> {
	        int n = field.getValue();
	        value.setText(Integer.toString(n));
	        
			if(field.getValueIsAdjusting()) return;
			
			fireSelectEvent(null,n);
		});
		field.setValue(defaultValue);

		Dimension dim = new Dimension(30,1);
		value.setMinimumSize(dim);
		value.setPreferredSize(dim);
		value.setMaximumSize(dim);
		
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.CENTER);
		this.add(value,BorderLayout.LINE_END);
	}
	
	public int getValue() {
		return field.getValue();
	}
	
	public void setValue(int v) {
		field.setValue(v);
	}
}
