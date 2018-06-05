package com.marginallyclever.makelangelo;

import javax.swing.JCheckBox;

/**
 * A JCheckBox that sets itself up to format true/false. 
 * @author Dan Royer
 * @since 7.8.0
 */
public class SelectBoolean extends JCheckBox {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8222996536287315655L;
	
	public SelectBoolean(boolean arg0) {
		this.setSelected(arg0);
	}
	
	public boolean getValue() {
		return this.isSelected();
	}
}
