package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

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

		panel.setLayout(new BorderLayout());
		panel.add(button,BorderLayout.LINE_START);
	}
	
	public void doClick() {
		button.doClick();
	}
}
