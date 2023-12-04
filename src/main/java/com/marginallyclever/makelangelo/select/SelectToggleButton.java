package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import java.awt.*;

/**
 * A toggle button that does nothing until you attach an observer.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectToggleButton extends Select {
	private final JToggleButton button;

	public SelectToggleButton(String internalName, AbstractAction action) {
		super(internalName);
		button = new JToggleButton(action);
	}

	public SelectToggleButton(String internalName, String labelText) {
		super(internalName);
		
		button = new JToggleButton(labelText);

		button.addActionListener((e) -> {
			fireSelectEvent(!button.isSelected(),button.isSelected());
		});

		this.add(button,BorderLayout.CENTER);
	}
	
	public void doClick() {
		if(button!=null) button.doClick();
	}
	
	public void setText(String label) {
		if(button!=null) button.setText(label);
	}
	
	public void setEnabled(boolean b) {
		if(button!=null) button.setEnabled(b);
	}

	public void setForeground(Color fg) {
		if(button!=null) button.setForeground(fg);
	}

	public boolean isSelected() {
		return button.isSelected();
	}
}
