package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.AbstractAction;
import javax.swing.JButton;

/**
 * A button that does nothing until you attach an observer.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectButton extends Select {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7096181261934634708L;
	private JButton button;

	public SelectButton(String internalName,AbstractAction action) {
		super(internalName);
		button = new JButton(action);
	}
	
	public SelectButton(String internalName,String labelText) {
		super(internalName);
		
		button = new JButton(labelText);
		button.addActionListener((e) -> {
			notifyPropertyChangeListeners(null,null);
		});

		this.add(button,BorderLayout.CENTER);
	}
	
	public void doClick() {
		button.doClick();
	}
	
	public void setText(String label) {
		button.setText(label);
	}
	
	public void setEnabled(boolean b) {
		button.setEnabled(b);
	}

	public void setForeground(Color fg) {
		button.setForeground(fg);
	}
}
