package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * A button that does nothing until you attach an observer.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectButton extends Select {
	private JButton button;
	
	public SelectButton(String labelText) {
		super();
		
		button = new JButton(labelText);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setChanged();
				notifyObservers();
			}
			
		});

		panel.add(button,BorderLayout.CENTER);
	}
	
	public void doClick() {
		button.doClick();
	}
}
