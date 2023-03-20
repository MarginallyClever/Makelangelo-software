package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A JCheckBox that sets itself up to format true/false. 
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectBoolean extends Select {
	private final JCheckBox field = new JCheckBox();
	
	public SelectBoolean(String internalName,String labelKey,boolean arg0) {
		super(internalName);

		JLabel label = new JLabel(labelKey, JLabel.LEADING);

		field.setSelected(arg0);
		field.setBorder(new EmptyBorder(0,0,0,0));
		field.addItemListener((e)-> {
			boolean newValue = field.isSelected();
			boolean oldValue = !newValue;
			firePropertyChange(oldValue, newValue);
		});

		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}
	
	public boolean isSelected() {
		return field.isSelected();
	}

	public void setSelected(boolean b) {
		field.setSelected(b);
	}	

}
