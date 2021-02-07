package com.marginallyclever.convenience.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;

/**
 * A button that does nothing until you attach an observer.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectButton extends Select {
	private JButton button;

	public SelectButton(AbstractAction action) {
		super();
		button = new JButton(action);
	}
	
	public SelectButton(String labelText) {
		super();
		
		button = new JButton(labelText);
		Select parent = this;
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				notifyPropertyChangeListeners(new PropertyChangeEvent(parent,"click",null,null));
			}
		});

		panel.add(button,BorderLayout.CENTER);
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
