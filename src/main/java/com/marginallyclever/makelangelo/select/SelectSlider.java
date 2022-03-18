package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JSlider;

public class SelectSlider extends Select {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6440245611041406805L;
	private JLabel label;
	private JSlider field;
	private JLabel value;
	
	public SelectSlider(String internalName,String labelText,int top,int bottom,int defaultValue) {
		super(internalName);

		label = new JLabel(labelText,JLabel.LEADING);
		value = new JLabel("0",JLabel.TRAILING);
		
		field = new JSlider();
		field.setMaximum(top);
		field.setMinimum(bottom);
		field.setMinorTickSpacing(1);
		field.addChangeListener((e) -> {
	        int n = field.getValue();
	        value.setText(Integer.toString(n));
	        
			if(field.getValueIsAdjusting()) return;
			
			firePropertyChange(null,n);			
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
