package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Observable;

import javax.swing.JPanel;

public class Select extends Observable implements FocusListener {
	protected JPanel panel;
	
	protected Select() {
		super();
		panel = new JPanel();
		panel.setLayout(new BorderLayout(2,0));
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub
	}
	
	JPanel getPanel() {
		return panel;
	}
}
