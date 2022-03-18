package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 * A JCheckBox that sets itself up to format true/false. 
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectBoolean extends Select {
	/**
	 * 
	 */
	private static final long serialVersionUID = 598928132833398652L;
	private JLabel label;
	private JCheckBox field;
	
	public SelectBoolean(String internalName,String labelKey,boolean arg0) {
		super(internalName);
		
		label = new JLabel(labelKey,JLabel.LEADING);
		
		field = new JCheckBox();
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
